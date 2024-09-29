package com.example.fiscalprintertest.connectivity;

import android.os.Parcelable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class AbstractConnector {

    public abstract InputStream getInputStream() throws IOException;
    public abstract OutputStream getOutputStream() throws IOException;

    public abstract void connect()throws IOException;
    public abstract void close() throws IOException;

    public abstract String getName();
    public abstract String getAddress();
    public abstract Parcelable getDevice();
    public abstract int getRSSI();
    public abstract boolean isBonded();
}
