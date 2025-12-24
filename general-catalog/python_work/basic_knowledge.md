# Python Work

---

## VARIABLES

### String
- Remove the whitespace at the right (left) end of a string: str.rstrip() (str.lstrip())
- Want to do it permanently: str = str.rstrip()

### Number
- Python tries to represent the result as precisely as possible
    ```python
    3 * 0.1 = 0.300000000000004
    ```
- Python ignores the underscores when storing these kinds of values
- 1000 is the same as 1_000, which is the same as 10_00
    ```python
    >>> universe_age = 14_000_000_000
    >>> print(universe_age)
    14000000000
    ```

### Comments
- explain what your code is supposed to do
- how you are making it work

---

## LIST
### Index Positions
- Accessing the last element in a list: 
    ```python
    bicycles = ['trek', 'cannondale', 'redline', 'specialized']
    print(bicycles[-1])  # specialized
    print(bicycles[-2])  # Get the second last element (= redline)
    ```

### Changing, Adding, and Removing Elements

#### 1. Modifying Elements in a List
    ```python
    motorcycles = ['honda', 'yamaha', 'suzuki']
    print(motorcycles)      # ['honda', 'yamaha', 'suzuki']
    motorcycles[0] = 'ducati'
    print(motorcycles)      # ['ducati', 'yamaha', 'suzuki']
    ```

#### 2. Adding Elements to a List
- Appending Elements to the End of a List:
    ```python
    motorcycles.append('ducati')    # ['honda', 'yamaha', 'suzuki']
    print(motorcycles)              # ['honda', 'yamaha', 'suzuki', 'ducati']  
    ```
- Inserting Elements into a List:
    ```python
    motorcycles.insert(0, 'ducati') # ['honda', 'yamaha', 'suzuki']
    print(motorcycles)              # ['ducati', 'honda', 'yamaha', 'suzuki']
    ```

#### #### 3. Removing Elements from a List
- Removing an Item Using the del Statement
    ```python
    del motorcycles[0]      # ['honda', 'yamaha', 'suzuki']
    print(motorcycles)      # ['yamaha', 'suzuki']
    ```
- Removing an Item Using the pop() Method
    ```python
    popped_motorcycle = motorcycles.pop()   # ['honda', 'yamaha', 'suzuki']
    print(motorcycles)                      # ['honda', 'yamaha']
    print(popped_motorcycle)                # suzuki
    ```
- Popping Items from any Position in a List
    ```python
    motorcycles = ['honda', 'yamaha', 'suzuki']
    first_owned = motorcycles.pop(0)    # ['yamaha', 'suzuki']
    print(first_owned)                  # honda
    ```
- When to use *del* and *pop()*
    - *del:* Delete an item from a list and not use that item in any way.
    - *pop():* Use an item as you remove it
- Removing an Item by Value
    ```python
    motorcycles.remove('ducati')    # ['honda', 'yamaha', 'suzuki', 'ducati']
    print(motorcycles)              # ['honda', 'yamaha', 'suzuki']
    - Note: remove(): deletes only the first occurrence of the value in the list
    ```

### Organizing a List
- sort(): changes the order of the list permanently.
    ```python
    cars = ['bmw', 'audi', 'toyota', 'subaru']
    cars.sort()
    print(cars)      #  ['audi', 'bmw', 'subaru', 'toyota']
    ```
- sorted(): maintain the original order of a list and present sorted order result
    ```python
    cars = ['bmw', 'audi', 'toyota', 'subaru']
    sort_cars = sorted(cars)        # ['audi', 'bmw', 'subaru', 'toyota']
    print(cars)                     # ['bmw', 'audi', 'toyota', 'subaru']
    ```

### Using range() to Make a List of Numbers
```python
numbers = list(range(1, 6))
print(numbers)          # [1, 2, 3, 4, 5]

even_numbers = list(range(2, 11, 2))
print(even_numbers)     # [2, 4, 6, 8, 10]

Note:  In Python, two asterisks (**) represent exponents
```

