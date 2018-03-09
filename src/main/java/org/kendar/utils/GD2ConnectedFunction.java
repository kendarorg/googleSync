package org.kendar.utils;

import com.google.api.services.drive.Drive;

@FunctionalInterface
public interface GD2ConnectedFunction {
    Object run(Drive drive) throws Exception;
}
