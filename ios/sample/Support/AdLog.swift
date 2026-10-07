import OSLog

private let logger = Logger(subsystem: "com.yandex.retailmedia.sample", category: "RetailMedia")

func logAdBindingFailure(_ place: String, _ error: Error) {
    logger.error("\(place, privacy: .public) binding failed: \(error.localizedDescription, privacy: .public)")
}
