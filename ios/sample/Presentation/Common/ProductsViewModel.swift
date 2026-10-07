import Foundation
import Observation

@Observable
class ProductsViewModel {

    private(set) var phase: ProductsPhase
    private(set) var loaded: [Product] = []
    private var sponsoredAds: [SponsoredAd] = []

    @ObservationIgnored var onEvent: ((ProductsEvent) -> Void)?
    @ObservationIgnored private let cart: CartRepository
    @ObservationIgnored private let adRepository: RetailMediaAdRepository
    @ObservationIgnored private let pageSize: Int
    @ObservationIgnored private var page = 0
    @ObservationIgnored private var hasMore = true
    @ObservationIgnored private var loadedIds: Set<String> = []
    @ObservationIgnored private var lastAdReadyResponse: String?
    @ObservationIgnored private var loadTask: Task<Void, Never>?
    @ObservationIgnored private var busy = false

    var state: ProductsUiState {
        switch phase {
        case .idle:
            return .idle
        case .loading:
            return .loading
        case .error:
            return .error
        case .content(let appending, let hasMore):
            guard !loaded.isEmpty else { return .empty }
            return .content(ProductsContent(items: feed(rows), appending: appending, hasMore: hasMore))
        }
    }

    private var rows: [ProductRow] {
        let quantities = Dictionary(cart.cart.items.map { ($0.id, $0.quantity) }, uniquingKeysWith: { first, _ in first })
        return buildProductRows(products: loaded, quantities: quantities, sponsoredAds: sponsoredAds)
    }

    init(
        cart: CartRepository,
        adRepository: RetailMediaAdRepository,
        initialPhase: ProductsPhase = .loading,
        pageSize: Int = 20
    ) {
        self.cart = cart
        self.adRepository = adRepository
        self.phase = initialPhase
        self.pageSize = pageSize
        Task {
            do {
                _ = try await cart.get()
            } catch {
                onEvent?(.cartActionFailed)
            }
        }
    }

    func fetch(page: Int, pageSize: Int) async throws -> CatalogPage {
        fatalError("Subclasses must override fetch(page:pageSize:)")
    }

    func feed(_ rows: [ProductRow]) -> [FeedItem] {
        rows.map(FeedItem.product)
    }

    /// A refresh cancels the page still in flight: otherwise its result would land in the new, empty list.
    func refresh() {
        loadTask?.cancel()
        loadTask = nil
        page = 0
        hasMore = true
        loaded = []
        loadedIds = []
        sponsoredAds = []
        lastAdReadyResponse = nil
        phase = .loading
        loadNext(reset: true)
    }

    func loadMore() {
        guard loadTask == nil, hasMore else { return }
        if case .content = phase {
            phase = .content(appending: true, hasMore: hasMore)
        }
        loadNext(reset: false)
    }

    func increase(_ product: Product) {
        guard !busy else { return }
        busy = true
        Task {
            do {
                _ = try await cart.add(productId: product.id, quantity: 1)
            } catch {
                onEvent?(.cartActionFailed)
            }
            busy = false
        }
    }

    func decrease(_ product: Product) {
        guard !busy else { return }
        busy = true
        Task {
            do {
                let quantity = cart.cart.items.first(where: { $0.id == product.id })?.quantity ?? 0
                _ = try await cart.setQuantity(productId: product.id, quantity: quantity - 1)
            } catch {
                onEvent?(.cartActionFailed)
            }
            busy = false
        }
    }

    private func loadNext(reset: Bool) {
        loadTask = Task {
            do {
                let result = try await fetch(page: page + 1, pageSize: pageSize)
                try Task.checkCancellation()
                let newProducts = result.products.items.filter { loadedIds.insert($0.id).inserted }
                if let adSlot = result.adSlot {
                    sponsoredAds += try await loadSponsoredAds(adSlot, newProducts: newProducts)
                }
                page += 1
                hasMore = result.products.hasMore
                loaded.append(contentsOf: newProducts)
                phase = .content(appending: false, hasMore: hasMore)
                loadTask = nil
                if newProducts.isEmpty, hasMore {
                    loadNext(reset: false)
                }
                return
            } catch is CancellationError {
                return
            } catch {
                if Task.isCancelled { return }
                if reset || loaded.isEmpty {
                    phase = .error
                } else {
                    phase = .content(appending: false, hasMore: hasMore)
                }
            }
            loadTask = nil
        }
    }

    /// The products are deduplicated across pages, so a page's ads are matched only against the sponsored cards
    /// this page actually added. A `readyResponse` the previous page already brought is not loaded again: it
    /// would create new ad objects for cards that already have theirs.
    private func loadSponsoredAds(_ adSlot: AdSlot, newProducts: [Product]) async throws -> [SponsoredAd] {
        guard adSlot.readyResponse != lastAdReadyResponse else { return [] }
        lastAdReadyResponse = adSlot.readyResponse
        let offerIds = newProducts.filter(\.sponsored).map(\.id)
        return try await bestEffort {
            try await adRepository.loadProductPromo(slot: adSlot, offerIds: offerIds)
        } ?? []
    }
}
