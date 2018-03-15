package org.kendar.utils;

@FunctionalInterface
public interface GD2Consumer<T> {
    <T> void run(T param) throws Exception;
}
