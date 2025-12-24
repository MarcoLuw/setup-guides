## 🔀 2. Types of Inheritance

### 🔹 Single Inheritance

A child class inherits directly from one parent class. It gets access to all methods and attributes of the parent.

```python
class A:
    def greet(self):
        print("Hello from A")

class B(A):
    pass

b = B()
b.greet()  # Inherited from A
print(B.mro())
```

#### ✅ Output:

```
Hello from A
[<class '__main__.B'>, <class '__main__.A'>, <class 'object'>]
```

#### 🔁 MRO Order:

`B → A → object`

#### 💡 Explanation:

Class `B` does not define its own `greet()` method, so Python looks in its parent class `A` and finds it.

#### 🧠 Quick Tip:

Check the child class — if it doesn’t override a method, the parent’s method is automatically used.

---

### 🔹 Multiple Inheritance

A class inherits from more than one parent class. Python resolves ambiguity through MRO (Method Resolution Order).

```python
class A:
    def method(self):
        print("Method from A")

class B:
    def method(self):
        print("Method from B")

class C(A, B):
    pass

c = C()
c.method()
print(C.mro())
```

#### ✅ Output:

```
Method from A
[<class '__main__.C'>, <class '__main__.A'>, <class '__main__.B'>, <class 'object'>]
```

#### 🔁 MRO Order:

`C → A → B → object`

#### 💡 Explanation:

Although both `A` and `B` define `method()`, Python uses MRO to resolve ambiguity. Since `A` comes before `B` in the definition of `C(A, B)`, Python picks `A.method()`.

#### 🧠 Quick Tip:

Use `print(C.mro())` to inspect the MRO order Python will follow.

---

### 🔹 Multilevel Inheritance

A chain of inheritance, where a class inherits from a child class of another class.

```python
class A:
    def show(self):
        print("Show from A")

class B(A):
    def display(self):
        print("Display from B")

class C(B):
    def render(self):
        print("Render from C")

c = C()
c.show()
c.display()
c.render()
print(C.mro())
```

#### ✅ Output:

```
Show from A
Display from B
Render from C
[<class '__main__.C'>, <class '__main__.B'>, <class '__main__.A'>, <class 'object'>]
```

#### 🔁 MRO Order:

`C → B → A → object`

#### 💡 Explanation:

`C` inherits from `B`, which inherits from `A`, so all methods from `A` and `B` are available in `C`.

#### 🧠 Quick Tip:

Trace the inheritance chain upward to see what’s inherited. Python searches from `C → B → A`.

---

### 🔹 Hierarchical Inheritance

Multiple child classes inherit from a single parent class.

```python
class A:
    def msg(self):
        print("Message from A")

class B(A):
    pass

class C(A):
    pass

b = B()
c = C()
b.msg()
c.msg()
print(B.mro())
print(C.mro())
```

#### ✅ Output:

```
Message from A
Message from A
[<class '__main__.B'>, <class '__main__.A'>, <class 'object'>]
[<class '__main__.C'>, <class '__main__.A'>, <class 'object'>]
```

#### 🔁 MRO Order:

`B → A → object` `C → A → object`


#### 💡 Explanation:
Both `B` and `C` independently inherit from `A`, so they both get access to `msg()`.

#### 🧠 Quick Tip:
If multiple classes inherit from a single base class, each gets a separate copy of its methods unless overridden.

---

### 🔹 Hybrid Inheritance
A combination of multiple types of inheritance. It can lead to the diamond problem, handled by C3 MRO.

```python
class A:
    def ping(self):
        print("Ping from A")

class B(A):
    pass

class C(A):
    pass

class D(B, C):
    pass

d = D()
d.ping()
print(D.mro())
````

#### ✅ Output:

```
Ping from A
[<class '__main__.D'>, <class '__main__.B'>, <class '__main__.C'>, <class '__main__.A'>, <class 'object'>]
```

#### 🔁 MRO Order:

`D → B → C → A → object`

#### 💡 Explanation:

Even though both `B` and `C` inherit from `A`, Python’s C3 MRO ensures `A` is only called once in the resolution path for `D`.

#### 🧠 Quick Tip:

To understand what path Python takes, use `print(D.mro())`.

---

