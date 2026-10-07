import UIKit
@_spi(RetailMedia) import YandexMobileAds

struct ProductAdBinding {
    let ad: RetailMediaAd
    let retailMedia: RetailMedia
    let binder: RetailMediaAdBinder
}

final class ProductCell: UICollectionViewCell {

    static let reuseIdentifier = "ProductCell"
    /// The SDK has no unbind, only rebind, so a cell that once held an ad is never reused for an organic product:
    /// it would keep reporting the old ad.
    static let sponsoredReuseIdentifier = "SponsoredProductCell"

    /// The view the slot of a sponsored page is bound to; see `ProductsGridView.slotBinder`.
    let slotView = RetailMediaAdView()
    var slotRetailMedia: RetailMedia?
    /// The page whose ad this card shows, so the slot of that page can move here.
    private(set) var adRetailMedia: RetailMedia?
    private let adView = RetailMediaAdView()
    private let imageView = UIImageView()
    private let adLabel = PaddingLabel()
    private let nameLabel = UILabel()
    private let priceLabel = UILabel()
    private let oldPriceLabel = UILabel()
    private let addToCartButton = UIButton(type: .system)
    private let minusButton = UIButton(type: .system)
    private let plusButton = UIButton(type: .system)
    private let quantityLabel = UILabel()
    private let stepperStack = UIStackView()

    private var imageTask: Task<Void, Never>?
    private var picture: String?
    private var boundAd: RetailMediaAd?
    private var onIncrease: (() -> Void)?
    private var onDecrease: (() -> Void)?

