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
