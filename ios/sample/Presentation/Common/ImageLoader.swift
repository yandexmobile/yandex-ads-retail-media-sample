import UIKit

final class ImageLoader {

    static let shared = ImageLoader()

    private let cache = NSCache<NSURL, UIImage>()
    private let session: URLSession

    init(session: URLSession = .shared) {
        self.session = session
        cache.countLimit = 200
    }

    func load(_ urlString: String) async -> UIImage? {
        guard let url = URL(string: urlString) else { return nil }
        if let cached = cache.object(forKey: url as NSURL) {
            return cached
        }
        guard !Task.isCancelled,
              let (data, _) = try? await session.data(from: url),
              let image = UIImage(data: data) else {
            return nil
        }
        cache.setObject(image, forKey: url as NSURL)
        return image
    }
}
