package homework1.box;

import homework1.fruit.Fruit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Box<T extends Fruit> {
    private final List<T> fruitList;

    public Box(List<T> fruitList) {
        this.fruitList = fruitList;
    }

    public Box(T... fruits) {
        this.fruitList = new ArrayList<>(Arrays.asList(fruits));
    }

    public void add(T fruit) {
        fruitList.add(fruit);
    }

    public List<T> getFruitList() {
        return fruitList;
    }

    public float getWeight() {
        if (fruitList.size() > 0)
            return fruitList.size() * fruitList.get(0).getWeight();
        return 0;
    }

    public void moveToOtherBox(Box<T> other) {
        for (T fruit : fruitList) {
            other.add(fruit);
        }
        fruitList.clear();
    }

    public <V extends Fruit> boolean compare(Box<V> other) {
        if (this == other) {
            return true;
        }
        return this.getWeight() == other.getWeight();
    }

    @Override
    public String toString() {
        return "Box{" +
                "fruitList=" + fruitList +
                '}';
    }
}
