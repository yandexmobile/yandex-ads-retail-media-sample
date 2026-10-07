import Foundation

struct Product: Equatable {
    let id: String
    let name: String
    let price: Double
    let oldPrice: Double?
    let currencyId: String
    let picture: String
    let categoryId: String
    let url: String
    let available: Bool
    let vendor: String?
    let sponsored: Bool
}
