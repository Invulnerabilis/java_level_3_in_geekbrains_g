package homework5;

/*
Java. Уровень 3. Урок 5.
Домашнее задание 5. "Многопоточность. Часть II".

Организуем гонки:
Все участники должны стартовать одновременно, несмотря на то, что на подготовку у каждого из них уходит разное время.
В туннель не может заехать одновременно больше половины участников (условность).
Попробуйте всё это синхронизировать.
Только после того как все завершат гонку, нужно выдать объявление об окончании.
Можете корректировать классы (в т.ч. конструктор машин) и добавлять объекты классов из пакета util.concurrent.
*/

import homework5.stage.Road;
import homework5.stage.Tunnel;

import java.util.concurrent.CyclicBarrier;

public class MainClass {
    public static final int CARS_COUNT = 4;

    public static void main(String[] args) {
        System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Подготовка!!!");
        Race race = createDefaultRace();

        CyclicBarrier finishBarrier = createFinishBarrier();
        Car.setFinishBarrier(finishBarrier);

        CyclicBarrier preparationBarrier = createPreparationBarrier();
        Car.setPreparationBarrier(preparationBarrier);

        Car[] cars = createCars();
        for (Car car : cars) {
            car.setRace(race);
        }
        for (Car car : cars) {
            new Thread(car).start();
        }

        waitCars(preparationBarrier);
        System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка началась!!!");

        waitCars(finishBarrier);
        System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка закончилась!!!");

        System.out.println(Car.getWinner());
    }

    private static Race createDefaultRace() {
        return new Race(new Road(60), new Tunnel(CARS_COUNT / 2), new Road(40));
    }

    private static CyclicBarrier createFinishBarrier() {
        return new CyclicBarrier(CARS_COUNT + 1);
    }

    private static CyclicBarrier createPreparationBarrier() {
        return new CyclicBarrier(CARS_COUNT + 1);
    }

    private static Car[] createCars() {
        Car[] cars = new Car[CARS_COUNT];
        for (int i = 0; i < cars.length; i++) {
            cars[i] = new Car(20 + (int) (Math.random() * 10));
        }
        return cars;
    }

    private static void waitCars(CyclicBarrier barrier) {
        try {
            barrier.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}