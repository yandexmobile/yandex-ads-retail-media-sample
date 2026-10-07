import Foundation

enum APIError: Error {
    case invalidResponse
    case http(status: Int)
    case decoding(Error)
}
