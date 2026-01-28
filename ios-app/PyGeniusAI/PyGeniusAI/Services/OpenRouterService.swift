import Foundation

class OpenRouterService {
    static let shared = OpenRouterService()
    
    private let apiKey = "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"
    private let baseURL = "https://openrouter.ai/api/v1/chat/completions"
    
    var hasApiKey: Bool { !apiKey.isEmpty }
    
    func askTutor(question: String, codeContext: String = "") async throws -> String {
        let systemPrompt = """
        You are PyGenius AI, a helpful Python programming tutor. 
        You help users learn Python, fix errors, and write better code.
        Be concise but thorough. Provide code examples when helpful.
        """
        
        var userPrompt = ""
        if !codeContext.isEmpty {
            userPrompt += "Here's my current code:\n```python\n\(codeContext)\n```\n\n"
        }
        userPrompt += question
        
        return try await callAPI(system: systemPrompt, user: userPrompt)
    }
    
    func explainCode(code: String) async throws -> String {
        let systemPrompt = """
        You are a Python code explainer. Explain the provided Python code clearly and concisely.
        Break down:
        1. What the code does overall
        2. Key concepts used
        3. Important functions/classes
        4. Any potential issues or improvements
        Use emoji icons to make it engaging.
        """
        
        let userPrompt = "Please explain this Python code:\n```python\n\(code)\n```"
        return try await callAPI(system: systemPrompt, user: userPrompt)
    }
    
    func findBugs(code: String) async throws -> String {
        let systemPrompt = """
        You are a Python code reviewer. Analyze the code for bugs, issues, and improvements.
        Check for syntax errors, logic bugs, performance issues, and best practice violations.
        Provide specific line numbers and fix suggestions.
        """
        
        let userPrompt = "Please analyze this Python code for bugs:\n```python\n\(code)\n```"
        return try await callAPI(system: systemPrompt, user: userPrompt)
    }
    
    func optimizeCode(code: String) async throws -> String {
        let systemPrompt = """
        You are a Python optimization expert. Analyze the provided code and suggest optimizations for:
        - Performance
        - Readability
        - Pythonic style
        - Memory usage
        Provide the optimized code with comments explaining the changes.
        """
        
        let userPrompt = "Please optimize this Python code:\n```python\n\(code)\n```"
        return try await callAPI(system: systemPrompt, user: userPrompt)
    }
    
    private func callAPI(system: String, user: String) async throws -> String {
        guard let url = URL(string: baseURL) else {
            throw URLError(.badURL)
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("Bearer \(apiKey)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("https://pygenius.ai", forHTTPHeaderField: "HTTP-Referer")
        request.setValue("PyGenius AI", forHTTPHeaderField: "X-Title")
        
        let body: [String: Any] = [
            "model": "openai/gpt-3.5-turbo",
            "messages": [
                ["role": "system", "content": system],
                ["role": "user", "content": user]
            ],
            "temperature": 0.7,
            "max_tokens": 2000
        ]
        
        request.httpBody = try JSONSerialization.data(withJSONObject: body)
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw URLError(.badServerResponse)
        }
        
        let json = try JSONSerialization.jsonObject(with: data) as? [String: Any]
        
        if let choices = json?["choices"] as? [[String: Any]],
           let first = choices.first,
           let message = first["message"] as? [String: Any],
           let content = message["content"] as? String {
            return content
        }
        
        return "Error: Unable to parse response"
    }
}
