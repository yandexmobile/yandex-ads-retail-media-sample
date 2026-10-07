import UIKit

final class CategoriesViewController: UIViewController, UITableViewDataSource, UITableViewDelegate {

    var onOpenSearch: (() -> Void)?
    var onSelectCategory: ((Category) -> Void)?

    private let viewModel: CategoriesViewModel
    private let banner = DisplayAdBannerView()
    private let tableView = UITableView(frame: .zero, style: .plain)
    private let refreshControl = UIRefreshControl()
    private let activityIndicator = UIActivityIndicatorView(style: .large)
    private let emptyView = StateView()
    private let errorView = StateView()
    private var categories: [Category] = []

    init(viewModel: CategoriesViewModel) {
        self.viewModel = viewModel
        super.init(nibName: nil, bundle: nil)
        title = Strings.titleCategories
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

        tableView.dataSource = self
        tableView.delegate = self
        tableView.register(CategoryCell.self, forCellReuseIdentifier: CategoryCell.reuseIdentifier)
        tableView.refreshControl = refreshControl
        refreshControl.addAction(UIAction { [weak self] _ in self?.viewModel.load() }, for: .valueChanged)

        emptyView.configure(systemImage: "tray", message: Strings.stateEmpty, showsRetry: false)
        emptyView.isHidden = true
        errorView.configure(systemImage: "exclamationmark.triangle", message: Strings.stateError, showsRetry: true)
        errorView.isHidden = true
        errorView.onRetry = { [weak self] in self?.viewModel.load() }

        let content = UIStackView(arrangedSubviews: [banner, tableView])
        content.axis = .vertical

        for subview in [content, emptyView, errorView, activityIndicator] {
            subview.translatesAutoresizingMaskIntoConstraints = false
            view.addSubview(subview)
        }

        NSLayoutConstraint.activate([
            content.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            content.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            content.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            content.bottomAnchor.constraint(equalTo: view.bottomAnchor),

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

        observeChanges { [weak self] in
            guard let self else { return }
            render(viewModel.state)
        }
        observeChanges { [weak self] in
            guard let self else { return }
            banner.show(viewModel.displayAds.ads)
        }
    }

    private func render(_ state: CategoriesUiState) {
        if case .loading = state, !refreshControl.isRefreshing {
            activityIndicator.startAnimating()
        } else {
            activityIndicator.stopAnimating()
        }

        emptyView.isHidden = state != .empty
        errorView.isHidden = state != .error

        if case .content(let items) = state {
            categories = items
        } else {
            categories = []
        }
        tableView.reloadData()

        if case .loading = state {} else {
            refreshControl.endRefreshing()
        }
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        categories.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(
            withIdentifier: CategoryCell.reuseIdentifier,
            for: indexPath
        ) as! CategoryCell
        cell.configure(with: categories[indexPath.row])
        return cell
    }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        onSelectCategory?(categories[indexPath.row])
    }
}
