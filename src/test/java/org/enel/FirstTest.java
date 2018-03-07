package org.enel;

import org.enel.utils.GDException;

public class FirstTest {

    public void shouldDoStuff() throws GDException {
        String gdDir ="/";
        String lcDir ="/";

        GDDatabase db = new GDDatabase();
        GDConnection settings = new GDConnection(db,"TestApplication","~/googleSettings");
        GDIgnore ignore = new GDIgnore(settings);
        GDDir gdd = new GDDir(settings,ignore);
        GD gd = new GD(settings,gdd);
        gd.download(gdDir,lcDir,true,false);
    }
}
