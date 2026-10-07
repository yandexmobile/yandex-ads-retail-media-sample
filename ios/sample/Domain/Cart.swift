import Foundation

struct CartItem: Equatable {
    let id: String
    let name: String
    let price: Double
    let picture: String
    let quantity: Int
}

struct Cart: Equatable {
    let items: [CartItem]

    var total: Double {
        items.reduce(0) { $0 + $1.price * Double($1.quantity) }
    }

    static let empty = Cart(items: [])
}

struct CheckoutResult: Equatable {
    let ok: Bool
    let message: String
}
