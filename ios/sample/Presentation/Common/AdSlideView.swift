import UIKit

final class AdSlideView: DisplayAdView {

    init() {
        super.init(
            title: Self.capsule(font: .systemFont(ofSize: 15, weight: .semibold), numberOfLines: 2),
            body: Self.capsule(font: .systemFont(ofSize: 13), numberOfLines: 2),
            warning: Self.capsule(font: .systemFont(ofSize: 11), numberOfLines: 2),
            sponsored: Self.capsule(font: .systemFont(ofSize: 11, weight: .medium), numberOfLines: 1)
        )
        setUp()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    private static func capsule(font: UIFont, numberOfLines: Int) -> UILabel {
        let label = PaddingLabel()
        label.font = font
        label.numberOfLines = numberOfLines
        label.textColor = UIColor(white: 0x21 / 255, alpha: 1)
        label.backgroundColor = UIColor.white.withAlphaComponent(0.82)
        label.textInsets = UIEdgeInsets(top: 5, left: 9, bottom: 5, right: 9)
        label.layer.cornerRadius = 12
        label.layer.masksToBounds = true
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }

    private func setUp() {
        layer.cornerRadius = 12
        clipsToBounds = true
        backgroundColor = .secondarySystemBackground

        let texts = UIStackView(arrangedSubviews: [title, body, warning])
        texts.axis = .vertical
        texts.alignment = .leading
        texts.spacing = 4
        texts.translatesAutoresizingMaskIntoConstraints = false

        for subview in [media, sponsored, feedback, texts] {
            addSubview(subview)
        }

        NSLayoutConstraint.activate([
            media.topAnchor.constraint(equalTo: topAnchor),
            media.leadingAnchor.constraint(equalTo: leadingAnchor),
            media.trailingAnchor.constraint(equalTo: trailingAnchor),
            media.bottomAnchor.constraint(equalTo: bottomAnchor),

            sponsored.topAnchor.constraint(equalTo: topAnchor, constant: 8),
            sponsored.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 8),

            feedback.topAnchor.constraint(equalTo: topAnchor, constant: 8),
            feedback.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -8),

            texts.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 8),
            texts.trailingAnchor.constraint(lessThanOrEqualTo: trailingAnchor, constant: -8),
            texts.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -8),
            texts.topAnchor.constraint(greaterThanOrEqualTo: sponsored.bottomAnchor, constant: 8),
        ])
    }
}