### Simple Statistics with a List of Numbers
```python
digits = [1, 2, 3, 4, 5, 6, 7, 8, 9, 0]
min(digits)     # 0
max(digits)     # 9
sum(digits)     # 45
```

### List Comprehensions
- Combines the *for* loop and the creation of new elements into one line
- Automatically appends each new element
    ```python
    squares = [value**2 for value in range(1, 11)]  
    # define the expression for the values -> write a for loop to generate the results feeded into the expression 
    print(squares)      # [1, 4, 9, 16, 25, 36, 49, 64, 81, 100]
    ```

### Working with Part of a List
#### 1. Slicing a List
- Make a slice, you specify the index of the first and last elements
    ```python
    players = ['charles', 'martina', 'michael', 'florence', 'eli']
    print(players[0:3])         # ['charles', 'martina', 'michael']
    ```
- Omit the first index, auto starts slice from the beginning
    ```python
    players = ['charles', 'martina', 'michael', 'florence', 'eli']
    print(players[:4])          # ['charles', 'martina', 'michael', 'florence']
    ```
- Omit the second index, slice is through the last item
    ```python
    players = ['charles', 'martina', 'michael', 'florence', 'eli']
    print(players[2:])          # ['michael', 'florence', 'eli']

    # Output the last three players
    players = ['charles', 'martina', 'michael', 'florence', 'eli']
    print(players[-3:])
    ```
- Add step for slicing:
    ```python
    players = ['charles', 'martina', 'michael', 'florence', 'eli']
    print(players[0:4:2])          # ['charles', 'michael']
    ```

#### 2. Copying a List
- Copy by creating a slice including the entire original list
    ```python
    my_foods = ['pizza', 'falafel', 'carrot cake']
    friend_foods = my_foods[:]      # Generate two separated lists
    print(my_foods)                 # ['pizza', 'falafel', 'carrot cake']
    print(friend_foods)             # ['pizza', 'falafel', 'carrot cake']

    # Note:
    my_foods = ['pizza', 'falafel', 'carrot cake']
    friend_foods = my_foods     # both friend_foods and my_foods refer to the same list
    
    my_foods.append('cannoli')
    print(my_foods)                 # ['pizza', 'falafel', 'carrot cake', 'cannoli']
    print(friend_foods)             # ['pizza', 'falafel', 'carrot cake', 'cannoli']
    ```

### Tuples 
- List: mutable list
- Tuple: immutable list
    ```python
    dimensions = (200, 50)
    print(dimensions[0])        # 200
    print(dimensions[1])        # 50
    ```
- Use tuple when you want to store a set of values that should not be changed throughout the life of a program

---

## IF STATEMENTS
```python
age = 12
if age < 4:
    price = 0
elif age < 18:
    price = 25
else:
    price = 40
print(f"Your admission cost is ${price}.")          # Your admission cost is 0
```

---

## DICTIONARIES
### Working with Dictionaries
#### 1. Accessing Values in a Dictionary
#### 2. Adding New Key-Value Pairs
    ```python
    alien_0 = {'color': 'green', 'points': 5}
    print(alien_0)              # {'color': 'green', 'points': 5}
    alien_0['x_position'] = 0
    alien_0['y_position'] = 25
    print(alien_0)              # {'color': 'green', 'points': 5, 'x_position': 0, 'y_position': 25}
    ```

#### 3. Modifying Values in a Dictionary
    ```python
    alien_0 = {'color': 'green'}
    print(alien_0['color'])             # green
    alien_0['color'] = 'yellow'
    print(alien_0['color'])             # yellow
    ```

#### 4. Removing Key-Value Pairs
    ```python
    alien_0 = {'color': 'green', 'points': 5}
    del alien_0['points']
    print(alien_0)              # {'color': 'green'}

    # Note: Be aware that the deleted key-value pair is removed permanently
    ```

