ms = "“A person who never made a mistake never tried anything new."
print(f'Albert Einstein once said, "{ms}"')

cube = [value**3 for value in range(1, 11)]
print(cube)

my_foods = ['pizza', 'falafel', 'carrot cake']
friend_foods = my_foods[:]
print(my_foods)           
print(friend_foods)

my_foods.append('cannoli')
friend_foods.append('ice cream')
print(my_foods)
print(friend_foods)

def make_pizza(*toppings):
    """Print the list of toppings that have been requested."""
    print(toppings)

make_pizza('pepperoni')
make_pizza('mushrooms', 'green peppers', 'extra cheese')

def build_profile(first, last, **user_info):
    """Build a dictionary containing everything we know about a user."""
    user_info['first_name'] = first
    user_info['last_name'] = last
    return user_info

user_profile = build_profile('albert', 'einstein',
                                location='princeton',
                                field='physics')

def make_car(manufacturer, model_name, **cars):
    cars["manufacturer"] = manufacturer
    cars["model_name"] = model_name
    print(cars)

car = make_car('subaru', 'outback', color='blue', tow_package=True)
print(car)
def test():
    try:
        answer = 5/0
        print("Hi from inside try block")
    except ZeroDivisionError as e:
        print("Cannot divide to 0")
    
    else:
        print(f"answer is: {answer}")
    print("Exception not terminate the program")

test()