# RetailMedia Sample — Backend

Local serving backend for the RetailMedia integration sample store.

**Stack:** Kotlin (JVM), Ktor. Runs locally only; there is no database — categories and products
are hardcoded, and the shopping cart is kept in memory.

## Run

```bash
./gradlew run
```

The server listens on `http://127.0.0.1:8080` (host and port are set in
`src/main/resources/application.yaml`).

## Endpoints

### Catalog

Ad-bearing catalog handles are **POST** — the body carries the client's `platform`, `bidderToken`
and ad session fields, which the backend forwards to the RetailMedia s2s API and merges into the
product list.

- `GET /` — health check
- `POST /home` — recommendation feed; body `{ page, pageSize, platform, bidderToken?, adSessionId?, adSessionHitNumber? }`
- `GET /categories` — category list (no ad context; display ads for this screen are via `POST /ads`)
- `POST /categories/{id}/products` — products in a category; body same as `/home`
- `POST /search` — product search; body `{ text, page, pageSize, platform, bidderToken?, adSessionId?, adSessionHitNumber? }`

`platform` is `"android"` or `"ios"`: ad units are issued per app, so the same screen is a
different placement in each build. The field is required: a request without it, or with any other
value, is rejected with `400 bad_request`.

Catalog responses are `CatalogResponse { products: Page<Product>, adSlot? }`.
`Page<Product>` has shape `{ items, page, pageSize, hasMore }` (`page` is 1-based).
Sponsored products have a `sponsored: true` flag.

### Ads

- `POST /ads` — dedicated display-ad slot for a screen; body `{ screen: "home"|"categories"|"cart", platform, bidderToken?, adSessionId?, adSessionHitNumber? }`; returns `AdSlot` or `204 No Content` when no ad is available

Home appears here as well as in the catalog handles: it is the one screen with both formats at
once — sponsored products come merged into `POST /home`, its display banner comes from its own ad
unit through `/ads`. Screens without a display ad unit (`category`, `search`) are rejected with
`400`.

### Feed

- `GET /feed` — YML product catalog (`Content-Type: application/xml`), used by the RetailMedia platform to sync inventory

### Cart (in-memory)

- `GET /cart`
- `POST /cart/items` — body `{ "productId": "...", "quantity": 1 }`
- `DELETE /cart/items/{id}`
- `POST /cart/checkout` — places the order and clears the cart

Errors are `{ "code", "message" }` with a matching HTTP status.

## RetailMedia configuration

A placement is addressed by three things — **platform**, **screen** and ad **format** — because the
partner interface issues its own page and ad unit per app and per format:

| Screen | `productPromo` | `display` |
|---|:---:|:---:|
| `home` | ✓ | ✓ |
| `category` | ✓ | — |
| `search` | ✓ | — |
| `categories` | — | ✓ |
| `cart` | — | ✓ |

Each slot carries three identifiers, and they do different jobs:

| Field | What it is |
|---|---|
| `pageId` | Addresses the s2s request: `POST /retail/{page-id}` |
| `impId` | The **number** of the block inside that page, sent as `imp-id`. RM parses it as an integer and rejects anything else with `400 Invalid imp-id` |
| `adUnitId` | The block id as shown in the partner interface, `R-M-<pageId>-<impId>`. RM never sees it — it is returned to the client as `AdSlot.adUnitId` for `RetailMediaLoader.loadAd` |

So `R-M-19820261-1` is page `19820261` plus block number `1`; do not send it as `imp-id`.

The sample ships with working ad units of its own in
`src/main/resources/application.yaml`, so ads show up out of the box. Point it at your own inventory
with environment variables:

| Variable | Purpose |
|---|---|
| `RM_BASE_URL` | RM s2s base URL (defaults to `https://yandex.ru/retail`) |
| `RM_RETAIL_FEED_ID` | Feed ID for inventory sync |
| `RM_<PLATFORM>_<SCREEN>_<FORMAT>_PAGE_ID` | Page of one slot, e.g. `RM_ANDROID_HOME_DISPLAY_PAGE_ID` |
| `RM_<PLATFORM>_<SCREEN>_<FORMAT>_IMP_ID` | Block number in that page, e.g. `RM_ANDROID_HOME_DISPLAY_IMP_ID` |
| `RM_<PLATFORM>_<SCREEN>_<FORMAT>_AD_UNIT_ID` | Ad unit for the client, e.g. `RM_ANDROID_HOME_DISPLAY_AD_UNIT_ID` |

`<PLATFORM>` is `ANDROID` or `IOS`, `<SCREEN>` is one of the screens above, `<FORMAT>` is
`PRODUCT_PROMO` or `DISPLAY`.

When a slot's `pageId`/`impId` are blank, or `RM_RETAIL_FEED_ID` is unset, or the client sends no
`bidderToken`, the backend returns catalog results without any sponsored products
(`adSlot` is omitted) and `/ads` answers `204`. RM requires `retail-feed-id` in every request, so
without a feed the backend does not call RM at all.

Ads are best-effort: if RM is slow, unreachable or rejects the request, the store still serves its
catalog, cart and search — the shopper just sees no ads, and the reason is logged. Calls to RM are
bounded by a timeout, and the `out_of_stock` notifications the response does not depend on are sent
in the background rather than in front of the catalog.

### Tracing ad requests

Every ad request is traced at `DEBUG`, because a screen without ads looks the same whether RM had
nothing to serve, refused the request, or answered fine and the store failed to read it:

```text
RM → POST https://yandex.ru/retail/19820261 imp-id=1 retail-feed-id=… ad-session-id=… bidder-token=CgkKBxIF… <42 chars>
RM ← 400 Bad request page=19820261 content-type=none body=Bad Request: Invalid bidder-token
categories: skipping RM, the client sent no bidder token
home: skipping RM, no PRODUCT_PROMO placement is configured for platform 'android'
```

The bidder token is logged as a length and a short head only: it carries the device's advertising
identifiers, user agent and IP, which do not belong in a log. Turn the tracing down by setting the
`com.yandex.retailmedia.sample.retailmedia` logger to `INFO` in `src/main/resources/logback.xml`.
