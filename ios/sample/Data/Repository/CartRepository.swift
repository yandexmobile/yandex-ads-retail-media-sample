import Foundation
import Observation

protocol CartRepository {
    var cart: Cart { get }
    func get() async throws -> Cart
    func add(productId: String, quantity: Int) async throws -> Cart
    func remove(productId: String) async throws -> Cart
    func setQuantity(productId: String, quantity: Int) async throws -> Cart
    func checkout() async throws -> CheckoutResult
}

@Observable
final class CartRepositoryImpl: CartRepository {

    private(set) var cart: Cart = .empty

    @ObservationIgnored private let api: StoreAPI

    init(api: StoreAPI) {
        self.api = api
    }

    func get() async throws -> Cart {
        let result = try await api.cart().toDomain()
        cart = result
        return result
    }

    func add(productId: String, quantity: Int) async throws -> Cart {
        let result = try await api.addToCart(AddToCartRequestDTO(productId: productId, quantity: quantity)).toDomain()
        cart = result
        return result
    }

    func remove(productId: String) async throws -> Cart {
        let result = try await api.removeFromCart(id: productId).toDomain()
        cart = result
        return result
    }

    func setQuantity(productId: String, quantity: Int) async throws -> Cart {
        let afterRemove = try await api.removeFromCart(id: productId).toDomain()
        let result: Cart
        if quantity > 0 {
            do {
                result = try await api.addToCart(
                    AddToCartRequestDTO(productId: productId, quantity: quantity)
                ).toDomain()
            } catch {
                cart = afterRemove
                throw error
            }
        } else {
            result = afterRemove
        }
        cart = result
        return result
    }

    func checkout() async throws -> CheckoutResult {
        let result = try await api.checkout().toDomain()
        if result.ok {
            cart = .empty
        }
        return result
    }
}
