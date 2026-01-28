import SwiftUI

struct ConsoleView: View {
    @EnvironmentObject var viewModel: PyGeniusViewModel
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                ScrollView {
                    LazyVStack(alignment: .leading, spacing: 4) {
                        ForEach(viewModel.consoleOutput) { line in
                            ConsoleLineView(line: line)
                        }
                    }
                    .padding()
                }
                .background(Color.black)
                .foregroundColor(.white)
                
                HStack {
                    Button(action: { viewModel.clearConsole() }) {
                        Label("Clear", systemImage: "trash")
                    }
                    Spacer()
                    if viewModel.isRunning {
                        ProgressView()
                    }
                }
                .padding()
                .background(Color(.systemGray6))
            }
            .navigationTitle("Console")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

struct ConsoleLineView: View {
    let line: ConsoleLine
    
    var body: some View {
        Text(line.text)
            .font(.system(.body, design: .monospaced))
            .foregroundColor(colorForType(line.type))
            .frame(maxWidth: .infinity, alignment: .leading)
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
