import Foundation

extension CategoryDTO {
    func toDomain() -> Category {
        Category(id: id, name: name)
    }
}

extension ProductDTO {
    func toDomain() -> Product {
        Product(
            id: id,
            name: name,
            price: price,
            oldPrice: oldPrice,
            currencyId: currencyId,
            picture: picture,
            categoryId: categoryId,
            url: url,
            available: available,
            vendor: vendor,
            sponsored: sponsored ?? false
        )
    }
}

extension AdSlotDTO {
    func toDomain() -> AdSlot {
        AdSlot(adUnitId: adUnitId, readyResponse: readyResponse)
    }
}

extension CatalogResponseDTO {
    func toDomain() -> CatalogPage {
        CatalogPage(products: products.toProductPage(), adSlot: adSlot?.toDomain())
    }
}

extension PageDTO where Element == ProductDTO {
    func toProductPage() -> Page<Product> {
        Page(
            items: items.map { $0.toDomain() },
            page: page,
            pageSize: pageSize,
            hasMore: hasMore
        )
    }
}
