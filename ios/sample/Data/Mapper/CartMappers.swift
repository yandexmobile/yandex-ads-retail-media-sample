import Foundation

extension CartItemDTO {
    func toDomain() -> CartItem {
        CartItem(id: id, name: name, price: price, picture: picture, quantity: quantity)
    }
}

extension CartDTO {
    func toDomain() -> Cart {
        Cart(items: items.map { $0.toDomain() })
    }
}

extension CheckoutResultDTO {
    func toDomain() -> CheckoutResult {
        CheckoutResult(ok: ok, message: message)
    }
}
