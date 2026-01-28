import SwiftUI

struct EditorView: View {
    @EnvironmentObject var viewModel: PyGeniusViewModel
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                HStack {
                    Text(viewModel.currentFileName)
                        .font(.caption)
                        .foregroundColor(.secondary)
                    if viewModel.isModified {
                        Text("•").foregroundColor(.orange)
                    }
                    Spacer()
                    Button(action: { viewModel.newFile() }) {
                        Image(systemName: "doc.badge.plus")
                    }
                    Button(action: { viewModel.runCode() }) {
                        HStack {
                            Image(systemName: "play.fill")
                            Text("Run")
                        }
                        .foregroundColor(.green)
                    }
                }
                .padding(.horizontal)
                .padding(.vertical, 8)
                .background(Color(.systemGray6))
                
                TextEditor(text: Binding(
                    get: { viewModel.code },
                    set: { viewModel.updateCode($0) }
                ))
                .font(.system(.body, design: .monospaced))
                .padding(8)
                
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 12) {
                        Button(action: { viewModel.explainCode() }) {
                            Label("Explain", systemImage: "text.magnifyingglass").font(.caption)
                        }
                        .buttonStyle(.bordered)
                        .tint(.blue)
                        
                        Button(action: { viewModel.findBugs() }) {
                            Label("Find Bugs", systemImage: "ladybug").font(.caption)
                        }
                        .buttonStyle(.bordered)
                        .tint(.red)
                        
                        Button(action: { viewModel.optimizeCode() }) {
                            Label("Optimize", systemImage: "wand.and.stars").font(.caption)
                        }
                        .buttonStyle(.bordered)
                        .tint(.orange)
                    }
                    .padding(.horizontal)
                }
                .padding(.vertical, 8)
                .background(Color(.systemGray6))
            }
            .navigationTitle("Code Editor")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}
