import UIKit
@_spi(RetailMedia) import YandexMobileAds

final class ProductsGridView: UIView, UICollectionViewDelegateFlowLayout {

    var onRefresh: (() -> Void)?
    var onLoadMore: (() -> Void)?
    var onRetry: (() -> Void)?
    var onIncrease: ((Product) -> Void)?
    var onDecrease: ((Product) -> Void)?

    private let collectionView: UICollectionView
    private let refreshControl = UIRefreshControl()
    private let activityIndicator = UIActivityIndicatorView(style: .large)
    private let emptyView = StateView()
    private let errorView = StateView()

    private lazy var dataSource = UICollectionViewDiffableDataSource<Int, FeedItem.ID>(
        collectionView: collectionView
    ) { [weak self] collectionView, indexPath, id in
        self?.cell(for: id, at: indexPath, in: collectionView)
    }
    private var itemsById: [FeedItem.ID: FeedItem] = [:]
    private var productCount = 0
    private var lastLoadTriggerCount = 0
    private var slotBindings: [ObjectIdentifier: SlotBinding] = [:]

    private let interitemSpacing: CGFloat = 12
    private let sectionInset: CGFloat = 12

    override init(frame: CGRect) {
        let layout = UICollectionViewFlowLayout()
        layout.scrollDirection = .vertical
        collectionView = UICollectionView(frame: .zero, collectionViewLayout: layout)
        super.init(frame: frame)
        setUp()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func setEmptyMessage(_ message: String) {
        emptyView.setMessage(message)
    }

    func render(_ state: ProductsUiState, showsEmptyForIdle: Bool = false) {
        let isRefreshing = refreshControl.isRefreshing

        if case .loading = state, !isRefreshing {
            activityIndicator.startAnimating()
        } else {
            activityIndicator.stopAnimating()
        }

        let isEmpty = state == .empty || (showsEmptyForIdle && state == .idle)
        emptyView.isHidden = !isEmpty
        errorView.isHidden = state != .error

        if case .content(let content) = state {
            update(items: content.items)
        } else {
            update(items: [])
        }

        if case .loading = state {} else {
            refreshControl.endRefreshing()
        }
    }

    /// A full reload hands cells to other index paths, so every card would rebind its ad and restart impression
    /// tracking on each cart change, page or slider arrival. The diffable data source keeps every card in its cell
    /// and only reconfigures the ones whose contents changed.
    private func update(items newItems: [FeedItem]) {
        let newProductCount = newItems.count(where: { if case .product = $0 { true } else { false } })
        if newProductCount < lastLoadTriggerCount {
            lastLoadTriggerCount = 0
        }
        productCount = newProductCount

        let newItemsById = Dictionary(newItems.map { ($0.id, $0) }, uniquingKeysWith: { first, _ in first })
        let changed = newItemsById.compactMap { id, item in
            itemsById[id].flatMap { $0 != item ? id : nil }
        }
        itemsById = newItemsById
        dropStaleSlotBindings()

        var seen = Set<FeedItem.ID>()
        var snapshot = NSDiffableDataSourceSnapshot<Int, FeedItem.ID>()
        snapshot.appendSections([0])
        snapshot.appendItems(newItems.map(\.id).filter { seen.insert($0).inserted })
        snapshot.reconfigureItems(changed)
        dataSource.apply(snapshot, animatingDifferences: false)
    }

    // [Step 4]
    /// Every page brings its own `RetailMedia`, whose slot must be bound to a container before its ads are bound
    /// to cards. The container is the outer view of a sponsored card rather than a view around the whole grid:
    /// the SDK keeps checking the visibility of the bound ad views and suppresses the "hidden ancestor" error
    /// only when the slot container sits inside a cell, as cells of a collection are hidden on purpose.
    ///
    /// The slot stays in the card that holds it while that card is on screen and still shows this page. When the
    /// card leaves the screen, the slot moves to another visible card of the same page (`handOverSlots`), so its
    /// tracking never stays in a hidden cell. Rebinding it on every sponsored card would restart slot tracking.
    private func slotBinder(for retailMedia: RetailMedia, in cell: ProductCell) -> RetailMediaAdBinder? {
        if let slot = slotBindings[ObjectIdentifier(retailMedia as AnyObject)], slot.hasLiveOwner {
            return slot.binder
        }
        return bindSlot(of: retailMedia, to: cell)
    }

    /// A card prepared ahead of display may have passed on the slot to a card that never showed up, or taken it
    /// while off screen; the card that actually appears picks it up.
    private func claimSlotIfOrphaned(for cell: ProductCell) {
        guard let retailMedia = cell.adRetailMedia,
              let slot = slotBindings[ObjectIdentifier(retailMedia as AnyObject)],
              !slot.hasLiveOwner else { return }
        _ = bindSlot(of: retailMedia, to: cell)
    }

    /// Binding a slot into a card that holds another page's slot unbinds that one in the SDK, so it loses its owner.
    private func bindSlot(of retailMedia: RetailMedia, to cell: ProductCell) -> RetailMediaAdBinder? {
        if let evicted = cell.slotRetailMedia, !isSameRetailMedia(evicted, retailMedia),
           let evictedSlot = slotBindings[ObjectIdentifier(evicted as AnyObject)], evictedSlot.owner === cell {
            evictedSlot.owner = nil
        }
        do {
            let binder = try retailMedia.bind(to: cell.slotView)
            cell.slotRetailMedia = retailMedia
            slotBindings[ObjectIdentifier(retailMedia as AnyObject)] = SlotBinding(
                retailMedia: retailMedia,
                binder: binder,
                owner: cell
            )
            return binder
        } catch {
            slotBindings[ObjectIdentifier(retailMedia as AnyObject)]?.owner = nil
            logAdBindingFailure("Product promo slot", error)
            return nil
        }
    }

    private func handOverSlots(from leavingCell: ProductCell) {
        let heldSlots = slotBindings.values.filter { $0.owner === leavingCell }
        let visibleCards = collectionView.visibleCells.compactMap { $0 as? ProductCell }.filter { $0 !== leavingCell }
        for slot in heldSlots {
            if let heir = visibleCards.first(where: { isSameRetailMedia($0.adRetailMedia, slot.retailMedia) }) {
                _ = bindSlot(of: slot.retailMedia, to: heir)
            } else {
                slot.owner = nil
            }
        }
    }

    private func dropStaleSlotBindings() {
        let shown = Set(itemsById.values.compactMap { item -> ObjectIdentifier? in
            guard case .product(let row) = item, let sponsoredAd = row.sponsoredAd else { return nil }
            return ObjectIdentifier(sponsoredAd.retailMedia as AnyObject)
        })
        slotBindings = slotBindings.filter { shown.contains($0.key) }
    }

    private func cell(
        for id: FeedItem.ID,
        at indexPath: IndexPath,
        in collectionView: UICollectionView
    ) -> UICollectionViewCell? {
        switch itemsById[id] {
        case .product(let row):
            let reuseIdentifier = row.sponsoredAd == nil ? ProductCell.reuseIdentifier : ProductCell.sponsoredReuseIdentifier
            let cell = collectionView.dequeueReusableCell(withReuseIdentifier: reuseIdentifier, for: indexPath) as! ProductCell
            let adBinding = row.sponsoredAd.flatMap { sponsoredAd in
                slotBinder(for: sponsoredAd.retailMedia, in: cell).map {
                    ProductAdBinding(ad: sponsoredAd.ad, retailMedia: sponsoredAd.retailMedia, binder: $0)
                }
            }
            cell.configure(
                with: row,
                adBinding: adBinding,
                onIncrease: { [weak self] in self?.onIncrease?(row.product) },
                onDecrease: { [weak self] in self?.onDecrease?(row.product) }
            )
            return cell
        case .adSlider(let ads):
            let cell = collectionView.dequeueReusableCell(
                withReuseIdentifier: AdSliderCell.reuseIdentifier,
                for: indexPath
            ) as! AdSliderCell
            cell.configure(with: ads)
            return cell
        case nil:
            return nil
        }
    }

    private func setUp() {
        backgroundColor = .systemBackground

        collectionView.backgroundColor = .systemBackground
        collectionView.alwaysBounceVertical = true
        collectionView.delegate = self
        collectionView.refreshControl = refreshControl
        collectionView.register(ProductCell.self, forCellWithReuseIdentifier: ProductCell.reuseIdentifier)
        collectionView.register(ProductCell.self, forCellWithReuseIdentifier: ProductCell.sponsoredReuseIdentifier)
        collectionView.register(AdSliderCell.self, forCellWithReuseIdentifier: AdSliderCell.reuseIdentifier)
        collectionView.contentInset = UIEdgeInsets(top: sectionInset, left: 0, bottom: sectionInset, right: 0)
        refreshControl.addAction(UIAction { [weak self] _ in self?.onRefresh?() }, for: .valueChanged)

        emptyView.configure(systemImage: "tray", message: Strings.stateEmpty, showsRetry: false)
        emptyView.isHidden = true

        errorView.configure(systemImage: "exclamationmark.triangle", message: Strings.stateError, showsRetry: true)
        errorView.isHidden = true
        errorView.onRetry = { [weak self] in self?.onRetry?() }

        for subview in [collectionView, emptyView, errorView, activityIndicator] {
            subview.translatesAutoresizingMaskIntoConstraints = false
            addSubview(subview)
        }

        NSLayoutConstraint.activate([
            collectionView.topAnchor.constraint(equalTo: topAnchor),
            collectionView.leadingAnchor.constraint(equalTo: leadingAnchor),
            collectionView.trailingAnchor.constraint(equalTo: trailingAnchor),
            collectionView.bottomAnchor.constraint(equalTo: bottomAnchor),

            emptyView.topAnchor.constraint(equalTo: safeAreaLayoutGuide.topAnchor),
            emptyView.leadingAnchor.constraint(equalTo: leadingAnchor),
            emptyView.trailingAnchor.constraint(equalTo: trailingAnchor),
            emptyView.bottomAnchor.constraint(equalTo: safeAreaLayoutGuide.bottomAnchor),

            errorView.topAnchor.constraint(equalTo: safeAreaLayoutGuide.topAnchor),
            errorView.leadingAnchor.constraint(equalTo: leadingAnchor),
            errorView.trailingAnchor.constraint(equalTo: trailingAnchor),
            errorView.bottomAnchor.constraint(equalTo: safeAreaLayoutGuide.bottomAnchor),

            activityIndicator.centerXAnchor.constraint(equalTo: centerXAnchor),
            activityIndicator.centerYAnchor.constraint(equalTo: safeAreaLayoutGuide.centerYAnchor),
        ])
    }

    // MARK: UICollectionViewDelegate

    func collectionView(
        _ collectionView: UICollectionView,
        willDisplay cell: UICollectionViewCell,
        forItemAt indexPath: IndexPath
    ) {
        (cell as? AdSliderCell)?.isOnScreen = true
        (cell as? ProductCell).map(claimSlotIfOrphaned(for:))
    }

    func collectionView(
        _ collectionView: UICollectionView,
        didEndDisplaying cell: UICollectionViewCell,
        forItemAt indexPath: IndexPath
    ) {
        (cell as? AdSliderCell)?.isOnScreen = false
        (cell as? ProductCell).map(handOverSlots(from:))
    }

    // MARK: UICollectionViewDelegateFlowLayout

    func collectionView(
        _ collectionView: UICollectionView,
        layout collectionViewLayout: UICollectionViewLayout,
        sizeForItemAt indexPath: IndexPath
    ) -> CGSize {
        let fullWidth = collectionView.bounds.width - sectionInset * 2
        switch dataSource.itemIdentifier(for: indexPath).flatMap({ itemsById[$0] }) {
        case .product, nil:
            let columns: CGFloat = 2
            let width = ((fullWidth - interitemSpacing * (columns - 1)) / columns).rounded(.down)
            return CGSize(width: width, height: width + 140)
        case .adSlider(let ads):
            return CGSize(width: fullWidth, height: AdSliderCell.height(for: ads, width: fullWidth))
        }
    }

    func collectionView(
        _ collectionView: UICollectionView,
        layout collectionViewLayout: UICollectionViewLayout,
        insetForSectionAt section: Int
    ) -> UIEdgeInsets {
        UIEdgeInsets(top: 0, left: sectionInset, bottom: 0, right: sectionInset)
    }

    func collectionView(
        _ collectionView: UICollectionView,
        layout collectionViewLayout: UICollectionViewLayout,
        minimumInteritemSpacingForSectionAt section: Int
    ) -> CGFloat {
        interitemSpacing
    }

    func collectionView(
        _ collectionView: UICollectionView,
        layout collectionViewLayout: UICollectionViewLayout,
        minimumLineSpacingForSectionAt section: Int
    ) -> CGFloat {
        interitemSpacing
    }

    // MARK: Pagination

    func scrollViewDidScroll(_ scrollView: UIScrollView) {
        guard scrollView.contentSize.height > 0 else { return }
        let threshold: CGFloat = 400
        let distanceToBottom = scrollView.contentSize.height - (scrollView.contentOffset.y + scrollView.bounds.height)
        if distanceToBottom < threshold, productCount > lastLoadTriggerCount {
            lastLoadTriggerCount = productCount
            onLoadMore?()
        }
    }
}

private final class SlotBinding {
    let retailMedia: RetailMedia
    let binder: RetailMediaAdBinder
    weak var owner: ProductCell?

    var hasLiveOwner: Bool {
        guard let owner else { return false }
        return owner.window != nil && !owner.isHidden && isSameRetailMedia(owner.slotRetailMedia, retailMedia)
    }

    init(retailMedia: RetailMedia, binder: RetailMediaAdBinder, owner: ProductCell) {
        self.retailMedia = retailMedia
        self.binder = binder
        self.owner = owner
    }
}
