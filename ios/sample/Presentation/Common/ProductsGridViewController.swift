import UIKit

final class ProductsGridViewController: UIViewController {

    var onOpenSearch: (() -> Void)?

    private let viewModel: ProductsViewModel
    private let gridView = ProductsGridView()

    init(viewModel: ProductsViewModel, title: String) {
        self.viewModel = viewModel
        super.init(nibName: nil, bundle: nil)
        self.title = title
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func loadView() {
        view = gridView
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        let searchItem = UIBarButtonItem(
            image: UIImage(systemName: "magnifyingglass"),
            primaryAction: UIAction { [weak self] _ in self?.onOpenSearch?() }
        )
        searchItem.accessibilityLabel = Strings.actionSearch
        navigationItem.rightBarButtonItem = searchItem

        gridView.onRefresh = { [weak self] in self?.viewModel.refresh() }
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
            gridView.render(viewModel.state)
        }
    }
}
