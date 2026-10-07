import Foundation

protocol CatalogRepository {
    func home(page: Int, pageSize: Int) async throws -> CatalogPage
    func categories() async throws -> [Category]
    func productsInCategory(categoryId: String, page: Int, pageSize: Int) async throws -> CatalogPage
    func search(text: String, page: Int, pageSize: Int) async throws -> CatalogPage
}

// [Step 2]
/// productPromo comes with the catalog itself, so the catalog requests carry the SDK `bidderToken` and the ad
/// context. The backend returns sponsored products inside the page along with an `AdSlot` for the SDK.
final class CatalogRepositoryImpl: CatalogRepository {

    private let api: StoreAPI
    private let bidderTokenProvider: RetailMediaBidderTokenProvider
    private let session: AdSessionTracker

    init(api: StoreAPI, bidderTokenProvider: RetailMediaBidderTokenProvider, session: AdSessionTracker) {
        self.api = api
        self.bidderTokenProvider = bidderTokenProvider
        self.session = session
    }

    func home(page: Int, pageSize: Int) async throws -> CatalogPage {
        try await api.home(catalogRequest(page: page, pageSize: pageSize)).toDomain()
    }

    func categories() async throws -> [Category] {
        try await api.categories().map { $0.toDomain() }
    }

    func productsInCategory(categoryId: String, page: Int, pageSize: Int) async throws -> CatalogPage {
        try await api.productsInCategory(
            id: categoryId,
            body: catalogRequest(page: page, pageSize: pageSize)
        ).toDomain()
    }

    func search(text: String, page: Int, pageSize: Int) async throws -> CatalogPage {
        let body = SearchAdRequestDTO(
            text: text,
            page: page,
            pageSize: pageSize,
            platform: adRequestPlatform,
            bidderToken: await bidderTokenProvider.bidderToken(),
            adSessionId: session.sessionId,
            adSessionHitNumber: session.nextHitNumber()
        )
        return try await api.search(body).toDomain()
    }

    private func catalogRequest(page: Int, pageSize: Int) async -> CatalogAdRequestDTO {
        CatalogAdRequestDTO(
            page: page,
            pageSize: pageSize,
            platform: adRequestPlatform,
            bidderToken: await bidderTokenProvider.bidderToken(),
            adSessionId: session.sessionId,
            adSessionHitNumber: session.nextHitNumber()
        )
    }
}
