"""
PyGenius AI Runtime - Python execution environment with AI hooks
"""
import sys
import io
import traceback
import json
import subprocess
import pkg_resources
from contextlib import redirect_stdout, redirect_stderr

class PyGeniusOutput:
    """Custom stdout/stderr capture with AI analysis hooks"""
    
    def __init__(self, callback=None):
        self.callback = callback
        self.output_buffer = []
        
    def write(self, text):
        if text.strip():
            self.output_buffer.append(text)
            if self.callback:
                self.callback(text, "output")
                
    def flush(self):
        pass
        
    def getvalue(self):
        return "".join(self.output_buffer)


def execute_code(code, callback=None):
    """
    Execute Python code with enhanced output capture and AI analysis
    
    Args:
        code: Python code string to execute
        callback: Function called with (line, line_type) for each output
        
    Returns:
        Execution result as string
    """
    # Create isolated namespace
    namespace = {
        '__name__': '__main__',
        '__builtins__': __builtins__,
    }
    
    # Import common libraries
    try:
        import numpy as np
        namespace['np'] = np
    except ImportError:
        pass
        
    try:
        import matplotlib.pyplot as plt
        namespace['plt'] = plt
    except ImportError:
        pass
        
    try:
        import pandas as pd
        namespace['pd'] = pd
    except ImportError:
        pass
    
    # Capture output
    stdout_capture = PyGeniusOutput(callback)
    stderr_capture = PyGeniusOutput(callback)
    
    result_lines = []
    
    try:
        # Compile the code to check for syntax errors
        compiled = compile(code, '<string>', 'exec')
        
        # Execute with captured output
        with redirect_stdout(stdout_capture), redirect_stderr(stderr_capture):
            exec(compiled, namespace)
            
        # Get results
        stdout_text = stdout_capture.getvalue()
        stderr_text = stderr_capture.getvalue()
        
        if stdout_text:
            result_lines.append(stdout_text)
        if stderr_text:
            result_lines.append(f"STDERR: {stderr_text}")
            
        # AI Analysis - check variable states
        if callback:
            for var_name, var_value in namespace.items():
                if not var_name.startswith('_') and var_name not in ['np', 'plt', 'pd']:
                    var_type = type(var_value).__name__
                    if var_type in ['ndarray', 'DataFrame', 'Series']:
                        shape_info = f"shape={var_value.shape}" if hasattr(var_value, 'shape') else ""
                        callback(f"📊 Variable '{var_name}': {var_type} {shape_info}", "progress")
                    elif var_type in ['list', 'dict', 'set']:
                        size = len(var_value)
                        callback(f"📦 Variable '{var_name}': {var_type} with {size} items", "progress")
        
        return "\n".join(result_lines) if result_lines else "Code executed successfully (no output)"
        
    except SyntaxError as e:
        error_msg = f"SyntaxError: {e.msg} at line {e.lineno}"
        if callback:
            callback(error_msg, "error")
        return error_msg
        
    except Exception as e:
        error_msg = f"{type(e).__name__}: {str(e)}"
        tb = traceback.format_exc()
        if callback:
            callback(error_msg, "error")
            callback(tb, "error")
        return f"{error_msg}\n{tb}"


def pip_install(package_name):
    """Install a Python package using pip"""
    try:
        result = subprocess.run(
            [sys.executable, "-m", "pip", "install", package_name],
            capture_output=True,
            text=True,
            timeout=120
        )
        if result.returncode == 0:
            return f"✓ Successfully installed {package_name}"
        else:
            return f"✗ Failed to install {package_name}:\n{result.stderr}"
    except Exception as e:
        return f"✗ Error installing {package_name}: {str(e)}"


def pip_uninstall(package_name):
    """Uninstall a Python package using pip"""
    try:
        result = subprocess.run(
            [sys.executable, "-m", "pip", "uninstall", "-y", package_name],
            capture_output=True,
            text=True,
            timeout=60
        )
        if result.returncode == 0:
            return f"✓ Successfully uninstalled {package_name}"
        else:
            return f"✗ Failed to uninstall {package_name}"
    except Exception as e:
        return f"✗ Error uninstalling {package_name}: {str(e)}"


