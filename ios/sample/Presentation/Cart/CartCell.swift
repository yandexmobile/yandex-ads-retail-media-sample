import UIKit

final class CartCell: UITableViewCell {

    static let reuseIdentifier = "CartCell"
    private static let currency = "RUB"

    private let productImageView = UIImageView()
    private let nameLabel = UILabel()
    private let priceLabel = UILabel()
    private let minusButton = UIButton(type: .system)
    private let plusButton = UIButton(type: .system)
    private let quantityLabel = UILabel()
    private let removeButton = UIButton(type: .system)

    private var imageTask: Task<Void, Never>?
    private var onIncrement: (() -> Void)?
    private var onDecrement: (() -> Void)?
    private var onRemove: (() -> Void)?

    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        setUp()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func configure(
        with item: CartItem,
        onIncrement: @escaping () -> Void,
        onDecrement: @escaping () -> Void,
        onRemove: @escaping () -> Void
    ) {
        self.onIncrement = onIncrement
        self.onDecrement = onDecrement
        self.onRemove = onRemove

        nameLabel.text = item.name
        priceLabel.text = Money.format(item.price, currencyId: CartCell.currency)
        quantityLabel.text = String(item.quantity)
        loadImage(item.picture)
    }

    override func prepareForReuse() {
        super.prepareForReuse()
        imageTask?.cancel()
        productImageView.image = CartCell.placeholder
    }

    private func loadImage(_ picture: String) {
        imageTask?.cancel()
        productImageView.image = CartCell.placeholder
        imageTask = Task { [weak self] in
            let image = await ImageLoader.shared.load(picture)
            if Task.isCancelled { return }
            self?.productImageView.image = image ?? CartCell.placeholder
        }
    }

    private func setUp() {
        selectionStyle = .none

        productImageView.contentMode = .scaleAspectFill
        productImageView.clipsToBounds = true
        productImageView.layer.cornerRadius = 8
        productImageView.backgroundColor = .secondarySystemBackground
        productImageView.image = CartCell.placeholder
        productImageView.translatesAutoresizingMaskIntoConstraints = false
        productImageView.widthAnchor.constraint(equalToConstant: 60).isActive = true
        productImageView.heightAnchor.constraint(equalToConstant: 60).isActive = true

        nameLabel.font = .preferredFont(forTextStyle: .body)
        nameLabel.numberOfLines = 2
        priceLabel.font = .preferredFont(forTextStyle: .subheadline)
        priceLabel.textColor = .secondaryLabel

        let info = UIStackView(arrangedSubviews: [nameLabel, priceLabel])
        info.axis = .vertical
        info.spacing = 4

        minusButton.setImage(UIImage(systemName: "minus"), for: .normal)
        minusButton.addAction(UIAction { [weak self] _ in self?.onDecrement?() }, for: .touchUpInside)
        minusButton.widthAnchor.constraint(equalToConstant: 36).isActive = true
        plusButton.setImage(UIImage(systemName: "plus"), for: .normal)
        plusButton.addAction(UIAction { [weak self] _ in self?.onIncrement?() }, for: .touchUpInside)
        plusButton.widthAnchor.constraint(equalToConstant: 36).isActive = true
        quantityLabel.font = .preferredFont(forTextStyle: .headline)
        quantityLabel.textAlignment = .center
        quantityLabel.widthAnchor.constraint(equalToConstant: 28).isActive = true

        let stepper = UIStackView(arrangedSubviews: [minusButton, quantityLabel, plusButton])
        stepper.axis = .horizontal
        stepper.spacing = 8
        stepper.alignment = .center
        stepper.widthAnchor.constraint(equalToConstant: 116).isActive = true

        removeButton.setImage(UIImage(systemName: "xmark"), for: .normal)
        removeButton.tintColor = .secondaryLabel
        removeButton.addAction(UIAction { [weak self] _ in self?.onRemove?() }, for: .touchUpInside)

        let row = UIStackView(arrangedSubviews: [productImageView, info, stepper, removeButton])
        row.axis = .horizontal
        row.spacing = 12
        row.alignment = .center
        info.setContentHuggingPriority(.defaultLow, for: .horizontal)
        removeButton.setContentHuggingPriority(.required, for: .horizontal)
        row.translatesAutoresizingMaskIntoConstraints = false
        contentView.addSubview(row)

        NSLayoutConstraint.activate([
            row.topAnchor.constraint(equalTo: contentView.topAnchor, constant: 8),
            row.bottomAnchor.constraint(equalTo: contentView.bottomAnchor, constant: -8),
            row.leadingAnchor.constraint(equalTo: contentView.layoutMarginsGuide.leadingAnchor),
            row.trailingAnchor.constraint(equalTo: contentView.layoutMarginsGuide.trailingAnchor),
        ])
    }

    private static let placeholder: UIImage? = UIImage(systemName: "photo")
}
