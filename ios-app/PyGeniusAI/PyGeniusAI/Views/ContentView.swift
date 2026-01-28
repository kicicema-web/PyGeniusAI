import SwiftUI

struct ContentView: View {
    @EnvironmentObject var viewModel: PyGeniusViewModel
    
    var body: some View {
        TabView(selection: $viewModel.selectedTab) {
            EditorView()
                .tabItem { Label("Editor", systemImage: "doc.text") }
                .tag(Tab.editor)
            
            ConsoleView()
                .tabItem { Label("Console", systemImage: "terminal") }
                .tag(Tab.console)
            
            AITutorView()
                .tabItem { Label("AI Tutor", systemImage: "brain") }
                .tag(Tab.aiTutor)
            
            SettingsView()
                .tabItem { Label("Settings", systemImage: "gear") }
                .tag(Tab.settings)
        }
        .accentColor(.purple)
    }
}
