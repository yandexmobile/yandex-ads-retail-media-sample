import Foundation

struct Page<Element: Equatable>: Equatable {
    let items: [Element]
    let page: Int
    let pageSize: Int
    let hasMore: Bool
}