#### 5. Using get() to Access Values
- **get():** first args: key, second args: default value if key not existed (optional)
    ```python
    alien_0 = {'color': 'green', 'speed': 'slow'}
    point_value = alien_0.get('points', 'No point value assigned.')
    print(point_value)              # No point value assigned.

    # If no default value defined, get() will return None
    ```

### Looping Through a Dictionary
#### 1. Use **items()** method
    ```python
    user_0 = {
        'username': 'efermi',
        'first': 'enrico',
        'last': 'fermi',
        }
    for key, value in user_0.items():
        print(f"\nKey: {key}")          # username, first, last
        print(f"Value: {value}")        # efermi, enrico, fermi
    ```

#### 2. Looping Through All the Keys in a Dictionary
- Use **keys()** method
    ```python
    favorite_languages = {
        'jen': 'python',
        'sarah': 'c',
        'edward': 'ruby',
        'phil': 'python',
    }
    for name in favorite_languages.keys():      # The same as default behaviour (for name in favorite_languages)
       print(name.title())
    ```

#### 3. Looping Through All Values in a Dictionary
- Use **values()** method
    ```python
    favorite_languages = {
        'jen': 'python',
        'sarah': 'c',
        'edward': 'ruby',
        'phil': 'python',
    }
    for language in favorite_languages.values():
        print(language.title())
    ```
- Above can result in repetition of values, can use **set** collection for unique value.
    ```python
    for language in set(favorite_languages.values()):
        print(language.title())
    ```

### Set
- You can build a set directly using braces and separating the elements with commas
- Sets do not retain items in any specific order.
- Items in sets are unique (no repetition)
    ```python
    languages = {'python', 'ruby', 'python', 'c', 'ruby'}
    print(languages)            # {'ruby', 'python', 'c'}
    ```

### Nesting
#### 1. A List of Dictionaries
#### 2. A List in a Dictionary
#### 3. A Dictionary in a Dictionary

---

##  USER INPUT AND WHILE LOOPS
### How the input() Function Works
- Use **input()** function to get client input
    ```python
    message = input("Tell me something, and I will repeat it back to you: ")
    print(message)
    ```

### Introducing while Loops
#### 1. The while Loop in Action
#### 2. Using continue in a Loop
- **continue**: get back to while condition
    ```python
    current_number = 0
    while current_number < 10:
        current_number += 1
        if current_number % 2 == 0:
            continue                        # If even, jump to while condition (by pass all codes below)
        print(current_number)               # 1 3 5 7 9
    ```

---

## FUNCTIONS
- Every function should have one specific job
- Can be helpful when splitting a complex task into a series of steps.

### Defining a Function
#### 1. Passing Information to a Function
#### 2.  Arguments and Parameters

### Passing Arguments
#### 1. Positional Arguments
- Positional arguments need to be passed in the same order the parameters were written 

#### 2. Keyword Arguments
- A keyword argument is a name-value pair that you pass to a function
    ```python
    def describe_pet(animal_type, pet_name):
        """Display information about a pet."""
        print(f"\nI have a {animal_type}.")
        print(f"My {animal_type}'s name is {pet_name.title()}.")
    
    describe_pet(animal_type='hamster', pet_name='harry')
    ```

#### 3. Default Values
```python
def describe_pet(pet_name, animal_type='dog')
```

*Note: any params with default value must be listed after all params without default values. This allows Python to continue interpreting positional arguments correctly*

### Return Values
#### 1. Making an Argument Optional
- A technique to make an argument optional is that we can give it a default value (None, empty string,...)
    ```python
    def get_formatted_name(first_name, last_name, middle_name=''):
        """Return a full name, neatly formatted."""
        if middle_name:
            full_name = f"{first_name} {middle_name} {last_name}"
        else:
            full_name = f"{first_name} {last_name}"
        return full_name.title()

    musician = get_formatted_name('jimi', 'hendrix')
    musician = get_formatted_name('john', 'hooker', 'lee')
    ```

