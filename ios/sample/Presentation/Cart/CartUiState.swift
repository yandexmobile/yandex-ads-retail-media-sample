import Foundation

enum CartUiState: Equatable {
    case loading
    case empty
    case error
    case content(items: [CartItem], total: Double)
}

enum CartEvent {
    case checkedOut
    case actionFailed
}
