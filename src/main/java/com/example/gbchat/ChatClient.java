package com.example.gbchat;

import Server.ClientHandler;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private ClientController controller;

    public ChatClient(ClientController controller) {
        this.controller = controller;
    }

    public void openConnection() throws IOException {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try{
                waitAuth();
                readMessage();
            } finally {
                closeConnection();
            }

        }).start();
    }

    private void closeConnection() {
        controller.getLoginBox().setVisible(false);
        sendMessage("/end");
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка отключения", e);
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка отключения", e);
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка отключения", e);
        }
    }

    private void readMessage() {
        while (true) {
            try {
                String msg = in.readUTF();
                if ("/end".equals(msg)) {
                    controller.toggleBoxesVisibility(false);
                    break;
                }
                controller.addMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void waitAuth() {
        while(true) {
            try {
                String msg = in.readUTF();
                if (msg.startsWith("/authok")) {
                    String[] split = msg.split(" ");
                    String nick = split[1];
                    controller.toggleBoxesVisibility(true);
                    controller.addMessage("Успешная авторизация под ником " + nick);
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

