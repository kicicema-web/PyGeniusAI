import SwiftUI

@main
struct PyGeniusAIApp: App {
    @StateObject private var viewModel = PyGeniusViewModel()
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(viewModel)
        }
    }
}
