package org.enel.utils;

import com.google.api.services.drive.Drive;

@FunctionalInterface
public interface ExceptionSupplier {
    Object run(Drive service) throws Exception;
}
