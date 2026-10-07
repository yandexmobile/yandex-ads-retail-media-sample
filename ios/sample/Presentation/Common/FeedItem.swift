import Foundation

enum FeedItem: Equatable {
    case product(ProductRow)
    case adSlider(RetailMediaAds)

    /// Identity of a card in the grid. The ad is part of it: a card that gains or loses an ad is a different card,
    /// with its own reuse identifier.
    nonisolated enum ID: Hashable, Sendable {
        case product(id: String, ad: ObjectIdentifier?)
        case adSlider(ObjectIdentifier)
    }

    var id: ID {
        switch self {
        case .product(let row):
            return .product(id: row.product.id, ad: row.sponsoredAd.map { ObjectIdentifier($0.ad) })
        case .adSlider(let ads):
            return .adSlider(ObjectIdentifier(ads.retailMedia as AnyObject))
        }
    }

    static func == (lhs: FeedItem, rhs: FeedItem) -> Bool {
        switch (lhs, rhs) {
        case let (.product(left), .product(right)):
            return left == right
        case let (.adSlider(left), .adSlider(right)):
            return isSameRetailMedia(left.retailMedia, right.retailMedia)
        case (.product, _), (.adSlider, _):
            return false
        }
    }
}
