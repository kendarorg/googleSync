package org.enel;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.SystemUtils;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.kendar.utils.PathUtils;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

@RunWith(JUnit4.class)
public class PathTest {
    private void allowWin() throws Exception {
        if(SystemUtils.IS_OS_UNIX){
            throw new Exception("crap");
        }
    }

    @Test
    public void testWin1() throws Exception {
        allowWin();

        String path = "C:"+ PathUtils.SEPARATOR+"temp";
        Path res = Paths.get(path);

        assertEquals("C:\\temp",res.toString());
    }

    @Test
    public void testWin4() throws Exception {
        allowWin();

        String path = "C:"+ PathUtils.SEPARATOR+"temp"+PathUtils.SEPARATOR;
        Path res = Paths.get(path);

        assertEquals("C:\\temp",res.toString());
    }

    @Test
    public void testWin2() throws Exception {
        allowWin();

        String path = PathUtils.SEPARATOR+"temp";
        Path res = Paths.get(path);

        assertEquals("\\temp",res.toString());
    }

    @Test
    public void testWin3() throws Exception {
        allowWin();

        String path = "temp"+PathUtils.SEPARATOR;
        Path res = Paths.get(path);

        assertEquals("temp",res.toString());
    }

}
