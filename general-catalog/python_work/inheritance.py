class A:
    def ping(self):
        print("Ping from A")

class B(A):
    pass

class C(B,A):
    pass

class D(B,A):
    pass

class E(D,B):
    pass
e = E()
e.ping()
print(E.mro())