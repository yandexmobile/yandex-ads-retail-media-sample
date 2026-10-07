# Yandex RetailMedia Integration Sample

A reference e-commerce ("ECOM") application that demonstrates a **complete and correct
integration of Yandex RetailMedia** into a real online-store flow — across an Android app,
an iOS app, and a serving backend.

## Why this sample exists

RetailMedia is an advertising product for retailers. Unlike ordinary in-app ads, a correct
integration is noticeably harder, and the tricky parts are exactly the ones that are hard to
convey with isolated snippets:

- the retailer's **own backend must take part** in serving ads;
- integration often requires **changes to existing catalog endpoints**, not just a new one;
- promoted products (`productPromo`) must be **rendered on top of the store's existing
  product cards**, not as a separate ad block;
- native-ad specifics (such as ad-container binding) are easy to get wrong.

The official documentation covers each step, but the listings are out of context. This
repository shows the whole picture: a working store you can run locally, with every
integration point annotated in the code.

## What's inside

| Component | Path | Stack |
| --- | --- | --- |
| Serving backend | [`backend/`](./backend) | Kotlin (JVM), Ktor |
| Android app | [`android/`](./android) | Kotlin, Android View, Coroutines, Retrofit + OkHttp, Hilt, Jetpack Navigation |
| iOS app | [`ios/`](./ios) | Swift, Swift Concurrency, Foundation, Codable |

## Demo scenarios

The sample store implements the everyday e-commerce flow, and RetailMedia promotions appear
naturally inside it:

- **Home** — a feed of natural recommendations;
- **Categories** — categories and product listings within a category;
- **Cart**;
- **Search** — a simple product search;
- **Feed export** — backend only, for uploading the product feed.

Product lists are paginated and support swipe-to-refresh.

## Running locally

Everything runs on `localhost`. The mobile apps talk **only** to a locally running backend.

To run the sample:

1. Start the backend locally (see [`backend/`](./backend)).
2. Run the Android or iOS app against `localhost` (see [`android/`](./android) /
   [`ios/`](./ios)).

## Where the integration lives

Every RetailMedia touch point in the code is annotated with **KDoc** (Android, backend) and
**SwiftDoc** (iOS) comments that explain _why_ each step is needed. Each of them is tagged
`// [Step N]`, so searching for `[Step` jumps straight to the integration points:

1. `[Step 1]` — the app gets a bidder token from the SDK;
2. `[Step 2]` — the app requests ads from the retailer's backend: catalog endpoints for
   `productPromo`, `/ads` for display ads;
   - `[Step 2.1]` — the backend calls Yandex RetailMedia server-to-server;
   - `[Step 2.2]` — the backend merges promoted products into the catalog response;
   - `[Step 2.3]` — the backend relays the display ad response as is;
3. `[Step 3]` — the app loads the ads into the SDK with `RetailMediaLoader.loadAd`;
4. `[Step 4]` — the app binds the ads to its views;
5. `[Step 5]` — the app tracks clicks on custom assets.

## Documentation

Official Yandex RetailMedia documentation:

- Mobile app integration (server-to-server):
  [s2s-integration-mobile](https://yandex.ru/support/retailmedia/ru/s2s-integration-mobile)
- Product feed export — quickstart:
  [quickstart](https://yandex.ru/support/retailmedia/ru/quickstart)
- Product feed (YML) requirements:
  [feeds/requirements-yml](https://yandex.ru/support/direct/ru/feeds/requirements-yml)

## License

The sample is distributed under the Yandex Mobile Ads SDK EULA:
[legal.yandex.com/partner_ch](https://legal.yandex.com/partner_ch/). See also
[`LICENSE.txt`](./LICENSE.txt).
