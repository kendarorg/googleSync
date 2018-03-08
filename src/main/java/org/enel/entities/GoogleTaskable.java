package org.enel.entities;

import org.enel.utils.GDException;

@FunctionalInterface
public interface GoogleTaskable {
    void run() throws GDException;
}
