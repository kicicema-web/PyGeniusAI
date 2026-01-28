import SwiftUI
import Combine

@MainActor
class PyGeniusViewModel: ObservableObject {
    @Published var code: String = DEFAULT_CODE
    @Published var consoleOutput: [ConsoleLine] = []
    @Published var isRunning: Bool = false
    @Published var selectedTab: Tab = .editor
    @Published var aiResponses: [AIResponse] = []
    @Published var isAiProcessing: Bool = false
    @Published var apiKeyStatus: ApiKeyStatus = .unknown
    @Published var bugPredictions: [BugPrediction] = []
    @Published var currentFileName: String = "untitled.py"
    @Published var isModified: Bool = false
    
    private let openRouterService = OpenRouterService.shared
    
    init() {
        checkApiKeyStatus()
    }
    
    func checkApiKeyStatus() {
        apiKeyStatus = openRouterService.hasApiKey ? .configured : .notConfigured
    }
    
    func updateCode(_ newCode: String) {
        code = newCode
        isModified = true
    }
    
    func runCode() {
        isRunning = true
        addConsoleLine("=== Running Python Code ===", type: .info)
        addConsoleLine("iOS Note: Full Python execution requires additional setup.", type: .info)
        addConsoleLine("This feature is available in the Android and Desktop versions.", type: .info)
        isRunning = false
    }
    
    func clearConsole() {
        consoleOutput.removeAll()
    }
    
    private func addConsoleLine(_ text: String, type: LineType) {
        let line = ConsoleLine(text: text, type: type, timestamp: Date())
        consoleOutput.append(line)
    }
    
    func executeConsoleInput(_ input: String) {
        // Add the input to console
        addConsoleLine(">>> \(input)", type: .input)
        
        // Try to execute as Python code
        Task {
            isRunning = true
            
            // For now, echo back that execution would happen here
            // In a full implementation, this would use PythonKit or a server
            addConsoleLine("Executing: \(input)", type: .info)
            
            // Simulate some processing
            try? await Task.sleep(nanoseconds: 500_000_000) // 0.5 seconds
            
            // Try to evaluate simple expressions
            if let result = evaluateSimpleExpression(input) {
                addConsoleLine("\(result)", type: .output)
            } else {
                addConsoleLine("Note: Full Python execution requires Python runtime integration.", type: .info)
                addConsoleLine("Use the Run button in the Editor to execute full scripts.", type: .info)
            }
            
            isRunning = false
        }
    }
    
    private func evaluateSimpleExpression(_ expression: String) -> String? {
        // Simple evaluator for basic math and strings
        let trimmed = expression.trimmingCharacters(in: .whitespaces)
        
        // Try to evaluate as a simple math expression
        let mathChars = CharacterSet(charactersIn: "0123456789+-*/(). ")
        if trimmed.rangeOfCharacter(from: mathChars.inverted) == nil {
            let exp = NSExpression(format: trimmed)
            if let result = exp.expressionValue(with: nil, context: nil) {
                return "\(result)"
            }
        }
        
        // Check for print statements - extract the content
        if trimmed.hasPrefix("print(") && trimmed.hasSuffix(")") {
            let start = trimmed.index(trimmed.startIndex, offsetBy: 6)
            let end = trimmed.index(trimmed.endIndex, offsetBy: -1)
            let content = String(trimmed[start..<end])
            
            // Remove quotes if present
            var output = content
            if (content.hasPrefix("\"") && content.hasSuffix("\"")) ||
               (content.hasPrefix("'") && content.hasSuffix("'")) {
                output = String(content.dropFirst().dropLast())
            }
            return output
        }
        
        return nil
    }
    
    func askAI(_ question: String) {
        guard !question.isEmpty else { return }
        let userResponse = AIResponse(text: question, isUser: true, timestamp: Date())
        aiResponses.append(userResponse)
        isAiProcessing = true
        
        Task {
            do {
                let response = try await openRouterService.askTutor(question: question, codeContext: code)
                let aiResponse = AIResponse(text: response, isUser: false, timestamp: Date())
                aiResponses.append(aiResponse)
            } catch {
                let errorResponse = AIResponse(text: "Error: \(error.localizedDescription)", isUser: false, timestamp: Date())
                aiResponses.append(errorResponse)
            }
            isAiProcessing = false
        }
    }
    
    func explainCode() {
        isAiProcessing = true
        Task {
            do {
                let response = try await openRouterService.explainCode(code: code)
                let aiResponse = AIResponse(text: response, isUser: false, timestamp: Date())
                aiResponses.append(aiResponse)
            } catch {
                let errorResponse = AIResponse(text: "Error: \(error.localizedDescription)", isUser: false, timestamp: Date())
                aiResponses.append(errorResponse)
            }
            isAiProcessing = false
        }
    }
    
    func findBugs() {
        isAiProcessing = true
        Task {
            do {
                let response = try await openRouterService.findBugs(code: code)
                let aiResponse = AIResponse(text: response, isUser: false, timestamp: Date())
                aiResponses.append(aiResponse)
            } catch {
                let errorResponse = AIResponse(text: "Error: \(error.localizedDescription)", isUser: false, timestamp: Date())
                aiResponses.append(errorResponse)
            }
            isAiProcessing = false
        }
    }
    
    func optimizeCode() {
        isAiProcessing = true
        Task {
            do {
                let response = try await openRouterService.optimizeCode(code: code)
                let aiResponse = AIResponse(text: response, isUser: false, timestamp: Date())
                aiResponses.append(aiResponse)
            } catch {
                let errorResponse = AIResponse(text: "Error: \(error.localizedDescription)", isUser: false, timestamp: Date())
                aiResponses.append(errorResponse)
            }
            isAiProcessing = false
        }
    }
    
    func newFile() {
        code = DEFAULT_CODE
        currentFileName = "untitled.py"
        isModified = false
    }
    
    static let DEFAULT_CODE = """
    # Welcome to PyGenius AI for iOS!
    # Write your Python code here
    
    def greet(name):
        return f"Hello, {name}!"
    
    # Try the AI features!
    message = greet("iOS Developer")
    print(message)
    """
}
