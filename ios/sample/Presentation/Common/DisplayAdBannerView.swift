import UIKit
@_spi(RetailMedia) import YandexMobileAds

/// The display banner slot of Categories and Cart; collapsed while there is no ad.
///
/// Every creative gets a fresh banner view, and the old one leaves the hierarchy instead of being hidden: the SDK
/// cannot unbind an ad, keeps checking the visibility of the ad's views, and outside a cell reports a hidden
/// ancestor as an integration error.
final class DisplayAdBannerView: UIView {

    private var banner: DisplayAdBanner?

    override init(frame: CGRect) {
        super.init(frame: frame)
        isHidden = true
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func show(_ ads: RetailMediaAds?) {
        guard !isSameRetailMedia(ads?.retailMedia, banner?.retailMedia) else { return }
        banner?.removeFromSuperview()
        banner = nil
        guard let ads, let ad = ads.ads.first else {
            isHidden = true
            return
        }
        let banner = DisplayAdBanner(retailMedia: ads.retailMedia)
        banner.translatesAutoresizingMaskIntoConstraints = false
        addSubview(banner)
        NSLayoutConstraint.activate([
            banner.topAnchor.constraint(equalTo: topAnchor),
            banner.leadingAnchor.constraint(equalTo: leadingAnchor),
            banner.trailingAnchor.constraint(equalTo: trailingAnchor),
            banner.bottomAnchor.constraint(equalTo: bottomAnchor),
        ])
        isHidden = false
        do {
            try banner.bind(ad)
            self.banner = banner
        } catch {
            banner.removeFromSuperview()
            isHidden = true
            logAdBindingFailure("Banner", error)
        }
    }
}

private final class DisplayAdBanner: RetailMediaAdView {

    let retailMedia: RetailMedia
    private let content = DisplayAdBannerContentView()

    init(retailMedia: RetailMedia) {
        self.retailMedia = retailMedia
        super.init(frame: .zero)
        content.translatesAutoresizingMaskIntoConstraints = false
        addSubview(content)
        NSLayoutConstraint.activate([
            content.topAnchor.constraint(equalTo: topAnchor),
            content.leadingAnchor.constraint(equalTo: leadingAnchor),
            content.trailingAnchor.constraint(equalTo: trailingAnchor),
            content.bottomAnchor.constraint(equalTo: bottomAnchor),
        ])
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    // [Step 4]
    /// The slot is bound to the banner and the first creative to its content; these screens have room for one.
    func bind(_ ad: RetailMediaAd) throws {
        let binder = try retailMedia.bind(to: self)
        try content.bind(ad, using: binder)
    }
}

private final class DisplayAdBannerContentView: DisplayAdView {

    private static let mediaWidth: CGFloat = 96

    init() {
        super.init(title: UILabel(), body: UILabel(), warning: UILabel(), sponsored: UILabel())
        setUp()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private func setUp() {
        backgroundColor = UIColor(white: 0xEE / 255, alpha: 1)

        title.font = .systemFont(ofSize: 15, weight: .semibold)
        title.numberOfLines = 2
        body.font = .systemFont(ofSize: 13)
        body.numberOfLines = 2
        for label in [sponsored, warning] {
            label.font = .systemFont(ofSize: 11)
            label.textColor = .secondaryLabel
            label.numberOfLines = 2
        }

        let texts = UIStackView(arrangedSubviews: [title, body, sponsored, warning])
        texts.axis = .vertical
        texts.spacing = 2

        media.layer.cornerRadius = 8
        media.clipsToBounds = true

        let row = UIStackView(arrangedSubviews: [media, texts])
        row.axis = .horizontal
        row.alignment = .top
        row.spacing = 12
        row.translatesAutoresizingMaskIntoConstraints = false

        addSubview(row)
        addSubview(feedback)

        let mediaWidth = media.widthAnchor.constraint(equalToConstant: Self.mediaWidth)
        mediaWidth.priority = .required - 1

        NSLayoutConstraint.activate([
            mediaWidth,

            row.topAnchor.constraint(equalTo: topAnchor, constant: 8),
            row.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 12),
            row.trailingAnchor.constraint(equalTo: feedback.leadingAnchor, constant: -8),
            row.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -8),

            feedback.topAnchor.constraint(equalTo: topAnchor, constant: 8),
            feedback.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -12),
        ])
    }
}
