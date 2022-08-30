package homework6_task_2_3;

import java.util.Arrays;

/*
Java. Уровень 3. Урок 6.
Домашнее задание 6. "Обзор средств разработки. Тестирование".

2. Написать метод, которому в качестве аргумента передаётся не пустой одномерный целочисленный массив.
Метод должен вернуть новый массив, который получен путём вытаскивания из исходного массива элементов, идущих после последней четвёрки.
Входной массив должен содержать хотя бы одну четвёрку, иначе в методе необходимо выбросить RuntimeException.
Написать набор тестов для этого метода (по 3-4 варианта входных данных).
Вх: [ 1 2 4 4 2 3 4 1 7 ] -> вых: [ 1 7 ].

3. Написать метод, который проверяет состав массива из чисел 1 и 4.
Если в нём нет хоть одной четвёрки или единицы, то метод вернёт false;
Написать набор тестов для этого метода (по 3-4 варианта входных данных).
*/

public class ArrayUtils {

    public static int[] getArrayAfterNumberFour(int[] array) {
        for (int i = array.length - 1; i >= 0; i--) {
            if (array[i] == 4) {
                return Arrays.copyOfRange(array, i + 1, array.length);
            }
        }
        throw new RuntimeException("Have not number 4");
    }

    public static boolean isArrayContainsOneOrFour(int[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == 4 || array[i] == 1) {
                return true;
            }
        }
        return false;
    }
}
