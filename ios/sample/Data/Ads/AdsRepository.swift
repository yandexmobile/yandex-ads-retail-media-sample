import Foundation

enum AdScreen: String {
    case home
    case categories
    case cart
}

// [Step 2]
/// Requests a display ad slot from our backend. The body carries the SDK `bidderToken` and the ad context, which
/// the backend uses to find the ad unit and call RM s2s. 204 means "no ads", a normal answer rather than an error.
final class AdsRepository {

    private let api: StoreAPI
    private let bidderTokenProvider: RetailMediaBidderTokenProvider
    private let session: AdSessionTracker

    init(api: StoreAPI, bidderTokenProvider: RetailMediaBidderTokenProvider, session: AdSessionTracker) {
        self.api = api
        self.bidderTokenProvider = bidderTokenProvider
        self.session = session
    }

    func screenAd(_ screen: AdScreen) async throws -> AdSlot? {
        let body = AdsRequestDTO(
            screen: screen.rawValue,
            platform: adRequestPlatform,
            bidderToken: await bidderTokenProvider.bidderToken(),
            adSessionId: session.sessionId,
            adSessionHitNumber: session.nextHitNumber()
        )
        return try await api.ads(body)?.toDomain()
    }
}
