#!/usr/bin/env python3
"""
PyGenius AI - Desktop Edition v2.0
Modern Material Design interface with setup wizard
"""

import tkinter as tk
from tkinter import ttk, scrolledtext, messagebox, filedialog
import threading
import json
import os
import sys
import subprocess
import time

# API Configuration
OPENROUTER_API_KEY = "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"

# Modern Color Scheme - Material Design
COLORS = {
    'primary': '#6200EE',
    'primary_dark': '#3700B3',
    'primary_light': '#BB86FC',
    'secondary': '#03DAC6',
    'secondary_dark': '#018786',
    'background': '#121212',
    'surface': '#1E1E1E',
    'surface_light': '#2D2D2D',
    'error': '#CF6679',
    'on_primary': '#FFFFFF',
    'on_secondary': '#000000',
    'on_background': '#FFFFFF',
    'on_surface': '#FFFFFF',
    'on_error': '#000000',
    'success': '#4CAF50',
    'warning': '#FFC107',
    'info': '#2196F3',
}

class ModernStyle:
    """Modern Material Design styles for tkinter"""
    
    @staticmethod
    def configure_styles(style):
        """Configure ttk styles"""
        style.theme_use('clam')
        
        style.configure('.',
            background=COLORS['background'],
            foreground=COLORS['on_background'],
            fieldbackground=COLORS['surface'],
            troughcolor=COLORS['surface_light']
        )

