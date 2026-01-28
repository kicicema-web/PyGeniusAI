package com.pygeniusai.ai

/**
 * Local AI Knowledge Base for PyGenius AI
 * Contains error explanations, lessons, and code patterns
 */
class LocalAiKnowledge {
    
    private val errorKnowledgeBase = mapOf(
        "IndexError" to ErrorExplanation(
            errorType = "IndexError",
            explanation = "You're trying to access an index that doesn't exist. Remember: Python uses 0-based indexing!",
            suggestion = "Check list length before accessing: if index < len(my_list):",
            example = "my_list = [1, 2, 3]  # Valid indices: 0, 1, 2"
        ),
        "KeyError" to ErrorExplanation(
            errorType = "KeyError",
            explanation = "The dictionary doesn't have this key. This is like looking for a word that's not in the dictionary.",
            suggestion = "Use .get() method: my_dict.get('key', default_value)",
            example = "data = {'a': 1}  # Use data.get('b', 0) instead of data['b']"
        ),
        "TypeError" to ErrorExplanation(
            errorType = "TypeError",
            explanation = "You're using an operation with incompatible types. Like trying to add a string and a number.",
            suggestion = "Convert types: int(string_var) or str(number_var)",
            example = "'5' + 3  # Error!  Use int('5') + 3  # Correct: 8"
        ),
        "ValueError" to ErrorExplanation(
            errorType = "ValueError",
            explanation = "The value has the right type but an inappropriate value.",
            suggestion = "Validate input before conversion or use try-except",
            example = "try: x = int(user_input) except ValueError: print('Enter a number')"
        ),
        "NameError" to ErrorExplanation(
            errorType = "NameError",
            explanation = "This variable or function name doesn't exist. Maybe a typo?",
            suggestion = "Check spelling, ensure variable is defined before use",
            example = "x = 5  # Define first, then print(x)"
        ),
        "ZeroDivisionError" to ErrorExplanation(
            errorType = "ZeroDivisionError",
            explanation = "Cannot divide by zero! This is mathematically undefined.",
            suggestion = "Add a check before division",
            example = "if divisor != 0: result = numerator / divisor"
        ),
        "AttributeError" to ErrorExplanation(
            errorType = "AttributeError",
            explanation = "This object doesn't have the attribute or method you're trying to use.",
            suggestion = "Check the object's type with type() or use hasattr()",
            example = "my_list = [1, 2, 3]  # my_list.append(4) not my_list.push(4)"
        ),
        "ImportError" to ErrorExplanation(
            errorType = "ImportError",
            explanation = "Cannot import this module. It might not be installed.",
            suggestion = "Install with pip or check the module name",
            example = "# pip install requests then import requests"
        ),
        "ModuleNotFoundError" to ErrorExplanation(
            errorType = "ModuleNotFoundError",
            explanation = "Python can't find this module. You need to install it first.",
            suggestion = "Use the Pip menu to install: pip install <module_name>",
            example = "# Tap Pip button, type 'numpy', and install"
        ),
        "SyntaxError" to ErrorExplanation(
            errorType = "SyntaxError",
            explanation = "Your code has a grammar mistake. Like a sentence without proper punctuation!",
            suggestion = "Check for missing colons, parentheses, or quotes",
            example = "# Wrong: if x == 5  # Right: if x == 5:"
        ),
        "IndentationError" to ErrorExplanation(
            errorType = "IndentationError",
            explanation = "Python uses indentation to define code blocks. Yours is wrong!",
            suggestion = "Use 4 spaces consistently. Don't mix tabs and spaces.",
            example = "def my_function():  # 4 spaces here  print('Hello')"
        ),
        "RecursionError" to ErrorExplanation(
            errorType = "RecursionError",
            explanation = "Your function called itself too many times. It went in circles!",
            suggestion = "Add a base case to stop recursion",
            example = "def factorial(n): if n <= 1: return 1  # Base case"
        ),
        "FileNotFoundError" to ErrorExplanation(
            errorType = "FileNotFoundError",
            explanation = "The file you're trying to open doesn't exist at that path.",
            suggestion = "Check the file path, or create the file first",
            example = "import os  if os.path.exists('file.txt'): open it"
        )
    )
    
    fun explainError(errorMessage: String, codeContext: String): ErrorExplanation {
        for ((errorType, explanation) in errorKnowledgeBase) {
            if (errorType in errorMessage) {
                return explanation
            }
        }
        
        return ErrorExplanation(
            errorType = "Unknown",
            explanation = "An unexpected error occurred. This might be a runtime issue or logic error.",
            suggestion = "Try breaking your code into smaller parts to identify the issue.",
            example = "# Use print statements to debug"
        )
    }
    
