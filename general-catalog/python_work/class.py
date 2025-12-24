class Car:
    def __init__(self, name, year):
        self.name = name
        self.year = year
    def info(self):
        print(f'Car info: {self.name}, manufactured in {self.year}')

a_car = Car('honda', '2020')
a_car.info()

class HybridCar(Car):
    def __init__(self, name, year):
        super().__init__(name, year)
    def fake_info(self):
        self.info()
        print('Hi, it from self')
    def info(self):
        print('Hi, it info() from this object, not from the parent')

f_car = HybridCar('suzuki', '2025')
f_car.fake_info()

class HybridCar_1(Car):
    def __init__(self, name, year):
        super().__init__(name, year)
    def fake_info(self):
        super().info()
        print('Hi, it from super()')

f_car_1 = HybridCar_1('yamaha', '2018')
f_car_1.fake_info()