package homework5.stage;

import homework5.Car;

import java.util.concurrent.Semaphore;

public class Tunnel extends homework5.stage.Stage {

    private Semaphore smp;

    public Tunnel(int maxCars) {
        smp = new Semaphore(maxCars);
        length = 80;
        description = "Тоннель " + length + " метров";
    }

    @Override
    public void go(Car c) {
        try {
            try {
                System.out.println(c.getName() + " готовится к этапу(ждет): " + description);
                smp.acquire();
                System.out.println(c.getName() + " начал этап: " + description);
                Thread.sleep(length / c.getSpeed() * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println(c.getName() + " закончил этап: " + description);
                smp.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}