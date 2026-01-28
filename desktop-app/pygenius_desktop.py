#!/usr/bin/env python3
"""
PyGenius AI - Desktop Edition
A Python coding assistant with AI-powered features for Windows, Linux, and macOS
"""

import tkinter as tk
from tkinter import ttk, scrolledtext, messagebox, filedialog
import threading
import json
import os
import sys

# API Configuration
OPENROUTER_API_KEY = "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"

try:
    import requests
except ImportError:
    print("Installing required packages...")
    import subprocess
    subprocess.check_call([sys.executable, "-m", "pip", "install", "requests", "-q"])
    import requests


class PyGeniusDesktop:
    def __init__(self, root):
        self.root = root
        self.root.title("PyGenius AI - Desktop Edition")
        self.root.geometry("1200x800")
        self.root.minsize(1000, 600)
        
        # Set icon if available
        try:
            self.root.iconbitmap("pygenius.ico")
        except:
            pass
        
        # Configure styles
        self.configure_styles()
        
        # Create UI
        self.create_menu()
        self.create_main_interface()
        
        # Current file
        self.current_file = None
        self.is_modified = False
        
        # Execution namespace for console
        self.console_namespace = {"__name__": "__console__"}
        
        # Bind keyboard shortcuts
        self.bind_shortcuts()
        
    def configure_styles(self):
        """Configure ttk styles"""
        style = ttk.Style()
        style.theme_use('clam')
        
        # Colors
        self.bg_color = "#1e1e1e"
        self.fg_color = "#d4d4d4"
        self.accent_color = "#007acc"
        self.success_color = "#4ec9b0"
        self.warning_color = "#ce9178"
        
        self.root.configure(bg=self.bg_color)
        
    def create_menu(self):
        """Create menu bar"""
        menubar = tk.Menu(self.root)
        self.root.config(menu=menubar)
        
        # File menu
        file_menu = tk.Menu(menubar, tearoff=0)
        menubar.add_cascade(label="File", menu=file_menu)
        file_menu.add_command(label="New (Ctrl+N)", command=self.new_file)
        file_menu.add_command(label="Open (Ctrl+O)", command=self.open_file)
        file_menu.add_command(label="Save (Ctrl+S)", command=self.save_file)
        file_menu.add_command(label="Save As", command=self.save_as_file)
        file_menu.add_separator()
        file_menu.add_command(label="Exit", command=self.root.quit)
        
        # Run menu
        run_menu = tk.Menu(menubar, tearoff=0)
        menubar.add_cascade(label="Run", menu=run_menu)
        run_menu.add_command(label="Run Code (F5)", command=self.run_code)
        
        # AI menu
        ai_menu = tk.Menu(menubar, tearoff=0)
        menubar.add_cascade(label="AI", menu=ai_menu)
        ai_menu.add_command(label="Ask AI Tutor", command=self.ask_ai)
        ai_menu.add_command(label="Explain Code", command=self.explain_code)
        ai_menu.add_command(label="Find Bugs", command=self.find_bugs)
        ai_menu.add_command(label="Optimize Code", command=self.optimize_code)
        
    def create_main_interface(self):
        """Create the main UI layout"""
        # Main container
        main_frame = ttk.Frame(self.root)
        main_frame.pack(fill=tk.BOTH, expand=True, padx=5, pady=5)
        
        # Create paned window for resizable panels
        paned = ttk.PanedWindow(main_frame, orient=tk.HORIZONTAL)
        paned.pack(fill=tk.BOTH, expand=True)
        
        # Left panel - Code Editor
        left_frame = ttk.Frame(paned)
        paned.add(left_frame, weight=3)
        
        # Editor header
        editor_header = ttk.Frame(left_frame)
        editor_header.pack(fill=tk.X, pady=(0, 5))
        
        ttk.Label(editor_header, text="📝 Code Editor", font=('Segoe UI', 10, 'bold')).pack(side=tk.LEFT)
        self.file_label = ttk.Label(editor_header, text="untitled.py")
        self.file_label.pack(side=tk.RIGHT)
        
        # Code editor
        self.editor_frame = ttk.Frame(left_frame)
        self.editor_frame.pack(fill=tk.BOTH, expand=True)
        
        # Line numbers
        self.line_numbers = tk.Text(
            self.editor_frame,
            width=4,
            padx=4,
            pady=5,
            bg="#252526",
            fg="#858585",
            font=('Consolas', 11),
            state='disabled',
            wrap=tk.NONE
        )
        self.line_numbers.pack(side=tk.LEFT, fill=tk.Y)
        
        # Main code editor
        self.code_editor = scrolledtext.ScrolledText(
            self.editor_frame,
            wrap=tk.NONE,
            font=('Consolas', 11),
            bg="#1e1e1e",
            fg="#d4d4d4",
            insertbackground="#d4d4d4",
            selectbackground="#264f78",
            padx=10,
            pady=5,
            undo=True
        )
        self.code_editor.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        self.code_editor.bind('<KeyRelease>', self.on_editor_change)
        self.code_editor.bind('<Return>', self.update_line_numbers)
        
        # Default code
        self.code_editor.insert('1.0', '''# Welcome to PyGenius AI Desktop!
# Write your Python code here and run it

def hello_world():
    print("Hello, World!")
    return "Welcome to PyGenius AI"

# Run your code with F5 or Run menu
result = hello_world()
print(result)
''')
        self.update_line_numbers()
        
        # Right panel - Console and AI
        right_frame = ttk.Frame(paned)
        paned.add(right_frame, weight=2)
        
        # Notebook for tabs
        self.notebook = ttk.Notebook(right_frame)
        self.notebook.pack(fill=tk.BOTH, expand=True)
        
        # Console tab
        console_frame = ttk.Frame(self.notebook)
        self.notebook.add(console_frame, text="💻 Console")
        
        console_header = ttk.Frame(console_frame)
        console_header.pack(fill=tk.X, pady=(0, 5))
        ttk.Label(console_header, text="Output:", font=('Segoe UI', 9, 'bold')).pack(side=tk.LEFT)
        ttk.Button(console_header, text="Clear", command=self.clear_console).pack(side=tk.RIGHT)
        ttk.Button(console_header, text="Run (F5)", command=self.run_code).pack(side=tk.RIGHT, padx=5)
        
        # Console output
        self.console = scrolledtext.ScrolledText(
            console_frame,
            wrap=tk.WORD,
            font=('Consolas', 10),
            bg="#0c0c0c",
            fg="#cccccc",
            state='disabled',
            height=12
        )
        self.console.pack(fill=tk.BOTH, expand=True)
        
        # Console input frame
        input_frame = ttk.Frame(console_frame)
        input_frame.pack(fill=tk.X, pady=(5, 0))
        
        ttk.Label(input_frame, text=">>>", font=('Consolas', 10)).pack(side=tk.LEFT, padx=(0, 5))
        
        self.console_input = ttk.Entry(input_frame, font=('Consolas', 10))
        self.console_input.pack(side=tk.LEFT, fill=tk.X, expand=True, padx=(0, 5))
        self.console_input.bind('<Return>', self.on_console_input)
        
        ttk.Button(input_frame, text="Execute", command=lambda: self.on_console_input(None)).pack(side=tk.RIGHT)
        
        # AI Tutor tab
        ai_frame = ttk.Frame(self.notebook)
        self.notebook.add(ai_frame, text="🤖 AI Tutor")
        
        ai_header = ttk.Frame(ai_frame)
        ai_header.pack(fill=tk.X, pady=(0, 5))
        ttk.Label(ai_header, text="Ask the AI:", font=('Segoe UI', 9, 'bold')).pack(side=tk.LEFT)
        
        self.ai_input = ttk.Entry(ai_frame, font=('Segoe UI', 10))
        self.ai_input.pack(fill=tk.X, pady=(0, 5))
        self.ai_input.bind('<Return>', lambda e: self.ask_ai())
        
        ai_buttons = ttk.Frame(ai_frame)
        ai_buttons.pack(fill=tk.X, pady=(0, 5))
        ttk.Button(ai_buttons, text="Ask", command=self.ask_ai).pack(side=tk.LEFT, padx=2)
        ttk.Button(ai_buttons, text="Explain Code", command=self.explain_code).pack(side=tk.LEFT, padx=2)
        ttk.Button(ai_buttons, text="Find Bugs", command=self.find_bugs).pack(side=tk.LEFT, padx=2)
        ttk.Button(ai_buttons, text="Optimize", command=self.optimize_code).pack(side=tk.LEFT, padx=2)
        
        self.ai_output = scrolledtext.ScrolledText(
            ai_frame,
            wrap=tk.WORD,
            font=('Segoe UI', 10),
            bg="#1e1e1e",
            fg="#d4d4d4",
            state='disabled',
            height=15
        )
        self.ai_output.pack(fill=tk.BOTH, expand=True)
        
        # Status bar
        self.status_bar = ttk.Label(
            self.root,
            text="Ready | AI: Connected | Type in console and press Enter to execute",
            relief=tk.SUNKEN,
            anchor=tk.W
        )
        self.status_bar.pack(fill=tk.X, side=tk.BOTTOM)
        
    def on_console_input(self, event):
        """Handle console input"""
        code = self.console_input.get().strip()
        if not code:
            return
        
        # Show input in console
        self.log_to_console(f">>> {code}", "input")
        self.console_input.delete(0, tk.END)
        
        # Execute in separate thread
        def execute():
            try:
                # Try to evaluate first (for expressions)
                try:
                    result = eval(code, self.console_namespace)
                    if result is not None:
                        self.root.after(0, lambda: self.log_to_console(str(result), "output"))
                except SyntaxError:
                    # If eval fails, use exec (for statements)
                    exec(code, self.console_namespace)
                except Exception as e:
                    self.root.after(0, lambda: self.log_to_console(f"Error: {str(e)}", "error"))
            except Exception as e:
                self.root.after(0, lambda: self.log_to_console(f"Error: {str(e)}", "error"))
        
        threading.Thread(target=execute, daemon=True).start()
        
    def update_line_numbers(self, event=None):
        """Update line numbers"""
        line_count = self.code_editor.get('1.0', tk.END).count('\n')
        line_numbers_text = '\n'.join(str(i) for i in range(1, line_count + 1))
        self.line_numbers.config(state='normal')
        self.line_numbers.delete('1.0', tk.END)
        self.line_numbers.insert('1.0', line_numbers_text)
        self.line_numbers.config(state='disabled')
        return None if event is None else "break"
        
    def on_editor_change(self, event=None):
        """Handle editor changes"""
        self.is_modified = True
        self.update_line_numbers()
        
    def bind_shortcuts(self):
        """Bind keyboard shortcuts"""
        self.root.bind('<Control-n>', lambda e: self.new_file())
        self.root.bind('<Control-o>', lambda e: self.open_file())
        self.root.bind('<Control-s>', lambda e: self.save_file())
        self.root.bind('<F5>', lambda e: self.run_code())
        
    def new_file(self):
        """Create new file"""
        if self.is_modified:
            if messagebox.askyesno("Save Changes?", "Do you want to save changes?"):
                self.save_file()
        self.code_editor.delete('1.0', tk.END)
        self.current_file = None
        self.file_label.config(text="untitled.py")
        self.is_modified = False
        
    def open_file(self):
        """Open file dialog"""
        file_path = filedialog.askopenfilename(
            defaultextension=".py",
            filetypes=[("Python files", "*.py"), ("All files", "*.*")]
        )
        if file_path:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            self.code_editor.delete('1.0', tk.END)
            self.code_editor.insert('1.0', content)
            self.current_file = file_path
            self.file_label.config(text=os.path.basename(file_path))
            self.is_modified = False
            self.update_line_numbers()
            
    def save_file(self):
        """Save current file"""
        if self.current_file:
            with open(self.current_file, 'w', encoding='utf-8') as f:
                f.write(self.code_editor.get('1.0', tk.END))
            self.is_modified = False
            self.status_bar.config(text=f"Saved: {self.current_file}")
        else:
            self.save_as_file()
            
    def save_as_file(self):
        """Save as dialog"""
        file_path = filedialog.asksaveasfilename(
            defaultextension=".py",
            filetypes=[("Python files", "*.py"), ("All files", "*.*")]
        )
        if file_path:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(self.code_editor.get('1.0', tk.END))
            self.current_file = file_path
            self.file_label.config(text=os.path.basename(file_path))
            self.is_modified = False
            
    def clear_console(self):
        """Clear console output"""
        self.console.config(state='normal')
        self.console.delete('1.0', tk.END)
        self.console.config(state='disabled')
        # Clear namespace
        self.console_namespace = {"__name__": "__console__"}
        
    def log_to_console(self, text, tag=""):
        """Add text to console"""
        self.console.config(state='normal')
        self.console.insert(tk.END, text + "\n", tag)
        self.console.see(tk.END)
        self.console.config(state='disabled')
        
    def log_to_ai(self, text):
        """Add text to AI output"""
        self.ai_output.config(state='normal')
        self.ai_output.delete('1.0', tk.END)
        self.ai_output.insert(tk.END, text)
        self.ai_output.see(tk.END)
        self.ai_output.config(state='disabled')
        
    def run_code(self):
        """Execute Python code from editor"""
        code = self.code_editor.get('1.0', tk.END)
        self.clear_console()
        self.log_to_console("=== Running Python Code ===\n")
        
        # Run in separate thread to avoid freezing UI
        def execute():
            import io
            import contextlib
            
            output = io.StringIO()
            try:
                with contextlib.redirect_stdout(output), contextlib.redirect_stderr(output):
                    exec(code, {"__name__": "__main__"})
                result = output.getvalue()
                if result:
                    self.root.after(0, lambda: self.log_to_console(result))
                else:
                    self.root.after(0, lambda: self.log_to_console("(Code executed successfully with no output)"))
            except Exception as e:
                self.root.after(0, lambda: self.log_to_console(f"Error: {str(e)}"))
                
        threading.Thread(target=execute, daemon=True).start()
        
    def call_openrouter(self, system_prompt, user_prompt):
        """Call OpenRouter API"""
        headers = {
            "Authorization": f"Bearer {OPENROUTER_API_KEY}",
            "Content-Type": "application/json",
            "HTTP-Referer": "https://pygenius.ai",
            "X-Title": "PyGenius AI Desktop"
        }
        
        data = {
            "model": "openai/gpt-3.5-turbo",
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            "temperature": 0.7,
            "max_tokens": 2000
        }
        
        try:
            response = requests.post(
                "https://openrouter.ai/api/v1/chat/completions",
                headers=headers,
                json=data,
                timeout=60
            )
            response.raise_for_status()
            return response.json()["choices"][0]["message"]["content"]
        except Exception as e:
            return f"Error: {str(e)}\n\nPlease check your internet connection."
            
    def ask_ai(self):
        """Ask AI tutor"""
        question = self.ai_input.get().strip()
        if not question:
            messagebox.showinfo("Ask AI", "Please enter a question first!")
            return
            
        code = self.code_editor.get('1.0', tk.END)
        self.log_to_ai("Thinking...")
        self.notebook.select(1)  # Switch to AI tab
        
        def ask():
            system = "You are PyGenius AI, a helpful Python programming tutor. Be concise but thorough. Provide code examples when helpful."
            user = f"Question: {question}\n\nCurrent code context:\n```python\n{code[:1000]}\n```"
            response = self.call_openrouter(system, user)
            self.root.after(0, lambda: self.log_to_ai(response))
            
        threading.Thread(target=ask, daemon=True).start()
        
    def explain_code(self):
        """Explain current code"""
        code = self.code_editor.get('1.0', tk.END).strip()
        if not code:
            messagebox.showinfo("Explain Code", "Please write some code first!")
            return
            
        self.log_to_ai("Analyzing your code...")
        self.notebook.select(1)
        
        def explain():
            system = """You are a Python code explainer. Explain the provided Python code clearly and concisely.
Break down:
1. What the code does overall
2. Key concepts used
3. Important functions/classes
4. Any potential issues or improvements
Use emoji icons to make it engaging."""
            user = f"Please explain this Python code:\n```python\n{code}\n```"
            response = self.call_openrouter(system, user)
            self.root.after(0, lambda: self.log_to_ai(response))
            
        threading.Thread(target=explain, daemon=True).start()
        
    def find_bugs(self):
        """Find bugs in code"""
        code = self.code_editor.get('1.0', tk.END).strip()
        if not code:
            messagebox.showinfo("Find Bugs", "Please write some code first!")
            return
            
        self.log_to_ai("Analyzing for bugs...")
        self.notebook.select(1)
        
        def analyze():
            system = """You are a Python code reviewer. Analyze the code for bugs, issues, and improvements.
Check for:
- Syntax errors
- Logic bugs
- Performance issues
- Security concerns
- Best practice violations

Provide specific line numbers and fix suggestions."""
            user = f"Please analyze this Python code:\n```python\n{code}\n```"
            response = self.call_openrouter(system, user)
            self.root.after(0, lambda: self.log_to_ai(response))
            
        threading.Thread(target=analyze, daemon=True).start()
        
    def optimize_code(self):
        """Optimize current code"""
        code = self.code_editor.get('1.0', tk.END).strip()
        if not code:
            messagebox.showinfo("Optimize Code", "Please write some code first!")
            return
            
        self.log_to_ai("Analyzing for optimizations...")
        self.notebook.select(1)
        
        def optimize():
            system = """You are a Python optimization expert. Analyze the provided code and suggest optimizations for:
- Performance
- Readability
- Pythonic style
- Memory usage

Provide the optimized code with comments explaining the changes."""
            user = f"Please optimize this Python code:\n```python\n{code}\n```"
            response = self.call_openrouter(system, user)
            self.root.after(0, lambda: self.log_to_ai(response))
            
        threading.Thread(target=optimize, daemon=True).start()


def main():
    root = tk.Tk()
    app = PyGeniusDesktop(root)
    root.mainloop()


if __name__ == "__main__":
    main()