#### 2. Returning a Dictionary

### Passing a List
#### 1. Pass a List
```python
def greet_users(names):
    """Print a simple greeting to each user in the list."""
    for name in names:
        msg = f"Hello, {name.title()}!"
        print(msg)
usernames = ['hannah', 'ty', 'margot']
greet_users(usernames)
```

#### 2. Modifying a List in a Function
- Note that if we pass directly a list into a function, every changes of the list in the function are permanantly
    ```python
    def print_models(unprinted_designs, completed_models):
    """
    Simulate printing each design, until none are left.
    Move each design to completed_models after printing.
     """
    while unprinted_designs:
        current_design = unprinted_designs.pop()
        print(f"Printing model: {current_design}")
        completed_models.append(current_design)
    ```

#### 3. Preventing a Function from Modifying a List
- Pass the function a copy of the list, not the original.
- Any changes the function makes to the list will affect only the copy, leaving the original list intact
    ```python
    function_name(list_name[:])
    print_models(unprinted_designs[:], completed_models)

    # The slice notation [:] makes a copy of the list
    ```
- It’s more efficient for a function to work with an existing list --> it saves time and memory needed to make a copy

### Passing an Arbitrary Number of Arguments

What if we don't know how many arguments need to be passed to a function?

- ***toppings:** tells Python to make a tuple called toppings
-  This syntax works no matter how many arguments the function receives.
    ```python
    def make_pizza(*toppings):
        """Print the list of toppings that have been requested."""
        print(toppings)

    make_pizza('pepperoni')
    make_pizza('mushrooms', 'green peppers', 'extra cheese')
    ```

#### 1. Mixing Positional and Arbitrary Arguments
- Python matches *positional* and *keyword arguments* first, then collects any remaining arguments in the final parameter.
    ```python
    def make_pizza(size, *toppings):
        """Summarize the pizza we are about to make."""
            print(f"\nMaking a {size}-inch pizza with the following toppings:")
        for topping in toppings:
                print(f"- {topping}")

    make_pizza(16, 'pepperoni')
    make_pizza(12, 'mushrooms', 'green peppers', 'extra cheese')

    # size = 12
    # toppings = ('mushrooms', 'green peppers', 'extra cheese')
    ```
*Note:* You’ll often see the generic parameter name **\*args**, which collects arbitrary positional arguments like this.

#### 2. Using Arbitrary Keyword Arguments
- Problems:  you don't know what kind of information will be passed to the function.
- Solution:  write functions that accept as many *key-value pairs* as the calling statement provides
    ```python
    def build_profile(first, last, **user_info):
    """Build a dictionary containing everything we know about a user."""
        user_info['first_name'] = first
        user_info['last_name'] = last
        return user_info
    
    user_profile = build_profile('albert', 'einstein',
                                location='princeton',
                                field='physics')
    print(user_profile)

    # {'location': 'princeton', 'field': 'physics', 'first_name': 'albert', 'last_name': 'einstein'
    ```
- The double asterisks **\*\*user_info**: create a dictionary called **user_info** containing all the extra name-value pairs the function receives

*Note:* You’ll often see the parameter name **\*\*kwargs** used to collect nonspecific keyword arguments.

### Storing Your Functions in Modules

Benefits:
- Allows to hide the details of your program’s code and focus on its higher-level logic.
- Allows to reuse functions in many different programs.
- Allows you to use libraries of functions that other programmers have written

#### 1. Importing an Entire Module
- A module is a file ending in .py that contains the code you want to import
    ```python
    import pizza
    
    pizza.make_pizza(16, 'pepperoni')
    pizza.make_pizza(12, 'mushrooms', 'green peppers', 'extra cheese')

    # Syntax: module_name.function_name()
    ```

