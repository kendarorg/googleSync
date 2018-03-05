package org.kendar;

import java.io.*;

public class CustomOutStream extends PrintWriter {

    public CustomOutStream(Writer out) {
        super(out);
    }

    public CustomOutStream(Writer out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public CustomOutStream(OutputStream out) {
        super(out);
    }

    public CustomOutStream(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public CustomOutStream(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    public CustomOutStream(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
    }

    public CustomOutStream(File file) throws FileNotFoundException {
        super(file);
    }

    public CustomOutStream(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
    }

    @Override
    public void write(char[] buf, int off, int len) {

    }

    @Override
    public void write(int c) {}

    @Override
    public void write(String s, int off, int len) {}

    @Override
    public void write(String s){}

    @Override
    public void println(){}

}
