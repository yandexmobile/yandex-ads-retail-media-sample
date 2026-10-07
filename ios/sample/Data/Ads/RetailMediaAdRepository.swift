import Foundation
@_spi(RetailMedia) import YandexMobileAds

// [Step 3]
/// `RetailMediaLoader.loadAd` turns the response the backend got from RM into SDK ads. One `readyResponse` yields
/// one `RetailMedia` whose `ads` may hold several creatives. `adType` separates the formats: `productPromo` is
/// bound into a product card, everything else is a display ad.
final class RetailMediaAdRepository {

    private let loader = RetailMediaLoader()

    /// `offerIds` is a filter, not an order: the SDK keeps only the ads for these offers and throws no-fill when
    /// none of them matches.
    func loadProductPromo(slot: AdSlot, offerIds: [String]) async throws -> [SponsoredAd] {
        guard !offerIds.isEmpty else { return [] }
        let retailMedia = try await loader.loadAd(
            adUnitID: slot.adUnitId,
            with: slot.readyResponse,
            offerIDs: offerIds
        )
        return retailMedia.ads.compactMap { ad in
            guard ad.adType == .productPromo, let offerId = ad.offerId else { return nil }
            return SponsoredAd(offerId: offerId, ad: ad, retailMedia: retailMedia)
        }
    }

    func loadDisplayAds(slot: AdSlot) async throws -> RetailMediaAds? {
        let retailMedia = try await loader.loadAd(adUnitID: slot.adUnitId, with: slot.readyResponse)
        let ads = retailMedia.ads.filter { $0.adType != .productPromo }
        guard !ads.isEmpty else { return nil }
        return RetailMediaAds(retailMedia: retailMedia, ads: ads)
    }
}

private extension RetailMediaAd {
    var offerId: String? {
        adInfo?.creatives.lazy.compactMap(\.offerID).first
    }
}
