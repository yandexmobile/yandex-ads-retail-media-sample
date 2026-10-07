import Foundation

struct ProductsContent: Equatable {
    let items: [FeedItem]
    let appending: Bool
    let hasMore: Bool
}

enum ProductsUiState: Equatable {
    case idle
    case loading
    case empty
    case error
    case content(ProductsContent)
}

enum ProductsPhase {
    case idle
    case loading
    case error
    case content(appending: Bool, hasMore: Bool)
}

enum ProductsEvent {
    case cartActionFailed
}
