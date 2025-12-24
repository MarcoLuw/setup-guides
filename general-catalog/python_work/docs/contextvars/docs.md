# ContextVars Documentation

### Module:

```python
import contextvars
```


## 1. **Main Components**

### `ContextVar`

* Represents a **context-local variable**.
* Keeps separate values for each execution context (thread, task, etc.).
* Think of it like `threading.local()` but for both threads **and** coroutines.

```python
my_var = contextvars.ContextVar("my_var", default=None)
```


## 2. **Common Methods**

### 1. `.get()` — Get the current value in this context

```python
print(user.get())  # Output: anonymous
```
- Returns the value of `user` for the current thread or coroutine context.


### 2. `.set(value)` — Set a value for the current context

```python
token = user.set("alice")
print(user.get())  # Output: alice
```
- Sets the value of `user` in this context.
- Returns a **Token** object that stores the previous value (`"anonymous"` in this case).

### 3. `.reset(token)` — Reset to previous value

```python
user.reset(token)
print(user.get())  # Output: anonymous
```
- Restores the variable to what it was before `set()`.


### 4. `Token` — The return value from `.set()`

```python
token = user.set("bob")  # token remembers previous value
print(token.old_value)   # anonymous (only available internally)
user.reset(token)
```
- You usually **don’t access `token.old_value` directly**, but you use the `token` to **undo** the `.set()`.

## 3. **Context & Isolation Rules**

* Each **thread** or **async task** gets its **own isolated value**.
* `await` and `asyncio.gather()` do **not** lose the context.
* Values do **not** leak between tasks.

## 4. **Example**

```python
from contextvars import ContextVar

user_id = ContextVar("user_id", default="guest")

def log():
    print("User:", user_id.get())

token = user_id.set("alice")
log()  # → User: alice
user_id.reset(token)
log()  # → User: guest
```

## 5. **When to Use It**

Use `ContextVar` when you need **per-request/session context** in:

* `asyncio` web servers (e.g. FastAPI, Starlette)
* Logging contextual info (e.g., request ID, user ID)
* Tracing, auditing, etc.
* Replacing `threading.local()` in async code


## 🚫 Notable Limitations

* Not inherited across threads unless explicitly copied.
* Must **manually reset** values if you're managing stack-like changes.
* Not designed for shared mutable state — always **context-specific**.

<br />
<br />
<br />

# Illustration: `ContextVar` in multithreaded task-based version

A **detailed ASCII diagram** that explains how `ContextVar.set()` and `ContextVar.reset()` work across different **execution contexts** (like threads or async tasks). It also shows how the `token` helps manage previous values.


## **ASCII Diagram: `ContextVar` in Different Contexts**

### Initial setup:

```python
from contextvars import ContextVar
current_ticket = ContextVar("current_ticket", default=None)
```


## 💡 BEFORE ANY SET (all contexts see default)

```
                +-----------------------+
                |  ContextVar:          |
                |  current_ticket       |
                |  default = None       |
                +-----------------------+

Contexts:
Thread A ──> current_ticket.get()  -->  None
Thread B ──> current_ticket.get()  -->  None
Main      ─> current_ticket.get()  -->  None
```


## 📝 THREAD A: Set ticket to TICKET-123

```python
token_a = current_ticket.set("TICKET-123")
```

```
                +-----------------------+
                |  current_ticket       |
                |  default = None       |
                +-----------------------+

Contexts:
Thread A ──> current_ticket.get()  -->  "TICKET-123"
Thread B ──> current_ticket.get()  -->  None
Main      ─> current_ticket.get()  -->  None
```

🔁 Behind the scenes, the context variable stores:

```
Thread A Context:
  current_ticket = "TICKET-123"
  token_a -> remembers: None
```


## 📝 THREAD B: Set ticket to TICKET-XYZ

```python
token_b = current_ticket.set("TICKET-XYZ")
```

```
Contexts:
Thread A ──> current_ticket.get()  -->  "TICKET-123"
Thread B ──> current_ticket.get()  -->  "TICKET-XYZ"
Main      ─> current_ticket.get()  -->  None
```

Each context is isolated! 🎯

## 🧹 RESET IN THREAD A:

```python
current_ticket.reset(token_a)
```

```
Thread A Context:
  current_ticket = token_a.old_value = None
```

```
Contexts:
Thread A ──> current_ticket.get()  -->  None
Thread B ──> current_ticket.get()  -->  "TICKET-XYZ"
Main      ─> current_ticket.get()  -->  None
```


## 📦 RESET IN THREAD B:

```python
current_ticket.reset(token_b)
```

Now:

