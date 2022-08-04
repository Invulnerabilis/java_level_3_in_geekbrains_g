package homework4;

/*
Java. Уровень 3. Урок 4.
Домашнее задание 4. "Многопоточность. Часть I".

Поработать с выводом букв на экран.

Создать три потока каждый из которых выводит определённую букву (A, B и C) пять раз (порядок – ABСABСABС), используя при этом wait/notify/notifyAll.
*/

public class WaitNotifyClass {
    private final Object mon = new Object();
    private static int COUNT = 5;
    private volatile char currentLetter = 'A';

    public static void main(String[] args) {
        WaitNotifyClass waitNotifyObj = new WaitNotifyClass();
        Thread threadA = new Thread(() -> {
            waitNotifyObj.printSymbol('A', 'B');
        });
        Thread threadB = new Thread(() -> {
            waitNotifyObj.printSymbol('B', 'C');
        });
        Thread threadC = new Thread(() -> {
            waitNotifyObj.printSymbol('C', 'A');
        });
        threadA.start();
        threadB.start();
        threadC.start();
    }

    public void printSymbol(char curr, char next) {
        synchronized (mon) {
            try {
                for (int i = 0; i < COUNT; i++) {
                    while (currentLetter != curr) {
                        mon.wait();
                    }
                    System.out.print(curr);
                    currentLetter = next;
                    mon.notifyAll();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
