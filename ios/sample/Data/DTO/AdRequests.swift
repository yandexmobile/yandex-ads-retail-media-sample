import Foundation

/// The backend keeps one ad unit per platform and screen, picks it by this field and rejects requests without it.
let adRequestPlatform = "ios"

struct CatalogAdRequestDTO: Encodable {
    let page: Int
    let pageSize: Int
    let platform: String
    let bidderToken: String?
    let adSessionId: String
    let adSessionHitNumber: Int
}

struct SearchAdRequestDTO: Encodable {
    let text: String
    let page: Int
    let pageSize: Int
    let platform: String
    let bidderToken: String?
    let adSessionId: String
    let adSessionHitNumber: Int
}

struct AdsRequestDTO: Encodable {
    let screen: String
    let platform: String
    let bidderToken: String?
    let adSessionId: String
    let adSessionHitNumber: Int
}
