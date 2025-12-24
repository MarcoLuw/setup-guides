## **GIL - Python's Global Interpreter Lock**
The Global Interpreter Lock (GIL) is a mutex that protects access to Python objects, preventing multiple threads from executing Python bytecodes at once. 

- This means that in a multi-threaded Python program, even if you have multiple threads, **only one thread can execute Python code at a time.**
- To take full advantage of multi-core processors, you need to use multiple processes in Python