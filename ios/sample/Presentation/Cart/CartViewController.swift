import UIKit

final class CartViewController: UIViewController, UITableViewDataSource {

    var onOpenSearch: (() -> Void)?

    private let viewModel: CartViewModel
    private let banner = DisplayAdBannerView()
    private let tableView = UITableView(frame: .zero, style: .plain)
    private let refreshControl = UIRefreshControl()
    private let activityIndicator = UIActivityIndicatorView(style: .large)
    private let emptyView = StateView()
    private let errorView = StateView()
    private let footerView = UIView()
    private let totalLabel = UILabel()
    private let checkoutButton = UIButton(type: .system)
    private var footerHeight: NSLayoutConstraint!

    private var items: [CartItem] = []

    init(viewModel: CartViewModel) {
        self.viewModel = viewModel
        super.init(nibName: nil, bundle: nil)
        title = Strings.titleCart
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground

        let searchItem = UIBarButtonItem(
            image: UIImage(systemName: "magnifyingglass"),
            primaryAction: UIAction { [weak self] _ in self?.onOpenSearch?() }
        )
        searchItem.accessibilityLabel = Strings.actionSearch
        navigationItem.rightBarButtonItem = searchItem

        setUpTable()
        setUpFooter()
        setUpStateViews()
        layout()

        viewModel.onEvent = { [weak self] event in
            self?.showEvent(event)
        }
        observeChanges { [weak self] in
            guard let self else { return }
            render(viewModel.state)
        }
        observeChanges { [weak self] in
            guard let self else { return }
            banner.show(viewModel.displayAds.ads)
        }
    }

    private func setUpTable() {
        tableView.dataSource = self
        tableView.register(CartCell.self, forCellReuseIdentifier: CartCell.reuseIdentifier)
        tableView.refreshControl = refreshControl
        tableView.allowsSelection = false
        refreshControl.addAction(UIAction { [weak self] _ in self?.viewModel.refresh() }, for: .valueChanged)
    }

    private func setUpFooter() {
        footerView.backgroundColor = .systemBackground

        totalLabel.font = .preferredFont(forTextStyle: .headline)

        var configuration = UIButton.Configuration.borderedProminent()
        configuration.title = Strings.actionCheckout
        checkoutButton.configuration = configuration
        checkoutButton.addAction(UIAction { [weak self] _ in self?.viewModel.checkout() }, for: .touchUpInside)

        let stack = UIStackView(arrangedSubviews: [totalLabel, checkoutButton])
        stack.axis = .horizontal
        stack.alignment = .center
        stack.distribution = .equalSpacing
        stack.translatesAutoresizingMaskIntoConstraints = false
        footerView.addSubview(stack)

        NSLayoutConstraint.activate([
            stack.topAnchor.constraint(equalTo: footerView.topAnchor, constant: 12),
            stack.bottomAnchor.constraint(equalTo: footerView.bottomAnchor, constant: -12),
            stack.leadingAnchor.constraint(equalTo: footerView.layoutMarginsGuide.leadingAnchor),
            stack.trailingAnchor.constraint(equalTo: footerView.layoutMarginsGuide.trailingAnchor),
        ])
    }

    private func setUpStateViews() {
        emptyView.configure(systemImage: "cart", message: Strings.cartEmpty, showsRetry: false)
        emptyView.isHidden = true
        errorView.configure(systemImage: "exclamationmark.triangle", message: Strings.stateError, showsRetry: true)
        errorView.isHidden = true
        errorView.onRetry = { [weak self] in self?.viewModel.refresh() }
    }

    private func layout() {
        let topBorder = UIView()
        topBorder.backgroundColor = .separator
        topBorder.translatesAutoresizingMaskIntoConstraints = false
        footerView.addSubview(topBorder)

        let content = UIStackView(arrangedSubviews: [banner, tableView])
        content.axis = .vertical

        for subview in [content, footerView, emptyView, errorView, activityIndicator] {
            subview.translatesAutoresizingMaskIntoConstraints = false
            view.addSubview(subview)
        }

        footerHeight = footerView.heightAnchor.constraint(equalToConstant: 0)

        NSLayoutConstraint.activate([
            content.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            content.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            content.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            content.bottomAnchor.constraint(equalTo: footerView.topAnchor),

            footerView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            footerView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            footerView.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor),

            topBorder.topAnchor.constraint(equalTo: footerView.topAnchor),
            topBorder.leadingAnchor.constraint(equalTo: footerView.leadingAnchor),
            topBorder.trailingAnchor.constraint(equalTo: footerView.trailingAnchor),
            topBorder.heightAnchor.constraint(equalToConstant: 0.5),

            emptyView.topAnchor.constraint(equalTo: tableView.topAnchor),
            emptyView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            emptyView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            emptyView.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor),

            errorView.topAnchor.constraint(equalTo: tableView.topAnchor),
            errorView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            errorView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            errorView.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor),

            activityIndicator.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            activityIndicator.centerYAnchor.constraint(equalTo: view.safeAreaLayoutGuide.centerYAnchor),
        ])
    }

    private func render(_ state: CartUiState) {
        if case .loading = state, !refreshControl.isRefreshing {
            activityIndicator.startAnimating()
        } else {
            activityIndicator.stopAnimating()
        }

        emptyView.isHidden = state != .empty
        errorView.isHidden = state != .error

        if case .content(let items, let total) = state {
            self.items = items
            totalLabel.text = "\(Strings.cartTotal): \(Money.format(total, currencyId: "RUB"))"
            footerView.isHidden = false
            footerHeight.isActive = false
        } else {
            self.items = []
            footerView.isHidden = true
            footerHeight.isActive = true
        }
        tableView.reloadData()

        if case .loading = state {} else {
            refreshControl.endRefreshing()
        }
    }

    private func showEvent(_ event: CartEvent) {
        switch event {
        case .checkedOut:
            Toast.show(Strings.msgOrderPlaced, in: view)
        case .actionFailed:
            Toast.show(Strings.msgActionFailed, in: view)
        }
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        items.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(
            withIdentifier: CartCell.reuseIdentifier,
            for: indexPath
        ) as! CartCell
        let item = items[indexPath.row]
        cell.configure(
            with: item,
            onIncrement: { [weak self] in self?.viewModel.increment(item) },
            onDecrement: { [weak self] in self?.viewModel.decrement(item) },
            onRemove: { [weak self] in self?.viewModel.remove(item) }
        )
        return cell
    }
}
