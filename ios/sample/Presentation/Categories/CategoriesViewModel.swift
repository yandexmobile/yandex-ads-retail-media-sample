import Foundation
import Observation

@Observable
final class CategoriesViewModel {

    private(set) var state: CategoriesUiState = .loading
    let displayAds: ScreenDisplayAds

    @ObservationIgnored private let catalog: CatalogRepository

    init(catalog: CatalogRepository, displayAds: ScreenDisplayAds) {
        self.catalog = catalog
        self.displayAds = displayAds
        load()
    }

    func load() {
        state = .loading
        displayAds.reload()
        Task {
            do {
                let categories = try await catalog.categories()
                state = categories.isEmpty ? .empty : .content(categories)
            } catch {
                state = .error
            }
        }
    }
}
