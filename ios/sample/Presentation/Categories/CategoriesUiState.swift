import Foundation

enum CategoriesUiState: Equatable {
    case loading
    case empty
    case error
    case content([Category])
}