    fun getLesson(type: LessonType, level: DifficultyLevel): Lesson {
        return when (type) {
            LessonType.VARIABLES -> createVariablesLesson(level)
            LessonType.LOOPS -> createLoopsLesson(level)
            LessonType.FUNCTIONS -> createFunctionsLesson(level)
            LessonType.CLASSES -> createClassesLesson(level)
            LessonType.LIST_COMPREHENSION -> createListComprehensionLesson(level)
            LessonType.DATA_STRUCTURES -> createDataStructuresLesson(level)
        }
    }
    
    private fun createVariablesLesson(level: DifficultyLevel): Lesson {
        return when (level) {
            DifficultyLevel.BEGINNER -> Lesson(
                title = "Variables & Data Types",
                description = "Learn how to store and use data in Python",
                code = BASIC_VARIABLES_CODE,
                challenge = "Create variables for your favorite food, a price (with decimal), and whether you like it. Print them.",
                hints = listOf(
                    "Use quotes for text (string)",
                    "Use numbers without quotes for integers",
                    "Use True or False (capitalized) for boolean"
                ),
                solution = VARIABLES_SOLUTION,
                difficulty = level
            )
            else -> createIntermediateVariablesLesson()
        }
    }
    
    private fun createLoopsLesson(level: DifficultyLevel): Lesson {
        return when (level) {
            DifficultyLevel.BEGINNER -> Lesson(
                title = "For Loops",
                description = "Repeat actions efficiently with loops",
                code = BASIC_LOOPS_CODE,
                challenge = "Print numbers 1 to 10, then print 'Done!'",
                hints = listOf(
                    "Use range(1, 11) for 1 to 10",
                    "The second number in range is exclusive"
                ),
                solution = LOOPS_SOLUTION,
                difficulty = level
            )
            DifficultyLevel.INTERMEDIATE -> Lesson(
                title = "Loop Patterns",
                description = "Common loop patterns every Python developer should know",
                code = INTERMEDIATE_LOOPS_CODE,
                challenge = "Given two lists: items = ['apple', 'banana'] and prices = [1.50, 0.75], print each item with its price.",
                hints = listOf(
                    "Use zip() to iterate both lists together",
                    "Unpack with: for item, price in zip(items, prices)"
                ),
                solution = INTERMEDIATE_LOOPS_SOLUTION,
                difficulty = level
            )
            else -> createAdvancedLoopsLesson()
        }
    }
    
    private fun createFunctionsLesson(level: DifficultyLevel): Lesson {
        return Lesson(
            title = "Functions",
            description = "Reusable blocks of code",
            code = FUNCTIONS_CODE,
            challenge = "Create a function 'square' that takes a number and returns its square. Test with square(5).",
            hints = listOf(
                "Use def to define a function",
                "Use return to send back the result",
                "n ** 2 gives you n squared"
            ),
            solution = FUNCTIONS_SOLUTION,
            difficulty = DifficultyLevel.BEGINNER
        )
    }
    
    private fun createClassesLesson(level: DifficultyLevel): Lesson {
        return Lesson(
            title = "Classes & Objects",
            description = "Create your own data types",
            code = CLASSES_CODE,
            challenge = "Create a Rectangle class with width and height. Add a method 'area' that returns width * height.",
            hints = listOf(
                "__init__ is the constructor method",
                "Use self.parameter to store instance variables"
            ),
            solution = CLASSES_SOLUTION,
            difficulty = DifficultyLevel.INTERMEDIATE
        )
    }
    
    private fun createListComprehensionLesson(level: DifficultyLevel): Lesson {
        return Lesson(
            title = "List Comprehensions",
            description = "Pythonic way to create lists",
            code = LIST_COMP_CODE,
            challenge = "Convert this loop to a list comprehension: result = [] for x in range(20): if x % 3 == 0: result.append(x)",
            hints = listOf(
                "Syntax: [expression for item in iterable if condition]",
                "The if comes at the end"
            ),
            solution = LIST_COMP_SOLUTION,
            difficulty = DifficultyLevel.INTERMEDIATE
        )
    }
    
    private fun createDataStructuresLesson(level: DifficultyLevel): Lesson {
        return Lesson(
            title = "Dictionaries",
            description = "Key-value data storage",
            code = DICT_CODE,
            challenge = "Create a phone book dictionary with 3 contacts. Write code to look up a phone number by name.",
            hints = listOf(
                "Use curly braces: {'key': 'value'}",
                "Use .get() to avoid KeyError"
            ),
            solution = DICT_SOLUTION,
            difficulty = DifficultyLevel.BEGINNER
        )
    }
    
    private fun createIntermediateVariablesLesson(): Lesson {
        return Lesson(
            title = "Variable Unpacking",
            description = "Advanced variable assignment techniques",
            code = VAR_UNPACK_CODE,
            challenge = "Given coordinates = (10, 20, 30), unpack into x, y, z variables and print them.",
            hints = listOf("Use: x, y, z = coordinates"),
            solution = VAR_UNPACK_SOLUTION,
            difficulty = DifficultyLevel.INTERMEDIATE
        )
    }
    
