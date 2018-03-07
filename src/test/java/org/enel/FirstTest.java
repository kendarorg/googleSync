package org.enel;

public class FirstTest {

    public void shouldDoStuff() throws GDException {
        String gdDir ="/";
        String lcDir ="/";

        GDTokenService tokens = new GDTokenService();
        GDConnection settings = new GDConnection("TestApplication","~/googleSettings",tokens);
        GDIgnore ignore = new GDIgnore(settings);
        GDDir gdd = new GDDir(settings,ignore);
        GD gd = new GD(settings,gdd);
        gd.download(gdDir,lcDir);
    }
}
