import UIKit
@_spi(RetailMedia) import YandexMobileAds

/// The asset views of a display creative; subclasses only lay them out.
class DisplayAdView: RetailMediaAdView {

    let media = NativeMediaView()
    let title: UILabel
    let body: UILabel
    let warning: UILabel
    let sponsored: UILabel
    /// A custom button: the SDK sets its own feedback image, and a system button would tint it into a solid circle.
    let feedback = UIButton(type: .custom)
    private var mediaRatio: NSLayoutConstraint?

    init(title: UILabel, body: UILabel, warning: UILabel, sponsored: UILabel) {
        self.title = title
        self.body = body
        self.warning = warning
        self.sponsored = sponsored
        super.init(frame: .zero)
        media.translatesAutoresizingMaskIntoConstraints = false
        feedback.accessibilityLabel = Strings.adFeedback
        feedback.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            feedback.widthAnchor.constraint(equalToConstant: 24),
            feedback.heightAnchor.constraint(equalToConstant: 24),
        ])
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    // [Step 4]
    /// The set of asset views is dictated by the creative, not by the layout: a display creative fails to bind
    /// without its warning view, so every slot is provided. Only the views of missing assets are hidden — the SDK
    /// treats a hidden asset view as invisible and drops the impression. `NativeMediaView` draws with aspect fill,
    /// so it takes the creative's own ratio to show it uncropped.
    func bind(_ ad: RetailMediaAd, using binder: RetailMediaAdBinder) throws {
        let assets = ad.adAssets()
        media.isHidden = assets.media == nil
        title.isHidden = assets.title == nil
        body.isHidden = assets.body == nil
        warning.isHidden = assets.warning == nil
        sponsored.isHidden = assets.sponsored == nil

        mediaRatio?.isActive = false
        mediaRatio = media.heightAnchor.constraint(equalTo: media.widthAnchor, multiplier: 1 / ad.mediaAspectRatio)
        mediaRatio?.isActive = true

        mediaView = media
        titleLabel = title
        bodyLabel = body
        warningLabel = warning
        sponsoredLabel = sponsored
        feedbackButton = feedback
        try binder.bind(ad, to: self)
    }
}

extension RetailMediaAd {
    /// 16:9 when the creative does not report its ratio.
    var mediaAspectRatio: CGFloat {
        let ratio = adAssets().media?.aspectRatio ?? 0
        return ratio > 0 ? ratio : 16 / 9
    }
}