#### 2. Other importing approaches

```python

####  1. Importing Specific Functions
from module_name import function_name
from module_name import function_0, function_1, function_2

# #### 2. Using as to Give a Function an Alias
from module_name import function_name as fn

# #### 3. Using as to Give a Module an Alias
import pizza as p
p.make_pizza(16, 'pepperoni')
p.make_pizza(12, 'mushrooms', 'green peppers', 'extra cheese')

# #### 4. Importing All Functions in a Module
from module_name import *
```

### Styling Functions

*Reference link: https://peps.python.org/pep-0008/#prescriptive-naming-conventions*

- Functions should have descriptive names
- These names should use lowercase letters and underscores
- Module names should use these conventions as well.
- Every function should have a comment that explains concisely what the function does
- This comment should appear immediately after the function definition and use the docstring format.
- If you specify a default value for a parameter, no spaces should be used on either side of the equal sign
    ```python
    def function_name(parameter_0, parameter_1='default value')
    ```
- Also keyword arguments in function calls
    ```python
    function_name(value_0, parameter_1='value')
    ```
- Limit lines of code to 79 characters
- If a set of parameters causes a function’s definition to be longer than 79 characters, press ENTER after the opening parenthesis on the definition line.
- On the next line, press the TAB key twice to separate the list of arguments from the body of the function
    ```python
    def function_name(
            parameter_0, parameter_1, parameter_2,
            parameter_3, parameter_4, parameter_5):
        function body...
    ```
- If there are more than two functions, seperate them by two blank lines
- All import statements should be written at the beginning of a file

---

## CLASSES
### Creating and Using a Class
#### 1. Creating the Dog Class
- By convention, capitalized names refer to classes in Python

    ```python
    class Dog:
        """A simple attempt to model a dog."""
        def __init__(self, name, age):
            """Initialize name and age attributes."""
            self.name = name
            self.age = age

        def sit(self):
            """Simulate a dog sitting in response to a command."""
            print(f"{self.name} is now sitting.")

        def sit(self):
            """Simulate rolling over in response to a command."""
            print(f"{self.name} rolled over!")
    ```

#### 2. The **\_\_init\_\_()** Method
- Runs automatically whenever we create a new instance based on the Dog class
- The **self** parameter is required in the method definition

    ```python
    self.name = name
    self.age = age

    def sit(self):
    def sit(self):

    my_dog = Dog('Willie', 6)       # my_dog is an instance of class Dog
    ```
- **self**: is a reference to the instance itself, it gives the individual instance access to the *attributes* and *methods* in the class
- **Attributes**: Variables prefixed with self that are accessible through instances, available to every method in the class
- **Method**: A function that’s part of a class

#### 3. Making an Instance from a Class
- When we make an instance from a Class:
    - Python calls **\_\_init\_\_()** method
    - **\_\_init\_\_()** creates an instance representing this particular dog and sets the name and age attributes using the provided values.
    - Python returns an instance representing this dog
    - Lastly, assign that instance to the variable *my_dog*

    ```python
    class Dog:
        --snip--

    my_dog = Dog('Willie', 6)
    print(f"My dog's name is {my_dog.name}.")
    print(f"My dog is {my_dog.age} years old."
    ```
- Accessing Attributes
- Calling Methods
- Creating Multiple Instances

### Working with Classes and Instances

### Inheritance

One class inherits from another, it takes on the attributes and methods of the first class.

