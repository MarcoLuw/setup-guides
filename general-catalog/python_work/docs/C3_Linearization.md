
# C3 Linearization in Python

## Introduction

C3 Linearization, or C3 superclass linearization, is the algorithm used by Python to determine the **Method Resolution Order (MRO)** in a class hierarchy. It is particularly useful when multiple inheritance is used, ensuring a clear and predictable order in which classes are considered when looking for a method.

## Why C3 Linearization Matters

- **Predictability**: C3 Linearization provides a consistent order for resolving methods, even in complex multiple inheritance situations.
- **Conflict Resolution**: It avoids ambiguity by providing a clear method resolution order, preventing issues like the **Diamond Problem**.

## How C3 Linearization Works

1. **Topological Sorting**: The algorithm produces a topological ordering of the classes.
2. **Rule**: A base class is added only if it does not conflict with another base class in the order.
3. **No Circular Dependencies**: C3 Linearization ensures that classes are checked in a predictable order without circular inheritance.

---

## 🔁 MRO Formula

Given a class `C` with parents `P1, P2, ..., Pn`:

```text
L[C] = [C] + merge(L[P1], L[P2], ..., L[Pn], [P1, P2, ..., Pn])
```

Where:
- `L[X]` = MRO of class X
- `merge()` = special merge function explained below

---

## ⚙️ How `merge()` Works

The merge function combines lists **while preserving**:

- The declared order
- No duplication
- No conflicting orders

### ✅ Rule:
> **Pick the first head of the lists that is not in the tail of any other list.**

Then remove it from all lists and repeat.

If no valid head exists, raise a `TypeError`.

---

## 🧪 Example: Basic Hierarchy

```python
class A: pass
class B(A): pass
class C(A): pass
class D(B, C): pass
```

### Step 1: Gather MROs
```text
L[A] = [A, object]
L[B] = [B, A, object]
L[C] = [C, A, object]
```

### Step 2: Compute L[D]
```text
L[D] = [D] + merge([B, A, object], [C, A, object], [B, C])
```

### Step 3: Merge Process

**Lists to merge:**
```text
[B, A, object]
[C, A, object]
[B, C]
```

**Round 1:**
- Heads: B, C, B
- ✅ B is not in any tail → choose B
- Remove B → Remaining: [A, object], [C, A, object], [C]

**Round 2:**
- Heads: A, C, C
- ✅ C is not in any tail → choose C
- Remove C → Remaining: [A, object], [A, object], []

**Round 3:**
- Heads: A, A
- ✅ A is not in any tail → choose A
- Remove A → Remaining: [object], [object]

**Round 4:**
- Heads: object, object
- ✅ object is not in any tail → choose object

✅ Final MRO:
```python
(D, B, C, A, object)
```

---

## 💥 Conflict Example

```python
class X: pass
class Y: pass
class A(X, Y): pass
class B(Y, X): pass
class C(A, B): pass  # ❌ Error
```

This results in:
```text
TypeError: Cannot create a consistent method resolution order (MRO) for bases A, B
```

Because:
- A requires X before Y
- B requires Y before X
- → C cannot satisfy both

---


## C3 Linearization with `super()`

The `super()` function works with C3 Linearization to resolve which class method to call next in the MRO (not required a parent):

```python
class A:
    def method(self):
        print("A method")

class B(A):
    def method(self):
        print("B method")
        super().method()

class C(A):
    def method(self):
        print("C method")
        super().method()

class D(B, C):
    pass

d = D()
d.method()
```

Output:

```bash
B method
C method
A method
```

## Conclusion

C3 Linearization in Python provides a predictable and conflict-free method resolution order, especially when dealing with multiple inheritance. It ensures that the `super()` function works as expected, respecting the MRO defined by the C3 algorithm.