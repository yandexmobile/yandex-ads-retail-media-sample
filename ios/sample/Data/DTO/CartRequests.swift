import Foundation

struct AddToCartRequestDTO: Encodable {
    let productId: String
    let quantity: Int
}

struct CheckoutResultDTO: Decodable {
    let ok: Bool
    let message: String
}
