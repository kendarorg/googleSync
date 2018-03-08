package org.enel.utils;

public class ValueContainer<T> {
    public ValueContainer(T value){
        this.value = value;
    }
    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
