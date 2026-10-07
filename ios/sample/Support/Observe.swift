import Observation

@MainActor
func observeChanges(_ render: @escaping () -> Void) {
    withObservationTracking(render) {
        Task { @MainActor in observeChanges(render) }
    }
}
