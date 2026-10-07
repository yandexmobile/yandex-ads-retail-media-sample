import Foundation

struct CartItemDTO: Decodable {
    let id: String
    let name: String
    let price: Double
    let picture: String
    let quantity: Int
}

struct CartDTO: Decodable {
    let items: [CartItemDTO]
}
