package org.kendar;

@FunctionalInterface
public interface ExceptionSupplier {
    Object run() throws Exception;
}
