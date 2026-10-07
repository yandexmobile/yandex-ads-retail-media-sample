# RetailMedia Sample — iOS

iOS e-commerce sample app demonstrating Yandex RetailMedia integration.

**Stack:** Swift, Swift Concurrency, UIKit, Foundation, Codable. Single-module architecture with
Presentation (MVVM) and Data layers; talks only to a local backend at `http://localhost:8080`.

## Yandex Mobile Ads SDK

The SDK comes from Swift Package Manager:
`https://github.com/yandexmobile/yandex-ads-sdk-ios`, product `YandexMobileAds`. The RetailMedia API
is imported with `@_spi(RetailMedia) import YandexMobileAds`.

Two integration steps the SDK needs:

- `-ObjC` in `OTHER_LDFLAGS` of the app target;
- `zq492l623r.skadnetwork` in `SKAdNetworkItems` of `Info.plist`.

## Running

1. Start the backend locally (see [`../backend`](../backend)).
2. Open `sample.xcodeproj` in Xcode and run the `sample` scheme on an iOS simulator.

## Where the integration lives

Search the code for `[Step` — see the root [`README.md`](../README.md#where-the-integration-lives).
