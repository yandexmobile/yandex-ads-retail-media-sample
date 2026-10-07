import UIKit

final class StateView: UIView {

    var onRetry: (() -> Void)?

    private let imageView = UIImageView()
    private let messageLabel = UILabel()
    private let retryButton = UIButton(type: .system)

    init() {
        super.init(frame: .zero)
        setUp()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func configure(systemImage: String, message: String, showsRetry: Bool) {
        imageView.image = UIImage(systemName: systemImage)
        messageLabel.text = message
        retryButton.isHidden = !showsRetry
    }

    func setMessage(_ message: String) {
        messageLabel.text = message
    }

    private func setUp() {
        imageView.tintColor = .tertiaryLabel
        imageView.contentMode = .scaleAspectFit
        imageView.preferredSymbolConfiguration = UIImage.SymbolConfiguration(pointSize: 44, weight: .regular)

        messageLabel.font = .preferredFont(forTextStyle: .body)
        messageLabel.textColor = .secondaryLabel
        messageLabel.textAlignment = .center
        messageLabel.numberOfLines = 0

        var configuration = UIButton.Configuration.borderedProminent()
        configuration.title = Strings.actionRetry
        retryButton.configuration = configuration
        retryButton.isHidden = true
        retryButton.addAction(UIAction { [weak self] _ in self?.onRetry?() }, for: .touchUpInside)

        let stack = UIStackView(arrangedSubviews: [imageView, messageLabel, retryButton])
        stack.axis = .vertical
        stack.spacing = 12
        stack.alignment = .center
        stack.translatesAutoresizingMaskIntoConstraints = false
        addSubview(stack)

        NSLayoutConstraint.activate([
            stack.centerXAnchor.constraint(equalTo: centerXAnchor),
            stack.centerYAnchor.constraint(equalTo: centerYAnchor),
            stack.leadingAnchor.constraint(greaterThanOrEqualTo: leadingAnchor, constant: 32),
            stack.trailingAnchor.constraint(lessThanOrEqualTo: trailingAnchor, constant: -32),
        ])
    }
}