class SetupWizard:
    """Setup wizard for first-time installation"""
    
    def __init__(self, parent, on_complete):
        self.parent = parent
        self.on_complete = on_complete
        self.window = tk.Toplevel(parent)
        self.window.title("PyGenius AI - Setup")
        self.window.geometry("700x500")
        self.window.configure(bg=COLORS['background'])
        self.window.transient(parent)
        self.window.grab_set()
        
        # Center window
        self.window.update_idletasks()
        x = (self.window.winfo_screenwidth() // 2) - (700 // 2)
        y = (self.window.winfo_screenheight() // 2) - (500 // 2)
        self.window.geometry(f"700x500+{x}+{y}")
        
        self.dependencies = {
            'requests': {'required': True, 'installed': False, 'description': 'HTTP library for AI API'},
            'pillow': {'required': False, 'installed': False, 'description': 'Image processing'},
            'numpy': {'required': False, 'installed': False, 'description': 'Numerical computing'},
        }
        
        self.create_ui()
        self.check_dependencies()
    
    def create_ui(self):
        """Create setup wizard UI"""
        # Header
        header = tk.Frame(self.window, bg=COLORS['primary'], height=80)
        header.pack(fill=tk.X)
        header.pack_propagate(False)
        
        tk.Label(header, text="PyGenius AI", font=('Segoe UI', 20, 'bold'),
                bg=COLORS['primary'], fg=COLORS['on_primary']).pack(side=tk.LEFT, padx=20, pady=15)
        
        # Main content
        self.content = tk.Frame(self.window, bg=COLORS['background'])
        self.content.pack(fill=tk.BOTH, expand=True, padx=30, pady=20)
        
        # Progress
        self.progress_var = tk.DoubleVar(value=0)
        self.progress = ttk.Progressbar(self.content, variable=self.progress_var,
                                       maximum=100, mode='determinate', length=600)
        self.progress.pack(fill=tk.X, pady=(0, 20))
        
        self.step_frame = tk.Frame(self.content, bg=COLORS['background'])
        self.step_frame.pack(fill=tk.BOTH, expand=True)
        
        # Buttons
        btn_frame = tk.Frame(self.content, bg=COLORS['background'])
        btn_frame.pack(fill=tk.X, pady=(20, 0))
        
        self.skip_btn = tk.Button(btn_frame, text="Skip", command=self.skip,
                                 bg=COLORS['surface'], fg=COLORS['on_surface'],
                                 font=('Segoe UI', 10), relief=tk.FLAT, padx=20, pady=8)
        self.skip_btn.pack(side=tk.RIGHT, padx=(10, 0))
        
        self.next_btn = tk.Button(btn_frame, text="Get Started", command=self.install_all,
                                 bg=COLORS['primary'], fg=COLORS['on_primary'],
                                 font=('Segoe UI', 10, 'bold'), relief=tk.FLAT, padx=25, pady=8)
        self.next_btn.pack(side=tk.RIGHT)
        
        self.show_welcome()
    
    def show_welcome(self):
        """Show welcome screen"""
        for w in self.step_frame.winfo_children():
            w.destroy()
        
        tk.Label(self.step_frame, text="Welcome!", font=('Segoe UI', 24, 'bold'),
                bg=COLORS['background'], fg=COLORS['on_background']).pack(pady=(30, 10))
        
        tk.Label(self.step_frame, text="Let's set up your Python coding environment.",
                font=('Segoe UI', 12), bg=COLORS['background'], fg=COLORS['primary_light']).pack()
        
        # Features
        features = [
            ("AI Tutor", "Get help from GPT-powered AI"),
            ("Code Editor", "Write and run Python code"),
            ("Bug Finder", "Detect and fix errors"),
        ]
        
        f_frame = tk.Frame(self.step_frame, bg=COLORS['background'])
        f_frame.pack(fill=tk.X, pady=30)
        
        for title, desc in features:
            card = tk.Frame(f_frame, bg=COLORS['surface'], padx=15, pady=15)
            card.pack(side=tk.LEFT, fill=tk.BOTH, expand=True, padx=5)
            tk.Label(card, text=title, font=('Segoe UI', 11, 'bold'),
                    bg=COLORS['surface'], fg=COLORS['on_surface']).pack()
            tk.Label(card, text=desc, font=('Segoe UI', 9),
                    bg=COLORS['surface'], fg='#888888').pack()
    
    def check_dependencies(self):
        """Check installed dependencies"""
        for pkg in self.dependencies:
            try:
                __import__(pkg if pkg != 'pillow' else 'PIL')
                self.dependencies[pkg]['installed'] = True
            except ImportError:
                pass
    
    def install_all(self):
        """Install dependencies"""
        self.next_btn.config(state=tk.DISABLED, text="Installing...")
        self.skip_btn.config(state=tk.DISABLED)
        threading.Thread(target=self._install_thread, daemon=True).start()
    
    def _install_thread(self):
        """Install in background"""
        for pkg_name, info in self.dependencies.items():
            if not info['installed']:
                try:
                    subprocess.run([sys.executable, "-m", "pip", "install", pkg_name, "--quiet"],
                                  capture_output=True, timeout=120)
                except:
                    pass
        self.window.after(0, self.finish)
    
    def skip(self):
        """Skip setup"""
        self.finish()
    
    def finish(self):
        """Complete setup"""
        config_dir = os.path.expanduser("~/.config/pygenius")
        os.makedirs(config_dir, exist_ok=True)
        with open(f"{config_dir}/setup_complete", "w") as f:
            f.write("1")
        self.window.destroy()
        self.on_complete()

class PyGeniusDesktop:
    """Main application"""
    
    def __init__(self, root):
        self.root = root
        self.root.title("PyGenius AI")
        self.root.geometry("1400x900")
        self.root.configure(bg=COLORS['background'])
        
        self.style = ttk.Style()
        ModernStyle.configure_styles(self.style)
        
        self.current_file = None
        self.console_namespace = {"__name__": "__console__"}
        
        self.create_ui()
        self.load_welcome()
    
    def create_ui(self):
        """Create modern UI"""
        # Main container
        main = tk.Frame(self.root, bg=COLORS['background'])
        main.pack(fill=tk.BOTH, expand=True)
        
        # Sidebar
        sidebar = tk.Frame(main, bg=COLORS['surface'], width=200)
        sidebar.pack(side=tk.LEFT, fill=tk.Y)
        sidebar.pack_propagate(False)
        
        # Logo
        logo = tk.Frame(sidebar, bg=COLORS['primary'], height=60)
        logo.pack(fill=tk.X)
        tk.Label(logo, text="PyGenius AI", font=('Segoe UI', 14, 'bold'),
                bg=COLORS['primary'], fg='white').pack(expand=True)
        
        # Menu buttons
        tk.Label(sidebar, text="MENU", font=('Segoe UI', 9),
                bg=COLORS['surface'], fg='#666').pack(anchor=tk.W, padx=15, pady=(20, 10))
        
        for icon, text, cmd in [
            ("Code", "Editor", lambda: self.show_frame('editor')),
            ("Console", "Console", lambda: self.show_frame('console')),
            ("AI", "AI Tutor", lambda: self.show_frame('ai')),
        ]:
            btn = tk.Button(sidebar, text=f"{icon} {text}", command=cmd,
                           bg=COLORS['surface'], fg='white',
                           font=('Segoe UI', 10), relief=tk.FLAT,
                           anchor=tk.W, padx=15, pady=8)
            btn.pack(fill=tk.X, padx=10, pady=2)
        
        # Content area
        self.content = tk.Frame(main, bg=COLORS['background'])
        self.content.pack(side=tk.LEFT, fill=tk.BOTH, expand=True, padx=10, pady=10)
        
        # Editor
        self.editor_frame = tk.Frame(self.content, bg=COLORS['background'])
        
        toolbar = tk.Frame(self.editor_frame, bg=COLORS['surface'], height=45)
        toolbar.pack(fill=tk.X, pady=(0, 10))
        toolbar.pack_propagate(False)
        
        self.file_label = tk.Label(toolbar, text="untitled.py",
                                  font=('Segoe UI', 10), bg=COLORS['surface'], fg='white')
        self.file_label.pack(side=tk.LEFT, padx=15)
        
        tk.Button(toolbar, text="Run", command=self.run_code,
                 bg=COLORS['success'], fg='white',
                 font=('Segoe UI', 9, 'bold'), relief=tk.FLAT, padx=15).pack(side=tk.RIGHT, padx=10, pady=8)
        
        # Editor with line numbers
        editor_container = tk.Frame(self.editor_frame, bg=COLORS['surface'])
        editor_container.pack(fill=tk.BOTH, expand=True)
        
        self.line_numbers = tk.Text(editor_container, width=4, padx=5, pady=10,
                                   bg=COLORS['surface'], fg='#666',
                                   font=('Consolas', 11), state='disabled', wrap=tk.NONE)
        self.line_numbers.pack(side=tk.LEFT, fill=tk.Y)
        
        self.code_editor = scrolledtext.ScrolledText(
            editor_container, wrap=tk.NONE, font=('Consolas', 11),
            bg=COLORS['surface'], fg='white',
            insertbackground=COLORS['primary'],
            padx=10, pady=10, undo=True
        )
        self.code_editor.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        self.code_editor.bind('<KeyRelease>', lambda e: self.update_lines())
        
        # Console
        self.console_frame = tk.Frame(self.content, bg=COLORS['background'])
        
        c_header = tk.Frame(self.console_frame, bg=COLORS['surface'], height=45)
        c_header.pack(fill=tk.X, pady=(0, 10))
        tk.Label(c_header, text="Console", font=('Segoe UI', 11, 'bold'),
                bg=COLORS['surface'], fg='white').pack(side=tk.LEFT, padx=15)
        tk.Button(c_header, text="Clear", command=self.clear_console,
                 bg=COLORS['surface_light'], fg='white', relief=tk.FLAT).pack(side=tk.RIGHT, padx=10)
        
        self.console = scrolledtext.ScrolledText(
            self.console_frame, wrap=tk.WORD, font=('Consolas', 10),
            bg='#0D1117', fg='#C9D1D9', state='disabled'
        )
        self.console.pack(fill=tk.BOTH, expand=True)
        
        # Input
        c_input = tk.Frame(self.console_frame, bg=COLORS['surface'], height=40)
        c_input.pack(fill=tk.X, pady=(10, 0))
        tk.Label(c_input, text=">>>", font=('Consolas', 11),
                bg=COLORS['surface'], fg=COLORS['primary']).pack(side=tk.LEFT, padx=10)
        self.c_input = tk.Entry(c_input, font=('Consolas', 11),
                               bg=COLORS['surface'], fg='white', relief=tk.FLAT)
        self.c_input.pack(side=tk.LEFT, fill=tk.X, expand=True, padx=5)
        self.c_input.bind('<Return>', lambda e: self.exec_console())
        tk.Button(c_input, text="Run", command=lambda: self.exec_console(),
                 bg=COLORS['primary'], fg='white', relief=tk.FLAT).pack(side=tk.RIGHT, padx=10)
        
        # AI Frame
        self.ai_frame = tk.Frame(self.content, bg=COLORS['background'])
        
        ai_header = tk.Frame(self.ai_frame, bg=COLORS['surface'], height=45)
        ai_header.pack(fill=tk.X, pady=(0, 10))
        tk.Label(ai_header, text="AI Tutor", font=('Segoe UI', 11, 'bold'),
                bg=COLORS['surface'], fg='white').pack(side=tk.LEFT, padx=15)
        
        ai_input = tk.Frame(self.ai_frame, bg=COLORS['surface'], height=50)
        ai_input.pack(fill=tk.X, pady=(0, 10))
        self.ai_entry = tk.Entry(ai_input, font=('Segoe UI', 11),
                                bg=COLORS['surface'], fg='white', relief=tk.FLAT)
        self.ai_entry.pack(side=tk.LEFT, fill=tk.X, expand=True, padx=15, pady=10)
        self.ai_entry.bind('<Return>', lambda e: self.ask_ai())
        tk.Button(ai_input, text="Ask", command=self.ask_ai,
                 bg=COLORS['primary'], fg='white', relief=tk.FLAT, padx=20).pack(side=tk.RIGHT, padx=15)
        
        # AI Quick buttons
        ai_btns = tk.Frame(self.ai_frame, bg=COLORS['background'])
        ai_btns.pack(fill=tk.X, pady=(0, 10))
        for text, cmd in [("Explain", self.explain_code), ("Find Bugs", self.find_bugs), ("Optimize", self.optimize_code)]:
            tk.Button(ai_btns, text=text, command=cmd,
                     bg=COLORS['surface'], fg='white', relief=tk.FLAT, padx=15).pack(side=tk.LEFT, padx=5)
        
        self.ai_output = scrolledtext.ScrolledText(
            self.ai_frame, wrap=tk.WORD, font=('Segoe UI', 10),
            bg=COLORS['surface'], fg='white', state='disabled'
        )
        self.ai_output.pack(fill=tk.BOTH, expand=True)
        
        # Status bar
        status = tk.Frame(self.root, bg=COLORS['surface_light'], height=25)
        status.pack(side=tk.BOTTOM, fill=tk.X)
        tk.Label(status, text="Ready", font=('Segoe UI', 9),
                bg=COLORS['surface_light'], fg='#888').pack(side=tk.LEFT, padx=15)
        
        self.show_frame('editor')
    
    def show_frame(self, name):
        """Show specific frame"""
        for f in [self.editor_frame, self.console_frame, self.ai_frame]:
            f.pack_forget()
        
        if name == 'editor':
            self.editor_frame.pack(fill=tk.BOTH, expand=True)
        elif name == 'console':
            self.console_frame.pack(fill=tk.BOTH, expand=True)
        elif name == 'ai':
            self.ai_frame.pack(fill=tk.BOTH, expand=True)
    
    def load_welcome(self):
        """Load welcome code"""
        code = '''# Welcome to PyGenius AI!
# Your Python coding assistant

def hello(name):
    return f"Hello, {name}!"

print(hello("World"))
'''
        self.code_editor.delete('1.0', tk.END)
        self.code_editor.insert('1.0', code)
        self.update_lines()
    
    def update_lines(self):
        """Update line numbers"""
        lines = self.code_editor.get('1.0', tk.END).count('\n')
        self.line_numbers.config(state='normal')
        self.line_numbers.delete('1.0', tk.END)
        self.line_numbers.insert('1.0', '\n'.join(str(i) for i in range(1, lines + 1)))
        self.line_numbers.config(state='disabled')
    
    def clear_console(self):
        """Clear console"""
        self.console.config(state='normal')
        self.console.delete('1.0', tk.END)
        self.console.config(state='disabled')
    
    def log_console(self, text):
        """Log to console"""
        self.console.config(state='normal')
        self.console.insert(tk.END, text + '\n')
        self.console.see(tk.END)
        self.console.config(state='disabled')
    
    def log_ai(self, text):
        """Log to AI"""
        self.ai_output.config(state='normal')
        self.ai_output.delete('1.0', tk.END)
        self.ai_output.insert(tk.END, text)
        self.ai_output.config(state='disabled')
    
    def exec_console(self):
        """Execute console input"""
        code = self.c_input.get().strip()
        if not code:
            return
        self.log_console(f">>> {code}")
        self.c_input.delete(0, tk.END)
        
        def run():
            try:
                try:
                    result = eval(code, self.console_namespace)
                    if result is not None:
                        self.root.after(0, lambda: self.log_console(str(result)))
                except SyntaxError:
                    exec(code, self.console_namespace)
            except Exception as e:
                self.root.after(0, lambda: self.log_console(f"Error: {e}"))
        
        threading.Thread(target=run, daemon=True).start()
    
    def run_code(self):
        """Run editor code"""
        code = self.code_editor.get('1.0', tk.END)
        self.clear_console()
        self.show_frame('console')
        self.log_console("Running...")
        
        def execute():
            import io
            from contextlib import redirect_stdout, redirect_stderr
            out = io.StringIO()
            try:
                with redirect_stdout(out), redirect_stderr(out):
                    exec(code, {"__name__": "__main__"})
                result = out.getvalue()
                self.root.after(0, lambda: self.log_console(result or "Done!"))
            except Exception as e:
                self.root.after(0, lambda: self.log_console(f"Error: {e}"))
        
        threading.Thread(target=execute, daemon=True).start()
    
    def call_ai(self, system, user):
        """Call OpenRouter API"""
        try:
            import requests
        except ImportError:
            return "Error: requests not installed"
        
        headers = {
            "Authorization": f"Bearer {OPENROUTER_API_KEY}",
            "Content-Type": "application/json",
        }
        data = {
            "model": "openai/gpt-3.5-turbo",
            "messages": [
                {"role": "system", "content": system},
                {"role": "user", "content": user}
            ],
            "temperature": 0.7,
            "max_tokens": 2000
        }
        
        try:
            resp = requests.post("https://openrouter.ai/api/v1/chat/completions",
                               headers=headers, json=data, timeout=60)
            return resp.json()["choices"][0]["message"]["content"]
        except Exception as e:
            return f"Error: {e}"
    
    def ask_ai(self):
        """Ask AI"""
        q = self.ai_entry.get().strip()
        if not q:
            return
        self.log_ai("Thinking...")
        self.show_frame('ai')
        
        def ask():
            code = self.code_editor.get('1.0', tk.END)[:1000]
            system = "You are a Python tutor. Be concise."
            user = f"Q: {q}\\nCode: {code}"
            resp = self.call_ai(system, user)
            self.root.after(0, lambda: self.log_ai(resp))
        
        threading.Thread(target=ask, daemon=True).start()
    
    def explain_code(self):
        """Explain code"""
        code = self.code_editor.get('1.0', tk.END).strip()
        self.log_ai("Analyzing...")
        self.show_frame('ai')
        
        def run():
            resp = self.call_ai("Explain Python code clearly.", f"Explain:\\n{code}")
            self.root.after(0, lambda: self.log_ai(resp))
        
        threading.Thread(target=run, daemon=True).start()
    
    def find_bugs(self):
        """Find bugs"""
        code = self.code_editor.get('1.0', tk.END).strip()
        self.log_ai("Checking for bugs...")
        self.show_frame('ai')
        
        def run():
            resp = self.call_ai("Find bugs in Python code.", f"Find bugs:\\n{code}")
            self.root.after(0, lambda: self.log_ai(resp))
        
        threading.Thread(target=run, daemon=True).start()
    
    def optimize_code(self):
        """Optimize code"""
        code = self.code_editor.get('1.0', tk.END).strip()
        self.log_ai("Optimizing...")
        self.show_frame('ai')
        
        def run():
            resp = self.call_ai("Optimize Python code.", f"Optimize:\\n{code}")
            self.root.after(0, lambda: self.log_ai(resp))
        
        threading.Thread(target=run, daemon=True).start()

def check_first_run():
    """Check first run"""
    config = os.path.expanduser("~/.config/pygenius/setup_complete")
    return not os.path.exists(config)

def main():
    """Main"""
    if check_first_run():
        root = tk.Tk()
        root.withdraw()
        SetupWizard(root, lambda: (root.destroy(), launch_app()))
        root.mainloop()
    else:
        launch_app()

def launch_app():
    """Launch main app"""
    root = tk.Tk()
    PyGeniusDesktop(root)
    root.mainloop()

if __name__ == "__main__":
    main()
