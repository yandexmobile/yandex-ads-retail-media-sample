import Foundation

struct PageDTO<Element: Decodable>: Decodable {
    let items: [Element]
    let page: Int
    let pageSize: Int
    let hasMore: Bool
}
