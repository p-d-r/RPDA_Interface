package com.example.rpda_interface.model.socketConnector;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketConnector {

    private static final int portNumber = 6666;
    //private static final String TARGET_IP_IPV4 = "192.168.0.3";
    private static final String TARGET_IP_IPV4 = "192.168.100.107";
    //private static final String TARGET_IP_IPV6 = " 2a02:810d:8f40:971:cda3:e5ee:19ab:982b";

    private static Socket clientSocket;
    private static InputStream inputStream;
    private static InputStreamReader receiverReader;
    private static OutputStream outputStream;
    private static PrintWriter transmissionWriter;


    public static void initializeSocket() throws IOException {
        try {
            clientSocket = new Socket(TARGET_IP_IPV4, portNumber);
            inputStream = clientSocket.getInputStream();
            receiverReader = new InputStreamReader(inputStream);
            outputStream  = clientSocket.getOutputStream();
            transmissionWriter = new PrintWriter(outputStream, true);
        } catch (Exception e) {
            e.printStackTrace();
            if (clientSocket != null) {
                clientSocket.close();
                clientSocket = null;
                transmissionWriter = null;
                receiverReader = null;
            }
            throw e;
        }
    }

    public static PrintWriter getTransmitter() throws IOException {
        try {
            if (clientSocket == null || clientSocket.isClosed())
                initializeSocket();

            return transmissionWriter;
        }catch (IOException e) {
            System.err.println("Invariants are violated and shutdown is needed.");
            throw e;
        }
    }

    public static InputStreamReader getReceiver() throws IOException {
        try {
            if (clientSocket == null || clientSocket.isClosed())
                initializeSocket();

            return receiverReader;
        }catch (IOException e) {
            System.err.println("Invariants are violated and shutdown is needed.");
            throw e;
        }
    }
}
