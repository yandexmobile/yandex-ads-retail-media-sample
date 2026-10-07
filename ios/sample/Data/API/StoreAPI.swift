import Foundation

protocol StoreAPI {
    func home(_ body: CatalogAdRequestDTO) async throws -> CatalogResponseDTO
    func categories() async throws -> [CategoryDTO]
    func productsInCategory(id: String, body: CatalogAdRequestDTO) async throws -> CatalogResponseDTO
    func search(_ body: SearchAdRequestDTO) async throws -> CatalogResponseDTO
    func ads(_ body: AdsRequestDTO) async throws -> AdSlotDTO?
    func cart() async throws -> CartDTO
    func addToCart(_ body: AddToCartRequestDTO) async throws -> CartDTO
    func removeFromCart(id: String) async throws -> CartDTO
    func checkout() async throws -> CheckoutResultDTO
}
