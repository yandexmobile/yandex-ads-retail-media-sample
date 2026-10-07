import UIKit

final class SearchViewController: UIViewController, UISearchBarDelegate {

    private let viewModel: SearchViewModel
    private let gridView = ProductsGridView()
    private let searchBar = UISearchBar()

    init(viewModel: SearchViewModel) {
        self.viewModel = viewModel
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground

        searchBar.placeholder = Strings.searchHint
        searchBar.delegate = self
        searchBar.returnKeyType = .search

        searchBar.translatesAutoresizingMaskIntoConstraints = false
        gridView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(searchBar)
        view.addSubview(gridView)

        NSLayoutConstraint.activate([
            searchBar.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            searchBar.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            searchBar.trailingAnchor.constraint(equalTo: view.trailingAnchor),

            gridView.topAnchor.constraint(equalTo: searchBar.bottomAnchor),
            gridView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            gridView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            gridView.bottomAnchor.constraint(equalTo: view.bottomAnchor),
        ])

        gridView.onRefresh = { [weak self] in
            guard let self else { return }
            if viewModel.hasQuery {
                viewModel.refresh()
            } else {
                render(viewModel.state)
            }
        }
        gridView.onLoadMore = { [weak self] in self?.viewModel.loadMore() }
        gridView.onRetry = { [weak self] in self?.viewModel.refresh() }
        gridView.onIncrease = { [weak self] product in self?.viewModel.increase(product) }
        gridView.onDecrease = { [weak self] product in self?.viewModel.decrease(product) }

        viewModel.onEvent = { [weak self] event in
            guard let self else { return }
            switch event {
            case .cartActionFailed:
                Toast.show(Strings.msgActionFailed, in: view)
            }
        }

        observeChanges { [weak self] in
            guard let self else { return }
            render(viewModel.state)
        }
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        searchBar.becomeFirstResponder()
    }

    private func render(_ state: ProductsUiState) {
        gridView.setEmptyMessage(state == .empty ? Strings.searchEmpty : Strings.searchStart)
        gridView.render(state, showsEmptyForIdle: true)
    }

    func searchBarSearchButtonClicked(_ searchBar: UISearchBar) {
        viewModel.search(searchBar.text ?? "")
        searchBar.resignFirstResponder()
    }
}