#### 1. The **\_\_init\_\_()** Method for a Child Class
- If you want to call the **\_\_init\_\_()** method from the parent class

    ```python
    class Car:
        """A simple attempt to represent a car."""
        def __init__(self, make, model, year):
            """Initialize attributes to describe a car."""
            self.make = make
            self.model = model
            self.year = year
            self.odometer_reading = 0
            self.oil = 0

        def get_descriptive_name(self):
        """Return a neatly formatted descriptive name."""
            long_name = f"{self.year} {self.make} {self.model}"
            return long_name.title()

        def read_odometer(self):
            """Print a statement showing the car's mileage."""
            print(f"This car has {self.odometer_reading} miles on it.")

        def update_odometer(self, mileage):
            """Set the odometer reading to the given value."""
            if mileage >= self.odometer_reading:
                self.odometer_reading = mileage
            else:
                print("You can't roll back an odometer!")

        def increment_odometer(self, miles):
            """Add the given amount to the odometer reading."""
            self.odometer_reading += miles

        def fill_gas_tank(self, oil):
            """Fill gas tank with a specified value of oil"""
            self.oil += oil


    class ElectricCar(Car):
        """Represent aspects of a car, specific to electric vehicles."""
        def __init__(self, make, model, year):
            """Initialize attributes of the parent class."""
            super().__init__(make, model, year)

    my_leaf = ElectricCar('nissan', 'leaf', 2024)
    print(my_leaf.get_descriptive_name())               # 2024 Nissan Leaf
    ```
- **super()** function: allows to call a method from the parent class

#### 2. Defining Attributes and Methods for the Child Class

#### 3. Overriding Methods from the Parent Class
- Initially, a child class can call a parent class method without defining the method again

    ```python
    class Car:
        def __init__(self, name, year):
            self.name = name
            self.year = year
        def info(self):
            print(f'Car info: {self.name}, manufactured in {self.year}')

    class HybridCar(Car):
        def __init__(self, name, year):
            super().__init__(name, year)
        def fake_info(self):
            self.info()             # or super().info()
            print('Hi, it from self')
    ```
- To override methods from parent class, you define a method in the child class with the same name as the method you want to override

    ```python
    class ElectricCar(Car):
        --snip--

        def fill_gas_tank(self):
            """Electric cars don't have gas tanks."""
            print("This car doesn't have a gas tank!")
    ```
- **Different between calling self.method() and super().method()**
    - **super().info()**: call `info()` method originally from parent class **Car**, bypassing any overridden versions in **HybridCar_1**
    - **self.info()**: call `info()` method of this object **HybridCar**
    - Example:

    ```python
    class HybridCar(Car):
        def __init__(self, name, year):
            super().__init__(name, year)
        def info(self):
            print('Hi, it info() from this object, not from the parent')
        def fake_info(self):
            self.info()
            print('Hi, it from self')

    f_car = HybridCar('suzuki', '2025')
    f_car.fake_info()

    # Hi, it info() from this object, not from the parent
    # Hi, it from self

    class HybridCar_1(Car):
        def __init__(self, name, year):
            super().__init__(name, year)
        def info(self):
            print('Hi, it info() from this object, not from the parent')
        def fake_info(self):
            super().info()
            print('Hi, it from super()')

    f_car_1 = HybridCar_1('yamaha', '2018')
    f_car_1.fake_info()

    # Car info: yamaha, manufactured in 2018
    # Hi, it from super()
    ```

#### 4. Instances as Attributes
- **composition**: You can break your large class into smaller classes that work together; this approach is called *composition*

    ```python
    # What if there are many attributes and methods about the car battery?
    # ==> Create a separated Battery class

    class Car:
        --snip--

    class Battery:
        """A simple attempt to model a battery for an electric car."""
        def __init__(self, battery_size=40):
            """Initialize the battery's attributes."""
            self.battery_size = battery_size

        def describe_battery(self):
            """Print a statement describing the battery size."""
            print(f"This car has a {self.battery_size}-kWh battery.")
    
    
    class ElectricCar(Car):
        """Represent aspects of a car, specific to electric vehicles."""
        def __init__(self, make, model, year):
            """
            Initialize attributes of the parent class.
            Then initialize attributes specific to an electric car.
            """
            super().__init__(make, model, year)
            self.battery = Battery()
    
    my_leaf = ElectricCar('nissan', 'leaf', 2024)
    print(my_leaf.get_descriptive_name())       # 2024 Nissan Leaf
    my_leaf.battery.describe_battery()          # This car has a 40-kWh battery.
    ```

