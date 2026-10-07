import Foundation

final class StoreAPIClient: StoreAPI {

    private let baseURL: URL
    private let session: URLSession
    private let decoder = JSONDecoder()
    private let encoder = JSONEncoder()

    init(baseURL: URL, session: URLSession = .shared) {
        self.baseURL = baseURL
        self.session = session
    }

    func home(_ body: CatalogAdRequestDTO) async throws -> CatalogResponseDTO {
        try await request("home", method: "POST", body: try encoder.encode(body))
    }

    func categories() async throws -> [CategoryDTO] {
        try await request("categories")
    }

    func productsInCategory(id: String, body: CatalogAdRequestDTO) async throws -> CatalogResponseDTO {
        try await request("categories/\(id)/products", method: "POST", body: try encoder.encode(body))
    }

    func search(_ body: SearchAdRequestDTO) async throws -> CatalogResponseDTO {
        try await request("search", method: "POST", body: try encoder.encode(body))
    }

    func ads(_ body: AdsRequestDTO) async throws -> AdSlotDTO? {
        let (data, status) = try await send("ads", method: "POST", body: try encoder.encode(body))
        guard status != 204 else { return nil }
        return try decode(data)
    }

    func cart() async throws -> CartDTO {
        try await request("cart")
    }

    func addToCart(_ body: AddToCartRequestDTO) async throws -> CartDTO {
        try await request("cart/items", method: "POST", body: try encoder.encode(body))
    }

    func removeFromCart(id: String) async throws -> CartDTO {
        try await request("cart/items/\(id)", method: "DELETE")
    }

    func checkout() async throws -> CheckoutResultDTO {
        try await request("cart/checkout", method: "POST")
    }

    private func request<Response: Decodable>(
        _ path: String,
        method: String = "GET",
        body: Data? = nil
    ) async throws -> Response {
        let (data, _) = try await send(path, method: method, body: body)
        return try decode(data)
    }

    private func send(_ path: String, method: String, body: Data?) async throws -> (Data, Int) {
        var urlRequest = URLRequest(url: baseURL.appendingPathComponent(path))
        urlRequest.httpMethod = method
        if let body {
            urlRequest.httpBody = body
            urlRequest.setValue("application/json", forHTTPHeaderField: "Content-Type")
        }

        let (data, response) = try await session.data(for: urlRequest)
        guard let http = response as? HTTPURLResponse else {
            throw APIError.invalidResponse
        }
        guard (200..<300).contains(http.statusCode) else {
            throw APIError.http(status: http.statusCode)
        }
        return (data, http.statusCode)
    }

    private func decode<Response: Decodable>(_ data: Data) throws -> Response {
        do {
            return try decoder.decode(Response.self, from: data)
        } catch {
            throw APIError.decoding(error)
        }
    }
}
