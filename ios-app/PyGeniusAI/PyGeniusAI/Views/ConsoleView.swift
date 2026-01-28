import SwiftUI

struct ConsoleView: View {
    @EnvironmentObject var viewModel: PyGeniusViewModel
    @State private var inputText: String = ""
    @FocusState private var isInputFocused: Bool
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Console Output
                ScrollViewReader { proxy in
                    ScrollView {
                        LazyVStack(alignment: .leading, spacing: 4) {
                            ForEach(viewModel.consoleOutput) { line in
                                ConsoleLineView(line: line)
                                    .id(line.id)
                            }
                        }
                        .padding()
                    }
                    .background(Color.black)
                    .foregroundColor(.white)
                    .onChange(of: viewModel.consoleOutput.count) { _ in
                        if let last = viewModel.consoleOutput.last {
                            withAnimation {
                                proxy.scrollTo(last.id, anchor: .bottom)
                            }
                        }
                    }
                }
                
                // Input Area
                VStack(spacing: 0) {
                    Divider()
                    
                    HStack(spacing: 8) {
                        TextField(">>>", text: $inputText)
                            .font(.system(.body, design: .monospaced))
                            .textFieldStyle(PlainTextFieldStyle())
                            .padding(8)
                            .background(Color(.systemGray6))
                            .cornerRadius(8)
                            .focused($isInputFocused)
                            .onSubmit {
                                submitInput()
                            }
                        
                        Button(action: submitInput) {
                            Image(systemName: "return")
                                .font(.system(size: 20, weight: .semibold))
                        }
                        .disabled(inputText.isEmpty)
                        .buttonStyle(.borderedProminent)
                        .tint(.green)
                    }
                    .padding(.horizontal)
                    .padding(.vertical, 8)
                }
                .background(Color(.systemBackground))
            }
            .navigationTitle("Console")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { viewModel.clearConsole() }) {
                        Image(systemName: "trash")
                    }
                }
                ToolbarItem(placement: .navigationBarLeading) {
                    if viewModel.isRunning {
                        ProgressView()
                    }
                }
            }
        }
    }
    
    private func submitInput() {
        guard !inputText.isEmpty else { return }
        viewModel.executeConsoleInput(inputText)
        inputText = ""
        isInputFocused = true
    }
}

struct ConsoleLineView: View {
    let line: ConsoleLine
    
    var body: some View {
        Text(line.text)
            .font(.system(.body, design: .monospaced))
            .foregroundColor(colorForType(line.type))
            .frame(maxWidth: .infinity, alignment: .leading)
            .textSelection(.enabled)
    }
    
    func colorForType(_ type: LineType) -> Color {
        switch type {
        case .output: return .white
        case .error: return .red
        case .input: return .green
        case .info: return .yellow
        }
    }
}
