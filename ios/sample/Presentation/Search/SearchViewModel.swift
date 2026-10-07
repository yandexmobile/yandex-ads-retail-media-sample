import Foundation

final class SearchViewModel: ProductsViewModel {

    private let catalog: CatalogRepository
    private var query: String = ""

    var hasQuery: Bool { !query.isEmpty }

    init(catalog: CatalogRepository, adRepository: RetailMediaAdRepository, cart: CartRepository) {
        self.catalog = catalog
        super.init(cart: cart, adRepository: adRepository, initialPhase: .idle)
    }

    func search(_ text: String) {
        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        query = trimmed
        refresh()
    }

    override func fetch(page: Int, pageSize: Int) async throws -> CatalogPage {
        try await catalog.search(text: query, page: page, pageSize: pageSize)
    }
}
