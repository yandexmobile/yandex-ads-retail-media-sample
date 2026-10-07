import UIKit

final class CategoryCell: UITableViewCell {

    static let reuseIdentifier = "CategoryCell"

    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        accessoryType = .disclosureIndicator
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func configure(with category: Category) {
        var content = defaultContentConfiguration()
        content.text = category.name
        content.image = UIImage(systemName: "square.grid.2x2")
        contentConfiguration = content
    }
}
