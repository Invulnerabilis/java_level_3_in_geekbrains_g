package homework1.fruit;

public abstract class Fruit {
    protected final float weight;

    public Fruit(float mass) {
        this.weight = mass;
    }

    public float getWeight() {
        return weight;
    }
}