#### 5. Modeling Real-World Objects

### Importing Classes

### The Python Standard Library

### Styling Classes
- **Class names**: should be written in CamelCase, capitalize first letter of each word in the name, and don’t use underscores
- **Instance and module names**: written in lowercase, with underscores between words.
- Every class should have a **docstring** immediately following the class definition - The docstring should be a brief description of what the class does
- Each module should also have a docstring describing what the classes in a module can be used for.
- Use blank lines to organize code:
    - one blank line between methods in a class
    - two blank lines to separate classes in a module
- Import modules order:
    - import statement for the **standard library** module first
    - add a blank line
    - import statement for the module you wrote

---

## FILES AND EXCEPTIONS

### Reading from a File

### Writing to a File

### Using try-except-else blocks
- **else block** is optional
- How to put code in a suitable block:
    - **try block**: code that might cause an exception to be raised
    - **except block**: code that tells Python what to do in case a certain exception arises when it tries to run the code in the *try block*
    - **else block**: code that should run only if the try block was successful
- Using Exception can prevent program from crashing

    ```python
    def test():
        try:
            answer = 5/0
            print("Hi from inside try block")
        except ZeroDivisionError as e:
            print("Cannot divide to 0")
        else:
            print(f"answer is: {answer}")
        print("Exception not terminate the program")

    test()      # Cannot divide to 0
                # Exception does not terminate the program
    ```
- We can use as much **except block** as we want to handle Exception cases.
    ```python
    try:
        num = int(input("Enter a number: "))
        result = 10 / num
        with open("sample.txt", "r") as f:
            content = f.read()
        print("Result:", result)
        print("File content:", content)
    except ValueError:
        print("Invalid input! Please enter a number.")
    except ZeroDivisionError:
        print("Oops! Cannot divide by zero.")
    except FileNotFoundError:
        print("File not found! Please make sure 'sample.txt' exists.")
    except Exception as e:
        print(f"Something went wrong: {e}")

    print("Program continues after exception handling.")
    ```

### Storing Data
#### 1. Using json.dumps() and json.loads()
#### 2. Saving and Reading User-Generated Data

---

## TESTING YOUR CODE

When you write a function or a class, you can also write tests for that code. Testing proves that your code works as it’s supposed to in response to all the kinds of input it’s designed to receive.

Using `pytest`to build a series of tests and check that each set of inputs results in the output you want.

### Testing a Function

```python
# name_function.py

def get_formatted_name(first, last):
    """Generate a neatly formatted full name."""
    full_name = f"{first} {last}"
    return full_name.title()
```

```python
# names.py

from name_function import get_formatted_name
print("Enter 'q' at any time to quit.")
while True:
    first = input("\nPlease give me a first name: ")
    if first == 'q':
        break
    last = input("Please give me a last name: ")
    if last == 'q':
        break
    formatted_name = get_formatted_name(first, last)
    print(f"\tNeatly formatted name: {formatted_name}.")
```

#### 1. Unit Tests and Test Cases
- **Unit test**: A unit test verifies that one specific aspect of a function’s behavior is correct
- **Test case**: A test case is a collection of unit tests that together prove that a function behaves as it’s supposed to

#### 2. A Passing Test

```python
# test_name_function.py

from name_function import get_formatted_name_function.py

def test_first_last_name():
    """Do names like 'Janis Joplin' work?"""
    formatted_name = get_formatted_name('janis', 'joplin')
    assert formatted_name == 'Janis Joplin'
```
- **Test files**: name must start with *test_*
- **Test functions**: 
    - need to start with the word *test*
    - followed by an underscore
    - will be run as part of the testing process
- **assertion**: a claim about a condition

#### 3. Running a Test
- Enter **pytest** in the terminal of folders containing test file

