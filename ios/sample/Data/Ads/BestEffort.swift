import Foundation

/// Ads must never break the store, so any failure turns into `nil`. Cancellation is the exception: a cancelled
/// ad request rethrows, whatever error the cancelled call surfaced (`URLError.cancelled` included), so a stale
/// result never overwrites the state of a newer request.
func bestEffort<T>(_ operation: () async throws -> T?) async throws -> T? {
    let value = try? await operation()
    try Task.checkCancellation()
    return value
}
