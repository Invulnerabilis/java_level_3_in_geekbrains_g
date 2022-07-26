package homework1;

/*
Java. Уровень 3. Урок 1. Домашнее задание "Обобщения".

1. Написать метод, который меняет два элемента массива местами (массив может быть любого ссылочного типа);

2. Написать метод, который преобразует массив в ArrayList;

3. Большая задача:

a) Есть классы Fruit -> Apple, Orange (больше фруктов не надо);

b) Класс Box в который можно складывать фрукты, коробки условно сортируются по типу фрукта, поэтому в одну коробку нельзя сложить и яблоки, и апельсины;

c) Для хранения фруктов внутри коробки можете использовать ArrayList;

d) Сделать метод getWeight() который высчитывает вес коробки, зная количество фруктов и вес одного фрукта (вес яблока - 1.0f, апельсина - 1.5f, не важно в каких это единицах);

e) Внутри класса коробка сделать метод compare, который позволяет сравнить текущую коробку с той, в которую подадут compare в качестве параметра, true - если их веса равны, false в противном случае (коробки с яблоками мы можем сравнивать с коробками с апельсинами);

f) Написать метод, который позволяет пересыпать фрукты из текущей коробки в другую коробку (помним про сортировку фруктов, нельзя яблоки высыпать в коробку с апельсинами), соответственно в текущей коробке фруктов не остаётся, а в другую перекидываются объекты, которые были в этой коробке;

g) Не забываем про метод добавления фрукта в коробку.
*/

import homework1.box.Box;
import homework1.fruit.Apple;
import homework1.fruit.Orange;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        Integer[] arr = {1, 2, 3, 4, 5, 6, 7, 8, 9};

        //task1
        GenericUtils.swap(arr, 1, 8);
        System.out.println(Arrays.toString(arr));

        //task2
        System.out.println(GenericUtils.toArrayList(arr));

        //task3
        Box<Apple> appleBox = new Box<>(new Apple(), new Apple(), new Apple());
        Box<Orange> orangeBox = new Box<>(new Orange(), new Orange(), new Orange());
        Box<Orange> orangeBox2 = new Box<>(new Orange(), new Orange(), new Orange());
        orangeBox2.moveToOtherBox(orangeBox);
        orangeBox.add(new Orange());

        System.out.println(appleBox.getWeight());
        System.out.println(orangeBox.getWeight());
        System.out.println(orangeBox2.getWeight());
        System.out.println(appleBox.compare(orangeBox));


    }
}
