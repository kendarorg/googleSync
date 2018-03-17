package org.kendar.utils;

@FunctionalInterface
public interface GD2Consumer<T> {
    void run(T param) throws Exception;
}
