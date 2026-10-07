import Foundation

// [Step 2] [Step 3]
/// Display ads: asks the backend for the screen's ad slot and loads it through the SDK. Ads are best-effort, so
/// no failure leaks out; only cancellation propagates.
final class DisplayAdsLoader {

    private let adsRepository: AdsRepository
    private let adRepository: RetailMediaAdRepository

    init(adsRepository: AdsRepository, adRepository: RetailMediaAdRepository) {
        self.adsRepository = adsRepository
        self.adRepository = adRepository
    }

    func load(_ screen: AdScreen) async throws -> RetailMediaAds? {
        try await bestEffort {
            guard let slot = try await adsRepository.screenAd(screen) else { return nil }
            return try await adRepository.loadDisplayAds(slot: slot)
        }
    }
}
