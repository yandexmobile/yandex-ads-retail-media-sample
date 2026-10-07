import Foundation

struct ProductRow: Equatable {
    let product: Product
    let quantityInCart: Int
    let sponsoredAd: SponsoredAd?

    static func == (lhs: ProductRow, rhs: ProductRow) -> Bool {
        lhs.product == rhs.product
            && lhs.quantityInCart == rhs.quantityInCart
            && lhs.sponsoredAd?.ad === rhs.sponsoredAd?.ad
    }
}

/// Ads are matched to cards by `offerId`, never by position: the SDK returns them in `readyResponse` order,
/// which differs from the page order. Only `sponsored` products get an ad, the first one per offer; an ad
/// without a matching card is dropped so that no impression is reported for an offer the user never saw.
func buildProductRows(
    products: [Product],
    quantities: [String: Int],
    sponsoredAds: [SponsoredAd]
) -> [ProductRow] {
    var adsByOfferId: [String: SponsoredAd] = [:]
    for sponsoredAd in sponsoredAds where adsByOfferId[sponsoredAd.offerId] == nil {
        adsByOfferId[sponsoredAd.offerId] = sponsoredAd
    }
    return products.map { product in
        ProductRow(
            product: product,
            quantityInCart: quantities[product.id] ?? 0,
            sponsoredAd: product.sponsored ? adsByOfferId[product.id] : nil
        )
    }
}
