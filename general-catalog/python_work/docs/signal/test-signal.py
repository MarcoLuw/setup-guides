import signal
import time

def my_handler(signum, frame):
    print("Custom SIGTERM handler invoked!")
    exit(0)

def default_behavior():
    print("Default SIGTERM handler")

# Check original handler
original = signal.getsignal(signal.SIGTERM)
print(f"Original handler: {original}")

# Task 1 - DOESN'T restore handler
def task_1_bad():
    old = signal.signal(signal.SIGTERM, my_handler)     # Set new handler (my_handler), return the previous handler
    print(f"Old handler: {old}")
    print(f"New handler: {signal.getsignal(signal.SIGTERM)}")
    try:
        print("Task 1 running...")
        1/0  # Exception!
    except Exception as e:
        print(f"Exception in task_1: {e}")
        return  # Returns early - handler NOT restored
    
    signal.signal(signal.SIGTERM, old)  # Never reached!
    # Should restore the old handler here
    # finally:
    #     signal.signal(signal.SIGTERM, old)

task_1_bad()

# Check if handler was restored
current = signal.getsignal(signal.SIGTERM)
print(f"After task_1 (bad): {current}")
print(f"Is it my_handler? {current == my_handler}")  # True - PROBLEM!

# Task 2 inherits wrong handler
def task_2():
    print("Task 2 running... send SIGTERM to this process")
    print(f"Current handler in task_2: {signal.getsignal(signal.SIGTERM)}")
    time.sleep(30)

# Now task_2 will use my_handler instead of default
task_2()