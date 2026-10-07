import Foundation

/// Client-owned ad session for RetailMedia: `sessionId` identifies a continuous browsing session and
/// `nextHitNumber()` counts ad requests within it. Both go into POST bodies and the backend forwards them to RM.
final class AdSessionTracker {

    let sessionId: String
    private var hitNumber = 0

    init(sessionId: String = UUID().uuidString) {
        self.sessionId = sessionId
    }

    func nextHitNumber() -> Int {
        hitNumber += 1
        return hitNumber
    }
}
