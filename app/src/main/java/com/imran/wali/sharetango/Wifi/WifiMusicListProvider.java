package com.imran.wali.sharetango.Wifi;

import android.content.Context;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Wali on 1/22/2017.
 */

public class WifiMusicListProvider implements Runnable {

    private final ExecutorService clientProcessingPool;

    public WifiMusicListProvider() {
        clientProcessingPool = Executors.newFixedThreadPool(10);
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(9900);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientProcessingPool.submit(new ClientTask(clientSocket));
            }
        } catch (Exception e) {
            e.printStackTrace(); // This should not happen
        }

    }

    private class ClientTask implements Runnable {
        private final Socket clientSocket;

        private ClientTask(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                System.out.println("Got a client !");
                OutputStream outputStream = clientSocket.getOutputStream();
                // TODO: Send List!
                outputStream.write("hahahahaha".getBytes());
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
