import SwiftUI

struct AITutorView: View {
    @EnvironmentObject var viewModel: PyGeniusViewModel
    @State private var question: String = ""
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                HStack {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundColor(.green)
                    Text("AI Ready - Powered by OpenRouter")
                        .font(.caption)
                    Spacer()
                }
                .padding()
                .background(Color.green.opacity(0.1))
                
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(viewModel.aiResponses) { response in
                            AIResponseView(response: response)
                        }
                    }
                    .padding()
                }
                
                VStack(spacing: 8) {
                    if viewModel.isAiProcessing {
                        ProgressView("AI is thinking...")
                            .padding(.vertical, 4)
                    }
                    
                    HStack(spacing: 12) {
                        TextField("Ask about Python...", text: $question)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                        
                        Button(action: {
                            viewModel.askAI(question)
                            question = ""
                        }) {
                            Image(systemName: "paperplane.fill")
                        }
                        .disabled(question.isEmpty || viewModel.isAiProcessing)
                    }
                    
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            QuickActionButton(title: "Explain Code", icon: "text.magnifyingglass") {
                                viewModel.explainCode()
                            }
                            QuickActionButton(title: "Find Bugs", icon: "ladybug") {
                                viewModel.findBugs()
                            }
                            QuickActionButton(title: "Optimize", icon: "wand.and.stars") {
                                viewModel.optimizeCode()
                            }
                        }
                    }
                }
                .padding()
                .background(Color(.systemGray6))
            }
            .navigationTitle("AI Tutor")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

struct AIResponseView: View {
    let response: AIResponse
    
    var body: some View {
        HStack {
            if response.isUser {
                Spacer()
                Text(response.text)
                    .padding()
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(12)
            } else {
                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Image(systemName: "brain")
                            .foregroundColor(.purple)
                        Text("PyGenius AI")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    Text(response.text)
                        .padding()
                        .background(Color(.systemGray5))
                        .cornerRadius(12)
                }
                Spacer()
            }
        }
    }
}

struct QuickActionButton: View {
    let title: String
    let icon: String
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Label(title, systemImage: icon)
                .font(.caption)
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(Color.purple.opacity(0.1))
                .foregroundColor(.purple)
                .cornerRadius(8)
        }
    }
}
