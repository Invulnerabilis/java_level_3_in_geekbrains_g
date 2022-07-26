package homework1;

import java.util.Arrays;
import java.util.List;

public class GenericUtils {
    static <T> void swap(T[] array, int first, int second) {
        T object = array[first];
        array[first] = array[second];
        array[second] = object;
    }

    static <T> List<T> toArrayList(T[] array) {
        return Arrays.asList(array);
    }
}
