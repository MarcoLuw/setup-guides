from contextvars import ContextVar
from multiprocessing import context

var = ContextVar("example", default="No Value")
var1 = ContextVar("example", default="No Value")

print(var.get())  # Output: No Value

token = var.set("Alice")
print(var.get())  # Output: Alice
print(token)  # Output: <ContextToken>

token = var.set("Bob")
print(var.get())  # Output: Bob
print(token)  # Output: <ContextToken>

var.reset(token)
print(var.get())  # Output: Alice

print(var1.get())  # Output: No Value
token1 = var1.set("Charlie")
print(var1.get())  # Output: Charlie
print(token1)  # Output: <ContextToken>

var1.reset(token1)
print(var1.get())  # Output: No Value


"""------------------------"""

from contextvars import ContextVar
import threading

myvar = ContextVar("myvar", default="NOPE")

def worker(name):
    print(f"[{name}] Before set:", myvar.get())
    token = myvar.set(f"Set by {name}")
    print(f"[{name}] After set:", myvar.get())
    myvar.set(f"Set again 1-{name}")
    print(f"[{name}] After second set:", myvar.get())
    myvar.reset(token)
    print(f"[{name}] After reset:", myvar.get())

# Create two threads that will use the same context variable: myvar
thread1 = threading.Thread(target=worker, args=("Thread A",))
thread2 = threading.Thread(target=worker, args=("Thread B",))

thread1.start()
thread2.start()
thread1.join()
thread2.join()

print("Main thread still has:", myvar.get())
myvar.set("Main thread set 1")
print("Main thread after set 1:", myvar.get())