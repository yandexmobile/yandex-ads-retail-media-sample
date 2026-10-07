import Foundation

enum Money {

    static func format(_ amount: Double, currencyId: String) -> String {
        let formatter = amount.truncatingRemainder(dividingBy: 1) == 0 ? integerFormatter : decimalFormatter
        let number = formatter.string(from: amount as NSNumber) ?? String(amount)
        switch currencyId.uppercased() {
        case "USD": return "$\(number)"
        case "RUB": return "\(number) ₽"
        case "EUR": return "\(number) €"
        default: return "\(number) \(currencyId)"
        }
    }

    private static let integerFormatter: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.groupingSeparator = " "
        formatter.maximumFractionDigits = 0
        return formatter
    }()

    private static let decimalFormatter: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.groupingSeparator = " "
        formatter.decimalSeparator = "."
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return formatter
    }()
}