    override init(frame: CGRect) {
        super.init(frame: frame)
        setUp()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func configure(
        with row: ProductRow,
        adBinding: ProductAdBinding?,
        onIncrease: @escaping () -> Void,
        onDecrease: @escaping () -> Void
    ) {
        self.onIncrease = onIncrease
        self.onDecrease = onDecrease

        let product = row.product
        nameLabel.text = product.name
        priceLabel.text = Money.format(product.price, currencyId: product.currencyId)
        adLabel.isHidden = !product.sponsored

        if let oldPrice = product.oldPrice {
            oldPriceLabel.isHidden = false
            oldPriceLabel.attributedText = NSAttributedString(
                string: Money.format(oldPrice, currencyId: product.currencyId),
                attributes: [
                    .strikethroughStyle: NSUnderlineStyle.single.rawValue,
                    .foregroundColor: UIColor.secondaryLabel,
                ]
            )
        } else {
            oldPriceLabel.isHidden = true
            oldPriceLabel.attributedText = nil
        }

        if let adBinding {
            bind(adBinding)
        }

        let inCart = row.quantityInCart > 0
        addToCartButton.isHidden = inCart
        stepperStack.isHidden = !inCart
        quantityLabel.text = String(row.quantityInCart)

        loadImage(product.picture)
    }

    /// `boundAd` is reset because the ad may have been rebound to another cell meanwhile, which unbinds it here.
    override func prepareForReuse() {
        super.prepareForReuse()
        imageTask?.cancel()
        picture = nil
        boundAd = nil
        adRetailMedia = nil
        imageView.image = ProductCell.placeholder
    }

    // [Step 4] [Step 5]
    /// The card's own labels and button become the ad's assets. The store action runs inside the custom asset's
    /// click handler: once the button is bound, the SDK takes its taps and the button's own action no longer
    /// fires. Rebinding the same ad is skipped because each bind restarts impression tracking.
    private func bind(_ adBinding: ProductAdBinding) {
        let ad = adBinding.ad
        guard ad !== boundAd else { return }
        adView.titleLabel = nameLabel
        adView.priceLabel = priceLabel
        adView.customAssets = [
            RetailMediaCustomAsset(view: addToCartButton, type: "cart") { [weak self] _, _ in
                self?.onIncrease?()
            },
        ]
        do {
            try adBinding.binder.bind(ad, to: adView)
            boundAd = ad
            adRetailMedia = adBinding.retailMedia
        } catch {
            boundAd = nil
            adRetailMedia = nil
            logAdBindingFailure("Product card", error)
        }
    }

    private func loadImage(_ picture: String) {
        guard picture != self.picture else { return }
        self.picture = picture
        imageTask?.cancel()
        imageView.image = ProductCell.placeholder
        imageTask = Task { [weak self] in
            let image = await ImageLoader.shared.load(picture)
            if Task.isCancelled { return }
            self?.imageView.image = image ?? ProductCell.placeholder
        }
    }

    private func setUp() {
        contentView.backgroundColor = .systemBackground

        imageView.contentMode = .scaleAspectFill
        imageView.clipsToBounds = true
        imageView.layer.cornerRadius = 12
        imageView.backgroundColor = .secondarySystemBackground
        imageView.image = ProductCell.placeholder
        imageView.translatesAutoresizingMaskIntoConstraints = false
        imageView.heightAnchor.constraint(equalTo: imageView.widthAnchor).isActive = true

        adLabel.text = Strings.adLabel
        adLabel.font = .systemFont(ofSize: 10, weight: .medium)
        adLabel.textColor = .black
        adLabel.backgroundColor = UIColor(red: 1, green: 0xD5 / 255, blue: 0, alpha: 1)
        adLabel.layer.cornerRadius = 4
        adLabel.layer.masksToBounds = true
        adLabel.textInsets = UIEdgeInsets(top: 2, left: 6, bottom: 2, right: 6)
        adLabel.isHidden = true
        adLabel.translatesAutoresizingMaskIntoConstraints = false

        nameLabel.font = .preferredFont(forTextStyle: .subheadline)
        nameLabel.numberOfLines = 2
        nameLabel.textColor = .label

        priceLabel.font = .preferredFont(forTextStyle: .headline)
        priceLabel.textColor = .label

        oldPriceLabel.font = .preferredFont(forTextStyle: .footnote)

        let priceRow = UIStackView(arrangedSubviews: [priceLabel, oldPriceLabel])
        priceRow.axis = .horizontal
        priceRow.spacing = 6
        priceRow.alignment = .firstBaseline

        configureAddToCartButton()
        configureStepper()

        let cartArea = UIView()
        addToCartButton.translatesAutoresizingMaskIntoConstraints = false
        stepperStack.translatesAutoresizingMaskIntoConstraints = false
        cartArea.addSubview(addToCartButton)
        cartArea.addSubview(stepperStack)

        let stack = UIStackView(arrangedSubviews: [imageView, nameLabel, priceRow, cartArea])
        stack.axis = .vertical
        stack.spacing = 6
        stack.setCustomSpacing(8, after: priceRow)
        stack.translatesAutoresizingMaskIntoConstraints = false

        slotView.translatesAutoresizingMaskIntoConstraints = false
        adView.translatesAutoresizingMaskIntoConstraints = false
        contentView.addSubview(slotView)
        slotView.addSubview(adView)
        adView.addSubview(stack)
        adView.addSubview(adLabel)

        NSLayoutConstraint.activate([
            slotView.topAnchor.constraint(equalTo: contentView.topAnchor),
            slotView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor),
            slotView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor),
            slotView.bottomAnchor.constraint(equalTo: contentView.bottomAnchor),

            adView.topAnchor.constraint(equalTo: slotView.topAnchor),
            adView.leadingAnchor.constraint(equalTo: slotView.leadingAnchor),
            adView.trailingAnchor.constraint(equalTo: slotView.trailingAnchor),
            adView.bottomAnchor.constraint(equalTo: slotView.bottomAnchor),

            stack.topAnchor.constraint(equalTo: adView.topAnchor),
            stack.leadingAnchor.constraint(equalTo: adView.leadingAnchor),
            stack.trailingAnchor.constraint(equalTo: adView.trailingAnchor),
            stack.bottomAnchor.constraint(lessThanOrEqualTo: adView.bottomAnchor),

            adLabel.topAnchor.constraint(equalTo: imageView.topAnchor, constant: 8),
            adLabel.leadingAnchor.constraint(equalTo: imageView.leadingAnchor, constant: 8),

            cartArea.heightAnchor.constraint(equalToConstant: ProductCell.controlHeight),
            addToCartButton.topAnchor.constraint(equalTo: cartArea.topAnchor),
            addToCartButton.bottomAnchor.constraint(equalTo: cartArea.bottomAnchor),
            addToCartButton.leadingAnchor.constraint(equalTo: cartArea.leadingAnchor),
            addToCartButton.trailingAnchor.constraint(equalTo: cartArea.trailingAnchor),
            stepperStack.topAnchor.constraint(equalTo: cartArea.topAnchor),
            stepperStack.bottomAnchor.constraint(equalTo: cartArea.bottomAnchor),
            stepperStack.leadingAnchor.constraint(equalTo: cartArea.leadingAnchor),
            stepperStack.trailingAnchor.constraint(equalTo: cartArea.trailingAnchor),
        ])
    }

    private static let controlHeight: CGFloat = 44

    private func configureAddToCartButton() {
        var configuration = UIButton.Configuration.bordered()
        configuration.title = Strings.actionAddToCart
        configuration.cornerStyle = .medium
        addToCartButton.configuration = configuration
        addToCartButton.addAction(UIAction { [weak self] _ in self?.onIncrease?() }, for: .touchUpInside)
    }

    private func configureStepper() {
        minusButton.setImage(UIImage(systemName: "minus"), for: .normal)
        minusButton.addAction(UIAction { [weak self] _ in self?.onDecrease?() }, for: .touchUpInside)
        plusButton.setImage(UIImage(systemName: "plus"), for: .normal)
        plusButton.addAction(UIAction { [weak self] _ in self?.onIncrease?() }, for: .touchUpInside)

        // Only the quantity label should stretch; the buttons hug their icons so they
        // sit at the edges with the number centered between them.
        for button in [minusButton, plusButton] {
            button.setContentHuggingPriority(.required, for: .horizontal)
            button.setContentCompressionResistancePriority(.required, for: .horizontal)
        }

        quantityLabel.font = .preferredFont(forTextStyle: .headline)
        quantityLabel.textAlignment = .center
        quantityLabel.setContentHuggingPriority(.defaultLow, for: .horizontal)
        quantityLabel.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)

        stepperStack.axis = .horizontal
        stepperStack.distribution = .fill
        stepperStack.alignment = .center
        stepperStack.spacing = 8
        stepperStack.addArrangedSubview(minusButton)
        stepperStack.addArrangedSubview(quantityLabel)
        stepperStack.addArrangedSubview(plusButton)
        stepperStack.layer.cornerRadius = 8
        stepperStack.layer.borderWidth = 1
        stepperStack.layer.borderColor = UIColor.separator.cgColor
        stepperStack.isLayoutMarginsRelativeArrangement = true
        stepperStack.layoutMargins = UIEdgeInsets(top: 2, left: 8, bottom: 2, right: 8)
        stepperStack.isHidden = true
    }

    private static let placeholder: UIImage? = UIImage(systemName: "photo")
}