```shell
$ pytest
========================= test session starts =========================
platform darwin -- Python 3.x.x, pytest-7.x.x, pluggy-1.x.x
rootdir: /.../python_work/chapter_11
collected 1 item
test_name_function.py .                                          [100%]
========================== 1 passed in 0.00s ==========================
```

#### 4. A Failing Test

#### 5. Adding New Tests

```python
# test_name_function.py

from name_function import get_formatted_name
def test_first_last_name():
    --snip--

def test_first_last_middle_name():
    """Do names like 'Wolfgang Amadeus Mozart' work?"""
    formatted_name = get_formatted_name('wolfgang', 'mozart', 'amadeus')
    assert formatted_name == 'Wolfgang Amadeus Mozart'
```

```bash
$ pytest
========================= test session starts =========================--snip-
collected 2 items
test_name_function.py ..                                         [100%]
========================== 2 passed in 0.01s ==========================
```

### Testing a Class

#### 1. A Variety of Assertions

- Table 11-1: Commonly Used Assertion Statements in Tests

| Assertion                   | Claim                                       |
|----------------------------|---------------------------------------------|
| `assert a == b`            | Assert that two values are equal.           |
| `assert a != b`            | Assert that two values are not equal.       |
| `assert a`                 | Assert that `a` evaluates to `True`.        |
| `assert not a`             | Assert that `a` evaluates to `False`.       |
| `assert element in list`   | Assert that an element is in a list.        |
| `assert element not in list` | Assert that an element is not in a list.  |

#### 2. A Class to Test

```python
# survey.py

class AnonymousSurvey:
    """Collect anonymous answers to a survey question."""
    def __init__(self, question):
        """Store a question, and prepare to store responses."""
        self.question = question
        self.responses = []
    def show_question(self):
        """Show the survey question."""
        print(self.question)
    def store_response(self, new_response):
        """Store a single response to the survey."""
        self.responses.append(new_response)
    def show_results(self):
        """Show all the responses that have been given."""
        print("Survey results:")
        for response in self.responses:
            print(f"- {response}")
```

- Testing the AnonymousSurvey Class

    ```python
    # test_survey.py

    from survey import AnonymousSurvey

    def test_store_single_response():
        """Test that a single response is stored properly."""
        question = "What language did you first learn to speak?"
        language_survey = AnonymousSurvey(question)
        language_survey.store_response('English')
        assert 'English' in language_survey.responses
    
    def test_store_three_responses():
        """Test that three individual responses are stored properly."""
        question = "What language did you first learn to speak?"
        language_survey = AnonymousSurvey(question)
        responses = ['English', 'Spanish', 'Mandarin']
        for response in responses:
            language_survey.store_response(response)
        for response in responses:
            assert response in language_survey.responses
    ```

```shell
$ pytest test_survey.py
========================= test session starts =========================--snip-
test_survey.py ..                                                [100%]
========================== 2 passed in 0.01s ==========================
```

#### 3. Using Fixtures
- **fixture**: set up a test environment, creating a resource that’s used by more than one test

    ```python
    import pytest
    from survey import AnonymousSurvey

    @pytest.fixture
    def language_survey():
        """A survey that will be available to all test functions."""
        question = "What language did you first learn to speak?"
        language_survey = AnonymousSurvey(question)
        return language_survey

    def test_store_single_response(language_survey):
        """Test that a single response is stored properly."""
        language_survey.store_response('English')
        assert 'English' in language_survey.responses

    def test_store_three_responses(language_survey):
        """Test that three individual responses are stored properly."""
        responses = ['English', 'Spanish', 'Mandarin']
        for response in responses:
            language_survey.store_response(response)
        for response in responses:
            assert response in language_survey.responses
    ```
- When *a parameter* in a test function matches the *name of a function* with the **@pytest.fixture decorator**, the fixture will be auto run and pass the return value to the test function