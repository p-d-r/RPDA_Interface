package com.example.rpda_interface.networking;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/*TODO make SocketConnector stateless and add static method that returns a new Socket*/
public class SocketConnector {

    private static final int portNumber = 6666;
    private static final String TARGET_IP_IPV4 = "192.168.0.2";
    public static String dyn_ip_ipv4 = null;
    //private static final String TARGET_IP_IPV4 = "192.168.100.107";
    //private static final String TARGET_IP_IPV4 = "192.168.43.205";
    //private static final String TARGET_IP_IPV6 = " 2a02:810d:8f40:971:cda3:e5ee:19ab:982b";

    private static Socket clientSocket;
    private static InputStream inputStream;
    private static InputStreamReader receiverReader;
    private static OutputStream outputStream;
    private static PrintWriter transmissionWriter;


    public static void initializeSocket() throws IOException {
        try {
            if (dyn_ip_ipv4 == null)
                clientSocket = new Socket(TARGET_IP_IPV4, portNumber);
            else
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
            e.printStackTrace();
            throw e;
        }
    }

    public static InputStreamReader getReceiver() throws IOException {
        try {
            if (clientSocket == null || clientSocket.isClosed())
                initializeSocket();

            return receiverReader;
        }catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
