package org.test;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class GeoTest {
    @Test
    public void doTest(){
        //Given
        String[] test= {
            "/C/D",
            "/C/D/E/F",
            "/A",
            "/AA/Z",
            "/A/B",
            "/D/E/F",
            "/B"
        };
        List<String> geoAreasFounded = Arrays.asList(test);


        //When
        List<String> minimizedGeoAreas = minimizeGeoAreas(geoAreasFounded);

        //Then
        assertEquals(5,minimizedGeoAreas.size());
        assertEquals("/A",minimizedGeoAreas.get(0));
        assertEquals("/AA/Z",minimizedGeoAreas.get(1));
        assertEquals("/B",minimizedGeoAreas.get(2));
        assertEquals("/C/D",minimizedGeoAreas.get(3));
        assertEquals("/D/E/F",minimizedGeoAreas.get(4));
    }

    private List<String> minimizeGeoAreas(List<String> geoAreasFounded) {
        Collections.sort(geoAreasFounded);
        List<String> minimizedGeoAreas = new ArrayList<>();
        String prev = UUID.randomUUID().toString();
        for(int i=0;i<geoAreasFounded.size();i++){
            String current = geoAreasFounded.get(i);
            if(!current.startsWith(prev+"/")){
                prev = current;
                minimizedGeoAreas.add(current);
            }
        }
        return minimizedGeoAreas;
    }
}
