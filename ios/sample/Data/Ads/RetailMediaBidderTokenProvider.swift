import Foundation
@_spi(RetailMedia) import YandexMobileAds

// [Step 1]
/// The SDK token that lets RM run its auction for this device. It is requested anew for every ad request and
/// travels in the POST body: it can be several kilobytes, too long for a header or a query string.
/// `nil` on failure: the request still goes out and the backend answers without ads.
final class RetailMediaBidderTokenProvider {

    private let loader = BidderTokenLoader()

    func bidderToken() async -> String? {
        await loader.loadBidderToken(request: .retailMedia())
    }
}