```
Contexts:
Thread A ──> current_ticket.get()  -->  None
Thread B ──> current_ticket.get()  -->  None
Main      ─> current_ticket.get()  -->  None
```


## 🧠 TL;DR FLOW VISUALIZATION

```text
[BEFORE SET]                 [AFTER SET]                    [AFTER RESET]
  Context A                    Context A                      Context A
  ┌────────────┐              ┌────────────┐                ┌────────────┐
  │ None       │ ───── set ─▶ │ TICKET-123 │ ───── reset ─▶ │ None       │
  └────────────┘              └────────────┘                └────────────┘

  Context B                    Context B                      Context B
  ┌────────────┐              ┌────────────┐                ┌────────────┐
  │ None       │ ───── set ─▶ │ TICKET-XYZ │ ───── reset ─▶ │ None       │
  └────────────┘              └────────────┘                └────────────┘
```


## 🔑 Key Concepts

* `ContextVar` keeps a **separate value per context** (like per thread or per async task).
* `set()` returns a `Token` that **remembers the previous value**.
* `reset(token)` puts it back as if the `set()` never happened.

<br />
<br />
<br />

# Illustration: `ContextVar` in async task-based version

A **text-based visualization** of how `ContextVar` behaves in an **`asyncio` environment** with coroutines (async tasks).

This is very similar to the thread-based example but adapted to **async task scheduling**, which is even trickier because tasks may **run concurrently in a single thread** and yield control back and forth.


## ContextVar in `asyncio` — Full Breakdown with Diagram

### 🧱 Setup:

```python
import asyncio
from contextvars import ContextVar

current_ticket = ContextVar("current_ticket", default=None)
```


## 🔁 Code Simulation

```python
async def handle_ticket(ticket_id):
    token = current_ticket.set(ticket_id)
    print(f"[{ticket_id}] Before await: {current_ticket.get()}")
    await asyncio.sleep(0.1)
    print(f"[{ticket_id}] After await: {current_ticket.get()}")
    current_ticket.reset(token)
    print(f"[{ticket_id}] After reset: {current_ticket.get()}")

async def main():
    await asyncio.gather(
        handle_ticket("TICKET-A"),
        handle_ticket("TICKET-B")
    )

asyncio.run(main())
```


## 📊 ASYNC TASK CONTEXT FLOW

```
        ┌────────────────────────┐
        │ ContextVar:            │
        │   current_ticket       │
        │   default = None       │
        └────────────────────────┘
```


## 🌀 Execution Timeline (Simplified View)

```
[ Task: handle_ticket("TICKET-A") ]
  1. set("TICKET-A") ➜ token_a
  2. current_ticket = "TICKET-A"
  3. await asyncio.sleep(0.1) yields control

[ Task: handle_ticket("TICKET-B") ]
  1. set("TICKET-B") ➜ token_b
  2. current_ticket = "TICKET-B"
  3. await asyncio.sleep(0.1) yields control

[ Task: handle_ticket("TICKET-A") resumes ]
  4. current_ticket.get() ➜ "TICKET-A"
  5. reset(token_a)
  6. current_ticket.get() ➜ None

[ Task: handle_ticket("TICKET-B") resumes ]
  4. current_ticket.get() ➜ "TICKET-B"
  5. reset(token_b)
  6. current_ticket.get() ➜ None
```


## 🎭 Visual Timeline

```text
TIME ─────────────────────────────────────────────▶

[Task A]
 ┌────────────┐
 │ set("A")   │──▶ current_ticket = "A"
 │ await      │──▶ yields to event loop
 │ resumes    │──▶ current_ticket = still "A"
 │ reset      │──▶ current_ticket = None
 └────────────┘

[Task B]
          ┌────────────┐
          │ set("B")   │──▶ current_ticket = "B"
          │ await      │──▶ yields to event loop
          │ resumes    │──▶ current_ticket = still "B"
          │ reset      │──▶ current_ticket = None
          └────────────┘
```

Notice: when `Task A` is running, `current_ticket.get()` returns `"A"`, and when `Task B` is running, it returns `"B"`, even though both are running in **the same thread**!


# ✅ Summary: Async ContextVar Recap

| Feature                     | Description                                                                   |
| --------------------------- | ----------------------------------------------------------------------------- |
| **Per-Task Isolation**      | Each async task sees its own value of the context variable.                   |
| **Preserved Across Awaits** | `ContextVar` context is saved and restored properly when task yields.         |
| **No Globals or Locks**     | You avoid shared mutable state completely — no `global`, no `threading.Lock`. |
| **Reset Is Important**      | Cleanup restores the context so other logic doesn’t see leftovers.            |
