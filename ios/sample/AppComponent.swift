import UIKit

final class AppComponent {

    private let catalogRepository: CatalogRepository
    private let cartRepository: CartRepository
    private let adRepository: RetailMediaAdRepository
    private let displayAdsLoader: DisplayAdsLoader

    init() {
        let baseURL = URL(string: "http://localhost:8080")!
        let apiClient = StoreAPIClient(baseURL: baseURL)
        let bidderTokenProvider = RetailMediaBidderTokenProvider()
        let session = AdSessionTracker()
        catalogRepository = CatalogRepositoryImpl(
            api: apiClient,
            bidderTokenProvider: bidderTokenProvider,
            session: session
        )
        cartRepository = CartRepositoryImpl(api: apiClient)
        adRepository = RetailMediaAdRepository()
        displayAdsLoader = DisplayAdsLoader(
            adsRepository: AdsRepository(
                api: apiClient,
                bidderTokenProvider: bidderTokenProvider,
                session: session
            ),
            adRepository: adRepository
        )
    }

    func makeRootViewController() -> UIViewController {
        let tabBarController = UITabBarController()
        tabBarController.viewControllers = [
            makeTab(makeHome(), title: Strings.titleHome, systemImage: "house.fill"),
            makeTab(makeCategories(), title: Strings.titleCategories, systemImage: "square.grid.2x2.fill"),
            makeTab(makeCart(), title: Strings.titleCart, systemImage: "cart.fill"),
        ]
        return tabBarController
    }

    private func makeHome() -> UIViewController {
        let viewModel = HomeViewModel(
            catalog: catalogRepository,
            displayAds: ScreenDisplayAds(screen: .home, loader: displayAdsLoader),
            adRepository: adRepository,
            cart: cartRepository
        )
        let controller = ProductsGridViewController(viewModel: viewModel, title: Strings.appName)
        controller.onOpenSearch = { [weak controller] in
            controller?.navigationController?.pushViewController(self.makeSearch(), animated: true)
        }
        return controller
    }

    private func makeCategories() -> UIViewController {
        let viewModel = CategoriesViewModel(
            catalog: catalogRepository,
            displayAds: ScreenDisplayAds(screen: .categories, loader: displayAdsLoader)
        )
        let controller = CategoriesViewController(viewModel: viewModel)
        controller.onOpenSearch = { [weak controller] in
            controller?.navigationController?.pushViewController(self.makeSearch(), animated: true)
        }
        controller.onSelectCategory = { [weak controller] category in
            controller?.navigationController?.pushViewController(self.makeProductList(category: category), animated: true)
        }
        return controller
    }

    private func makeProductList(category: Category) -> UIViewController {
        let viewModel = ProductListViewModel(
            categoryId: category.id,
            categoryName: category.name,
            catalog: catalogRepository,
            adRepository: adRepository,
            cart: cartRepository
        )
        let controller = ProductsGridViewController(viewModel: viewModel, title: viewModel.categoryName)
        controller.onOpenSearch = { [weak controller] in
            controller?.navigationController?.pushViewController(self.makeSearch(), animated: true)
        }
        return controller
    }

    private func makeSearch() -> UIViewController {
        let viewModel = SearchViewModel(
            catalog: catalogRepository,
            adRepository: adRepository,
            cart: cartRepository
        )
        return SearchViewController(viewModel: viewModel)
    }

    private func makeCart() -> UIViewController {
        let viewModel = CartViewModel(
            cart: cartRepository,
            displayAds: ScreenDisplayAds(screen: .cart, loader: displayAdsLoader)
        )
        let controller = CartViewController(viewModel: viewModel)
        controller.onOpenSearch = { [weak controller] in
            controller?.navigationController?.pushViewController(self.makeSearch(), animated: true)
        }
        return controller
    }

    private func makeTab(_ root: UIViewController, title: String, systemImage: String) -> UINavigationController {
        let navigationController = UINavigationController(rootViewController: root)
        navigationController.tabBarItem = UITabBarItem(
            title: title,
            image: UIImage(systemName: systemImage),
            selectedImage: nil
        )
        return navigationController
    }
}
