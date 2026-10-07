import Foundation
@_spi(RetailMedia) import YandexMobileAds

/// One `loadAd` result. `retailMedia` is kept next to its ads because binding needs it: the slot is bound to a
/// container first, and the returned binder then binds each ad to its own view.
struct RetailMediaAds {
    let retailMedia: RetailMedia
    let ads: [RetailMediaAd]
}

struct SponsoredAd {
    let offerId: String
    let ad: RetailMediaAd
    let retailMedia: RetailMedia
}

/// `RetailMedia` is not class-bound, so its identity is compared through `AnyObject`.
func isSameRetailMedia(_ lhs: RetailMedia?, _ rhs: RetailMedia?) -> Bool {
    lhs as AnyObject? === rhs as AnyObject?
}
