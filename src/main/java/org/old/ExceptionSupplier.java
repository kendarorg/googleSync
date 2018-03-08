package org.old;

import com.google.api.services.drive.Drive;

@FunctionalInterface
public interface ExceptionSupplier {
    Object run() throws Exception;
}
