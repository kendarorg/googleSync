package org;

import org.kendar.entities.GD2DriveItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ResGD2Drive {
    public static List<GD2DriveItem> loadFromRes(String path){
        List<GD2DriveItem> result = new ArrayList<>();
        ClassLoader classLoader = ResGD2Drive.class.getClassLoader();
        File file = new File(classLoader.getResource(path).getFile());


        try (Scanner scanner = new Scanner(file)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                //result.append(line).append("\n");
                throw new IOException();
            }

            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
