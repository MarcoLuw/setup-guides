## **Class method**

The `@classmethod` decorator is needed for `get_from_yaml_and_env()` because it is designed to be called on the class itself, not on an instance.

### 1. Why?
- `get_from_yaml_and_env()` creates and returns a new instance of the class (`cls(**yaml_data)`).
- Using `@classmethod` allows you to call it as `AppSettings.from_yaml_and_env(yaml_path)`, and `cls` will refer to `AppSettings` (or any subclass).
- Without `@classmethod`, you would need an instance to call the method, but you don’t have one yet—you’re trying to create it!

### 2. In summary:
`@classmethod` lets you use the class (`cls`) to construct a new instance with data from YAML and environment variables. This is the standard Python pattern for alternative constructors.

---

## **Static method**

The `@staticmethod` decorator is used when a method belongs to a class **but doesn't need access to the class (`cls`) or instance (`self`)**. It behaves like a regular function but is grouped inside the class for organizational purposes.

### 1. Why?

* A static method doesn’t access or modify class or instance attributes.
* It logically belongs to the class’s domain, but it doesn’t need to know about the class or instance.
* Using `@staticmethod` lets you call it as `MyClass.some_utility_method()`, without needing an instance and without having access to `self` or `cls`.

### 2. In summary:

`@staticmethod` is for utility/helper methods that are conceptually related to a class but don't depend on the class or instance.
It's often used for things like validation, calculations, or formatting logic that supports the class but doesn't use its data.

### 3. Different between `@staticmethod` and `@classmethod`

| Feature              | `@classmethod`           | `@staticmethod`          |
| -------------------- | ------------------------ | ------------------------ |
| First argument       | `cls`                    | None                     |
| Access class data    | ✅ Yes                    | ❌ No                     |
| Access instance data | ❌ No                     | ❌ No                     |
| Supports subclassing | ✅ Yes                    | ❌ No (hardcoded class)   |
| Typical use          | Alternative constructors | Utility/helper functions |
| Needs class context? | ✅ Yes                    | ❌ No                     |

---

## defaultdict

`defaultdict(list)` creates a dictionary where each value is a list by default.

- **Type:** `collections.defaultdict[list]`
- **Behavior:** If you access a key that doesn’t exist, it automatically creates an empty list for that key.

**Example:**
```python
from collections import defaultdict

d = defaultdict(list)
d["a"].append(1)
print(d)  # Output: defaultdict(<class 'list'>, {'a': [1]})
```

So, it acts like a regular `dict`, but every value is a list, and you don’t need to initialize the list before appending.

> Similar to `defaultdict(set)`, `defaultdict(dict)`

---

## zip()

- `zip()` pairs each section’s start with the next section’s start, so you can get the start and end positions for each block.

```python
for (anchor, type_name, start), (_, _, end) in zip(types, types[1:])"
    block = markdown[start:end]

# Example:
# types = [
#     ('anchor1', 'Type1', 100),
#     ('anchor2', 'Type2', 200),
#     ('anchor3', 'Type3', 300),
#     # sentinel: (None, None, len(markdown))
# ]

# zip(types, types[1:]) -->
#     ((anchor1, Type1, 100), (anchor2, Type2, 200))
#     ((anchor2, Type2, 200), (anchor3, Type3, 300))
#     ((anchor3, Type3, 300), (None, None, len(markdown)))
```

---

## Asynchronous

The `asyncio` in Python is library that supports **Asynchronous**

### 1. Event Loop

- The **event loop** is the core mechanism of `asyncio` in Python.
- It’s a loop that manages and dispatches (send) all the asynchronous tasks (coroutines, I/O operations, etc.) by scheduling and switching between them — **without using threads**.
- No thread usage
- No run **Parallelly** --> **Concurrently** run instead (still only run on main process)

### 2. How Event Loop Works

1. User schedule one or more coroutines, with:
- `coroutine`: Functions that use async def and await to yield control
- `await`: Pauses execution so the loop can run other tasks
- `create_task()`: run tasks "in the background" (still single-threaded)
- `run()`: Starts and stops event loop
2. The **event loop** picks one ready coroutine --> runs it **until coroutine hits** `await`.
3. While that coroutine waits (for I/O, sleep, etc.), the event loop **runs the next ready task**.
4. When the first coroutine’s awaitable is ready (e.g., sleep finished, I/O is done), the event loop resumes it.
5. This loop continues until all coroutines are done.

### 3. What Is the Event Loop Doing?

Internally, the event loop maintains several queues, creating tick like this:

```text
+-------------------------+
| Event Loop Tick Begins |
+-------------------------+
           |
           v
   Process ready tasks
           |
           v
  Check for I/O completion
           |
           v
     Run scheduled callbacks
           |
           v
  Sleep/wait for next event
           |
           v
+-------------------------+
| Event Loop Tick Ends   |
+-------------------------+

```

### 4. Scheduling & Switching

How tasks are scheduled over time by the event loop:

```text
Time ────────────────────────────────────────▶

Loop Tick 1:
  [Task A] runs until it hits `await` ─────┐
                                           ▼
Loop Tick 2:
  [Task B] starts running, hits `await` ───┐
                                           ▼
Loop Tick 3:
  [Task A] resumes after await completes ──┐
                                           ▼
Loop Tick 4:
  [Task B] resumes after await completes ──┐
                                           ▼
Loop Tick 5:
  [Task C] scheduled, runs, hits await ────┐
                                           ▼
... and so on

```

### 5. Example

```python
import asyncio

async def say_hello():
    while True:
        print("Hello!")
        await asyncio.sleep(1)  # sleep for 1 seconds

async def say_world():
    while True:
        print("World!")
        await asyncio.sleep(2)  # sleep for 2 seconds

async def main():
    asyncio.create_task(say_hello())
    asyncio.create_task(say_world())
    await asyncio.sleep(10)  # run for 10 seconds

asyncio.run(main())

# Hello!
# World!
# Hello!
# Hello!
# World!
# Hello!
# Hello!
# ...

```

### 6. `asyncio.run(...)` vs `asyncio.create_task(...)`

| Feature                           | `asyncio.run()`                       | `asyncio.create_task()`                        |
| --------------------------------- | ------------------------------------- | ---------------------------------------------- |
| Where it's used                   | Top-level entry point (like `main()`) | Inside an already-running async function       |
| Starts the event loop?            | ✅ Yes (starts and stops event loop)   | ❌ No (requires event loop already running)     |
| Returns                           | Result of the coroutine               | Task object (you don’t `await` it immediately) |
| For long-running background tasks | ❌ Not ideal (it blocks)               | ✅ Yes — runs concurrently in background        |
| Example use case                  | Run a coroutine in a CLI or script    | Fire off a background job in FastAPI           |

---