def list_packages():
    """List installed Python packages"""
    try:
        installed = sorted(
            [{"name": d.project_name, "version": d.version} 
             for d in pkg_resources.working_set],
            key=lambda x: x["name"].lower()
        )
        return installed
    except Exception as e:
        return [{"name": "Error", "version": str(e)}]


def analyze_code_for_bugs(code):
    """
    AI-powered static analysis for common Python bugs
    Returns list of potential issues
    """
    issues = []
    lines = code.split('\n')
    
    for i, line in enumerate(lines, 1):
        line_stripped = line.strip()
        
        # Check for division that could cause ZeroDivisionError
        if '/' in line and '= 0' not in line:
            if any(x in line for x in ['/0', '/ 0', '/variable', '/ value']):
                issues.append({
                    'line': i,
                    'type': 'warning',
                    'message': 'Possible ZeroDivisionError - ensure divisor is not zero',
                    'severity': 'high'
                })
        
        # Check for unused variables (simple heuristic)
        if '=' in line and '==' not in line:
            var_name = line.split('=')[0].strip()
            # Check if variable is used later
            used = False
            for j in range(i, len(lines)):
                if var_name in lines[j] and lines[j].find('=') != lines[j].find(var_name):
                    used = True
                    break
            if not used and var_name.isidentifier():
                issues.append({
                    'line': i,
                    'type': 'info',
                    'message': f"Variable '{var_name}' may be unused (dead code)",
                    'severity': 'low'
                })
        
        # Check for mutable default arguments
        if 'def ' in line and '=' in line:
            if '[]' in line or '{}' in line:
                issues.append({
                    'line': i,
                    'type': 'warning',
                    'message': 'Mutable default argument detected - use None and initialize inside function',
                    'severity': 'medium'
                })
        
        # Check for bare except
        if 'except:' in line and 'Exception' not in line:
            issues.append({
                'line': i,
                'type': 'warning',
                'message': 'Bare except clause - catch specific exceptions instead',
                'severity': 'medium'
            })
    
    return issues


def explain_error(error_message, code_context):
    """
    Generate AI explanation for Python errors
    """
    explanations = {
        'IndexError': "You're trying to access an index that doesn't exist in the list/array. Remember: Python uses 0-based indexing, so a list with 5 items has indices 0-4.",
        'KeyError': "The dictionary doesn't have this key. Use .get() method or check if key exists with 'in' operator.",
        'TypeError': "You're using an operation with incompatible types. Check variable types with type().",
        'ValueError': "The value is correct type but inappropriate value. Check the documentation for valid values.",
        'NameError': "This variable or function hasn't been defined. Check spelling and scope.",
        'ZeroDivisionError': "Cannot divide by zero. Add a check before division.",
        'AttributeError': "This object doesn't have the attribute/method you're trying to use. Check the object's type.",
        'ImportError': "Cannot import this module. Check if it's installed: pip install <module>",
        'ModuleNotFoundError': "Python can't find this module. You may need to install it first.",
    }
    
    for error_type, explanation in explanations.items():
        if error_type in error_message:
            return {
                'error_type': error_type,
                'explanation': explanation,
                'suggestion': get_fix_suggestion(error_type, code_context)
            }
    
    return {
        'error_type': 'Unknown',
        'explanation': 'An unexpected error occurred. Check your code syntax and logic.',
        'suggestion': 'Try running the code line by line to identify the issue.'
    }


def get_fix_suggestion(error_type, code_context):
    """Get fix suggestion based on error type"""
    suggestions = {
        'IndexError': 'Add bounds checking: if index < len(your_list):',
        'KeyError': 'Use: your_dict.get(key, default_value)',
        'ZeroDivisionError': 'Add check: if divisor != 0:',
        'NameError': 'Define the variable before using it, or import the module',
        'AttributeError': 'Check object type or use hasattr(obj, "attribute")'
    }
    return suggestions.get(error_type, 'Review the code carefully and fix the issue.')
