import Foundation

enum Tab: String, CaseIterable {
    case editor = "Editor"
    case console = "Console"
    case aiTutor = "AI Tutor"
    case settings = "Settings"
    
    var icon: String {
        switch self {
        case .editor: return "doc.text"
        case .console: return "terminal"
        case .aiTutor: return "brain"
        case .settings: return "gear"
        }
    }
}

struct ConsoleLine: Identifiable {
    let id = UUID()
    let text: String
    let type: LineType
    let timestamp: Date
}

enum LineType {
    case output
    case error
    case input
    case info
}

struct BugPrediction: Identifiable {
    let id = UUID()
    let line: Int
    let message: String
    let severity: Severity
    let fixSuggestion: String
}

enum Severity: String {
    case low = "LOW"
    case medium = "MEDIUM"
    case high = "HIGH"
    case critical = "CRITICAL"
}

struct AIResponse: Identifiable {
    let id = UUID()
    let text: String
    let isUser: Bool
    let timestamp: Date
}

enum ApiKeyStatus {
    case unknown
    case notConfigured
    case configured
}
