package org.utils;

import org.apache.poi.ss.usermodel.*;
import org.kendar.entities.GD2DriveItem;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class PoiService {

    public static GD2DriveItem loadFile(String filePath,boolean local){
        Stack<GD2DriveItem> items = new Stack<>();
        try {
            ClassLoader classLoader = PoiService.class.getClassLoader();
            File file = new File(classLoader.getResource(filePath).getFile());

            Workbook workbook = WorkbookFactory.create(file);
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter dataFormatter = new DataFormatter();


            int prevLevel = 1;
            boolean first=true;
            for (Row row: sheet) {
                if(first){
                    first = false;
                    continue;
                }

                int firstCellNum=row.getFirstCellNum();
                boolean cellFound = false;
                for(;firstCellNum<row.getLastCellNum();firstCellNum++) {
                    String cellValue = getCellValue(dataFormatter, row, firstCellNum);
                    if(cellValue==null || cellValue.trim().length()==0){
                        continue;
                    }
                    cellFound = true;
                    break;
                }
                if(!cellFound)continue;
                int level = firstCellNum;
                GD2DriveItem item = new GD2DriveItem();
                item.setId(getCellValue(dataFormatter, row, firstCellNum));
                item.setName(getCellValue(dataFormatter, row, firstCellNum+1));

                item.setCreatedTime(getInstant(getCellValue(dataFormatter, row, firstCellNum+2)));
                item.setModifiedTime(getInstant(getCellValue(dataFormatter, row, firstCellNum+3)));

                item.setSize(getLong(getCellValue(dataFormatter, row, firstCellNum+4)));

                item.setMd5(getCellValue(dataFormatter, row, firstCellNum+5));

                item.setThrashed(getBool(getCellValue(dataFormatter, row, firstCellNum+6)));
                item.setLocal(local);

                if(item.getSize()==0){
                    item.setDir(true);
                }
                if(level==0){
                    items.push(item);
                }else {
                    while(level<items.size()){
                        items.pop();
                    }
                    items.peek().addChild(item);
                    items.push(item);
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        while(items.size()>1)items.pop();
        return items.pop();
    }

    private static Instant getInstant(String val){
        if(val==null||val.trim().isEmpty())return Instant.now();
        return Instant.ofEpochSecond(
                Integer.parseInt(val.trim()));
    }

    private static long getLong(String val){
        if(val==null||val.trim().isEmpty())return 0L;
        return Long.parseLong(val.trim());
    }
    private static boolean getBool(String val){
        try {
            if (val == null || val.trim().isEmpty()) return false;
            return Boolean.parseBoolean(val.trim());
        }catch(Exception ex){
            return false;
        }
    }

    private static String getCellValue(DataFormatter dataFormatter, Row row, int firstCellNum) {
        Cell cell = row.getCell(firstCellNum);
        String result= dataFormatter.formatCellValue(cell);
        if(result!=null && result.equalsIgnoreCase("EMPTY")){
            return "";
        }
        return result;
    }
}
