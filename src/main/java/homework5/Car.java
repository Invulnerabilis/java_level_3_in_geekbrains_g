package homework5;

import homework5.stage.Stage;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Car implements Runnable {

    private static int CARS_COUNT;
    private static CyclicBarrier preparation;
    private static CyclicBarrier finish;

    private static Car winner = null;

    private Race race;
    private int speed;
    private String name;


    public Car(int speed) {
        CARS_COUNT++;
        this.speed = speed;
        this.name = "Участник #" + CARS_COUNT;
    }

    public Car(Race race, int speed) {
        this(speed);
        this.race = race;
    }

    public static void setFinishBarrier(CyclicBarrier finish) {
        Car.finish = finish;
    }

    public static void setPreparationBarrier(CyclicBarrier preparation) {
        Car.preparation = preparation;
    }

    public String getName() {
        return name;
    }

    public int getSpeed() {
        return speed;
    }

    public void setRace(Race race) {
        this.race = race;
    }

    @Override
    public void run() {
        try {
            preparation();
            preparation.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            startRace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void preparation() throws InterruptedException {
        System.out.println(name + " готовится");
        Thread.sleep(500 + (int) (Math.random() * 800));
        System.out.println(name + " готов");
    }

    private void startRace() throws BrokenBarrierException, InterruptedException {
        for (Stage stage : race.getStages()) {
            stage.go(this);
        }
        synchronized (finish) {
            if (winner == null) {
                System.out.println("Победитель " + name);
                winner = this;
            }
        }
        finish.await();
    }

    public static Car getWinner() {
        return winner;
    }

    @Override
    public String toString() {
        return "Car={ speed=" + speed + ", name='" + name + '}';
    }
}