    private fun createAdvancedLoopsLesson(): Lesson {
        return Lesson(
            title = "Generator Expressions",
            description = "Memory-efficient iteration",
            code = GENERATOR_CODE,
            challenge = "Calculate sum of squares from 1 to 1000 using a generator expression.",
            hints = listOf("Use parentheses for generator: (x**2 for x in ...)", "Pass directly to sum()"),
            solution = GENERATOR_SOLUTION,
            difficulty = DifficultyLevel.ADVANCED
        )
    }
    
    companion object {
        private val BASIC_VARIABLES_CODE = """
# Variables are containers for data
name = "Alice"      # String (text)
age = 25            # Integer (whole number)
height = 5.6        # Float (decimal)
is_student = True   # Boolean (True/False)

# Print the variables
print(f"{name} is {age} years old")
"""

        private val VARIABLES_SOLUTION = """
food = "Pizza"
price = 12.99
is_delicious = True
print(f"{food} costs $" + "{price}" + " and delicious={is_delicious}")
"""

        private val BASIC_LOOPS_CODE = """
# For loop iterates over a sequence
fruits = ["apple", "banana", "cherry"]

for fruit in fruits:
    print(f"I like {fruit}")

# Range gives you numbers
for i in range(5):
    print(i)  # Prints 0, 1, 2, 3, 4
"""

        private val LOOPS_SOLUTION = """
for i in range(1, 11):
    print(i)
print("Done!")
"""

        private val INTERMEDIATE_LOOPS_CODE = """
# Enumerate gives index and value
items = ["a", "b", "c"]
for index, value in enumerate(items):
    print(f"{index}: {value}")

# Zip iterates multiple lists together
names = ["Alice", "Bob"]
scores = [95, 87]
for name, score in zip(names, scores):
    print(f"{name} scored {score}")
"""

        private val INTERMEDIATE_LOOPS_SOLUTION = """
items = ['apple', 'banana']
prices = [1.50, 0.75]
for item, price in zip(items, prices):
    print(f"{item}: $" + "{price}")
"""

        private val FUNCTIONS_CODE = """
def greet(name):
    return f"Hello, {name}!"

# Call the function
message = greet("Alice")
print(message)
"""

        private val FUNCTIONS_SOLUTION = """
def square(n):
    return n ** 2

result = square(5)
print(result)  # 25
"""

        private val CLASSES_CODE = """
class Dog:
    def __init__(self, name):
        self.name = name
    
    def bark(self):
        return f"{self.name} says Woof!"

my_dog = Dog("Buddy")
print(my_dog.bark())
"""

        private val CLASSES_SOLUTION = """
class Rectangle:
    def __init__(self, width, height):
        self.width = width
        self.height = height
    
    def area(self):
        return self.width * self.height

rect = Rectangle(5, 3)
print(rect.area())  # 15
"""

        private val LIST_COMP_CODE = """
# Traditional way
squares = []
for x in range(10):
    squares.append(x**2)

# List comprehension - same result, one line!
squares = [x**2 for x in range(10)]

# With condition
even_squares = [x**2 for x in range(10) if x % 2 == 0]
"""

        private val LIST_COMP_SOLUTION = "result = [x for x in range(20) if x % 3 == 0]"

        private val DICT_CODE = """
# Dictionary: key -> value mapping
student = {
    "name": "Alice",
    "age": 20,
    "grade": "A"
}

# Access values
print(student["name"])

# Safe access
major = student.get("major", "Undeclared")

# Add/Update
student["major"] = "Computer Science"
"""

        private val DICT_SOLUTION = """
phone_book = {
    "Alice": "555-0101",
    "Bob": "555-0102",
    "Charlie": "555-0103"
}

name = "Alice"
number = phone_book.get(name, "Not found")
print(f"{name}: {number}")
"""

        private val VAR_UNPACK_CODE = """
# Multiple assignment
x, y, z = 1, 2, 3

# Swapping
a, b = 10, 20
a, b = b, a  # Now a=20, b=10!

# Extended unpacking
first, *rest = [1, 2, 3, 4, 5]
"""

        private val VAR_UNPACK_SOLUTION = """
coordinates = (10, 20, 30)
x, y, z = coordinates
print(f"x={x}, y={y}, z={z}")
"""

        private val GENERATOR_CODE = """
# Generator expression (lazy evaluation)
squares = (x**2 for x in range(1000000))

# Memory efficient - computes one at a time
total = sum(x**2 for x in range(1000000))
"""

        private val GENERATOR_SOLUTION = """
result = sum(x**2 for x in range(1, 1001))
print(result)
"""
    }
}
