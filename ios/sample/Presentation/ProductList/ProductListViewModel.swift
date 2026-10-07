import Foundation

final class ProductListViewModel: ProductsViewModel {

    let categoryName: String

    private let catalog: CatalogRepository
    private let categoryId: String

    init(
        categoryId: String,
        categoryName: String,
        catalog: CatalogRepository,
        adRepository: RetailMediaAdRepository,
        cart: CartRepository
    ) {
        self.categoryId = categoryId
        self.categoryName = categoryName
        self.catalog = catalog
        super.init(cart: cart, adRepository: adRepository)
        refresh()
    }

    override func fetch(page: Int, pageSize: Int) async throws -> CatalogPage {
        try await catalog.productsInCategory(categoryId: categoryId, page: page, pageSize: pageSize)
    }
}
