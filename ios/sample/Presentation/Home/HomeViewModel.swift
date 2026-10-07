import Foundation

/// The only screen with both RetailMedia formats, each from its own ad unit: sponsored products come with the
/// catalog response (handled by `ProductsViewModel`), display ads are requested separately via `/ads` and shown
/// as a slider after the first row of cards.
final class HomeViewModel: ProductsViewModel {

    private static let adSliderPosition = 2

    @ObservationIgnored private let catalog: CatalogRepository
    @ObservationIgnored private let displayAds: ScreenDisplayAds

    init(
        catalog: CatalogRepository,
        displayAds: ScreenDisplayAds,
        adRepository: RetailMediaAdRepository,
        cart: CartRepository
    ) {
        self.catalog = catalog
        self.displayAds = displayAds
        super.init(cart: cart, adRepository: adRepository)
        refresh()
    }

    override func refresh() {
        super.refresh()
        displayAds.reload()
    }

    override func fetch(page: Int, pageSize: Int) async throws -> CatalogPage {
        try await catalog.home(page: page, pageSize: pageSize)
    }

    override func feed(_ rows: [ProductRow]) -> [FeedItem] {
        let products = rows.map(FeedItem.product)
        guard let ads = displayAds.ads else { return products }
        let position = min(Self.adSliderPosition, products.count)
        return Array(products[..<position]) + [.adSlider(ads)] + Array(products[position...])
    }
}
