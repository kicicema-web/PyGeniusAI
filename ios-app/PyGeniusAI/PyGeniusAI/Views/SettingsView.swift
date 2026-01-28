import SwiftUI

struct SettingsView: View {
    @EnvironmentObject var viewModel: PyGeniusViewModel
    
    var body: some View {
        NavigationView {
            List {
                Section(header: Text("AI Configuration")) {
                    HStack {
                        Image(systemName: "checkmark.circle.fill")
                            .foregroundColor(.green)
                        Text("OpenRouter AI")
                        Spacer()
                        Text("Connected")
                            .foregroundColor(.secondary)
                    }
                    Text("AI features are pre-configured and ready to use!")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                
                Section(header: Text("Available AI Features")) {
                    FeatureRow(icon: "brain", title: "AI Tutor", description: "Ask Python questions")
                    FeatureRow(icon: "text.magnifyingglass", title: "Code Explanation", description: "Understand any code")
                    FeatureRow(icon: "ladybug", title: "Bug Detection", description: "Find issues early")
                    FeatureRow(icon: "wand.and.stars", title: "Code Optimization", description: "Improve performance")
                }
                
                Section(header: Text("About")) {
                    HStack {
                        Text("Version")
                        Spacer()
                        Text("1.0.1")
                            .foregroundColor(.secondary)
                    }
                    HStack {
                        Text("AI Provider")
                        Spacer()
                        Text("OpenRouter (GPT-3.5)")
                            .foregroundColor(.secondary)
                    }
                }
            }
            .navigationTitle("Settings")
        }
    }
}

struct FeatureRow: View {
    let icon: String
    let title: String
    let description: String
    
    var body: some View {
        HStack {
            Image(systemName: icon)
                .frame(width: 30)
                .foregroundColor(.purple)
            VStack(alignment: .leading) {
                Text(title)
                    .font(.body)
                Text(description)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding(.vertical, 4)
    }
}
