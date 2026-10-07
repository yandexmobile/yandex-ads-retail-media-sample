import Foundation
import Observation

enum CartPhase {
    case loading
    case error
    case loaded
}

@Observable
final class CartViewModel {

    private(set) var phase: CartPhase = .loading
    let displayAds: ScreenDisplayAds

    @ObservationIgnored var onEvent: ((CartEvent) -> Void)?
    @ObservationIgnored private let cart: CartRepository
    @ObservationIgnored private var busy = false

    var state: CartUiState {
        switch phase {
        case .loading:
            return .loading
        case .error:
            return .error
        case .loaded:
            let current = cart.cart
            return current.items.isEmpty ? .empty : .content(items: current.items, total: current.total)
        }
    }

    init(cart: CartRepository, displayAds: ScreenDisplayAds) {
        self.cart = cart
        self.displayAds = displayAds
        refresh()
    }

    func refresh() {
        phase = .loading
        displayAds.reload()
        Task {
            do {
                _ = try await cart.get()
                phase = .loaded
            } catch {
                phase = .error
            }
        }
    }

    func increment(_ item: CartItem) {
        guard !busy else { return }
        busy = true
        Task {
            do {
                _ = try await cart.add(productId: item.id, quantity: 1)
            } catch {
                onEvent?(.actionFailed)
            }
            busy = false
        }
    }

    func decrement(_ item: CartItem) {
        guard !busy else { return }
        busy = true
        Task {
            do {
                let quantity = cart.cart.items.first(where: { $0.id == item.id })?.quantity ?? 0
                _ = try await cart.setQuantity(productId: item.id, quantity: quantity - 1)
            } catch {
                onEvent?(.actionFailed)
            }
            busy = false
        }
    }

    func remove(_ item: CartItem) {
        guard !busy else { return }
        busy = true
        Task {
            do {
                _ = try await cart.remove(productId: item.id)
            } catch {
                onEvent?(.actionFailed)
            }
            busy = false
        }
    }

    func checkout() {
        guard !busy else { return }
        busy = true
        Task {
            do {
                let result = try await cart.checkout()
                if result.ok {
                    onEvent?(.checkedOut)
                } else {
                    onEvent?(.actionFailed)
                }
            } catch {
                onEvent?(.actionFailed)
            }
            busy = false
        }
    }
}
