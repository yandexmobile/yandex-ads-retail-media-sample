import Foundation

struct CatalogResponseDTO: Decodable {
    let products: PageDTO<ProductDTO>
    let adSlot: AdSlotDTO?
}
