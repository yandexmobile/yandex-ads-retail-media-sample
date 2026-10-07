import UIKit

enum Toast {

    private static var activeContainer: UIView?

    static func show(_ message: String, in view: UIView) {
        activeContainer?.removeFromSuperview()

        let container = UIView()
        container.backgroundColor = UIColor.label.withAlphaComponent(0.9)
        container.layer.cornerRadius = 12
        container.alpha = 0
        container.translatesAutoresizingMaskIntoConstraints = false

        let label = UILabel()
        label.text = message
        label.textColor = .systemBackground
        label.font = .preferredFont(forTextStyle: .subheadline)
        label.textAlignment = .center
        label.numberOfLines = 0
        label.translatesAutoresizingMaskIntoConstraints = false

        container.addSubview(label)
        view.addSubview(container)
        activeContainer = container

        NSLayoutConstraint.activate([
            label.topAnchor.constraint(equalTo: container.topAnchor, constant: 10),
            label.bottomAnchor.constraint(equalTo: container.bottomAnchor, constant: -10),
            label.leadingAnchor.constraint(equalTo: container.leadingAnchor, constant: 16),
            label.trailingAnchor.constraint(equalTo: container.trailingAnchor, constant: -16),

            container.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            container.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor, constant: -24),
            container.leadingAnchor.constraint(greaterThanOrEqualTo: view.leadingAnchor, constant: 24),
            container.trailingAnchor.constraint(lessThanOrEqualTo: view.trailingAnchor, constant: -24),
        ])

        UIView.animate(withDuration: 0.2, animations: { container.alpha = 1 }) { _ in
            UIView.animate(
                withDuration: 0.2,
                delay: 1.6,
                options: [],
                animations: { container.alpha = 0 },
                completion: { _ in
                    container.removeFromSuperview()
                    if activeContainer === container {
                        activeContainer = nil
                    }
                }
            )
        }
    }
}
