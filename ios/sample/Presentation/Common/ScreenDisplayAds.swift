import Foundation
import Observation

/// The display ads of one screen. `reload()` runs on every screen refresh rather than once per view model: `/ads`
/// runs an auction per request, and the view model outlives the screen, so a creative loaded once would stay for
/// the whole visit. The request still in flight is cancelled and the ads cleared first, so with two quick
/// refreshes the slower auction never wins the screen.
@Observable
final class ScreenDisplayAds {

    private(set) var ads: RetailMediaAds?

    @ObservationIgnored private let screen: AdScreen
    @ObservationIgnored private let loader: DisplayAdsLoader
    @ObservationIgnored private var task: Task<Void, Never>?

    init(screen: AdScreen, loader: DisplayAdsLoader) {
        self.screen = screen
        self.loader = loader
    }

    func reload() {
        task?.cancel()
        ads = nil
        task = Task {
            guard let loaded = try? await loader.load(screen) else { return }
            ads = loaded
        }
    }
}
