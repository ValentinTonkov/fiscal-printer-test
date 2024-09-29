package com.example.fiscalprintertest.connectivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class BluetoothSppConnector extends AbstractConnector {

    private BluetoothSocket mBtSocket;

    // The UUID for the SPP bluetooth profile.
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothDevice mBtDevice;

    public BluetoothSppConnector(BluetoothDevice btDevice) {
        mBtDevice = btDevice;
    }

    @SuppressLint("MissingPermission")
    private BluetoothSocket getBtSocket(BluetoothDevice btDevice) throws IOException {
        BluetoothSocket btSocket = null;

        if (VERSION.SDK_INT < VERSION_CODES.GINGERBREAD_MR1) {
            btSocket = btDevice.createRfcommSocketToServiceRecord(SPP_UUID);
        } else {
            try {
                // compatibility with pre SDK 10 devices
                Method method = btDevice.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
                btSocket = (BluetoothSocket) method.invoke(btDevice, SPP_UUID);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                throw new IOException(e);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                throw new IOException(e);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new IOException(e);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                throw new IOException(e);
            }
        }
        return btSocket;
    }

    @SuppressLint("MissingPermission")
    @Override
    public synchronized void connect() throws IOException {
        BluetoothSocket btSocket = getBtSocket(mBtDevice);
        mBtSocket = btSocket;
        mBtSocket.connect();
    }

    @Override
    public synchronized void close() throws IOException {
        if (mBtSocket != null) {
            mBtSocket.close();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public String getName() {
        return mBtDevice.getName();
    }

    @Override
    public String getAddress() {
        return mBtDevice.getAddress();
    }

    @Override
    public BluetoothDevice getDevice() {
        return mBtDevice;
    }

    @Override
    public int getRSSI() {
        return 100;
    }

    @Override
    public boolean isBonded() {
        return true;
    }

    @Override
    public synchronized InputStream getInputStream() throws IOException {
        if (mBtSocket != null) {
            return mBtSocket.getInputStream();
        }
        throw new IOException("Socket error");
    }

    @Override
    public synchronized OutputStream getOutputStream() throws IOException {
        if (mBtSocket != null) {
            return mBtSocket.getOutputStream();
        }
        throw new IOException("Socket error");
    }

}
