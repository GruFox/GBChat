package Server;

import com.example.gbchat.ClientController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class ClientHandler {
    private final Socket socket;
    private final ChatServer server;
    private String nick;
    private String login;       // НОВОЕ
    private final DataInputStream in;
    private final DataOutputStream out;
    private AuthService authService;
    private boolean isAuthenticated;

    public ClientHandler(Socket socket, ChatServer server, AuthService authService) {

        // считает 120 секунд
        // если клиент не авторизовался
        new Thread (() -> {
            try {
                sleep(120000);
                if (!isAuthenticated) {
                    closeConnection();
                    // код для остановки потоков и соединения
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        try {
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.authService = authService;

            new Thread(() -> {
                try {
                    authenticate();
                    readMessage();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка создания подключения к клиенту", e);
        }
    }

    private void readMessage() {
        try {
            while (true) {
                String msg = in.readUTF();
                if ("/end".equals(msg)) {
                    break;
                }

//
                //Простой способ, на ограниченное число клиентов, в данном случае 5

//                List<ClientHandler> clients = server.getClients();
//                String message = msg.substring(msg.indexOf(" ", 4) + 1);
//                for (int i = 0; i < 5; i++) {
//                    if (msg.startsWith("/w nick" + i+1)){
//                        clients.get(i).sendMessage(message);
//                    } else {
//                    System.out.println("Получено сообщение: " + msg);
//                    server.broadcast(msg);
//                    }
//                }


                //Более сложный способ, на неограниченное чило клиентов (конечно, можно еще причесать код)))

                if (msg.length() > 7) {

                    if (msg.startsWith("/changeNick ")) {
                        String newNick = msg.substring(12);
                        authService.changeNick(login, newNick);
                    } else {
                        Scanner s = new Scanner(msg.substring(msg.indexOf("k") + 1));
                        List<ClientHandler> clients = server.getClients();
                        if (msg.startsWith("/w nick") && s.hasNextInt() && s.nextInt() <= clients.size()) { // startsWith проверяет только начало строки
                            s.close();
                            String nick = msg.substring(3, msg.indexOf(" ", 4)); //вырезаем строку начиная с 3 индекса до пробела (3 вкл, пробел невкл)
                            String message = msg.substring(msg.indexOf(" ", 4) + 1);
                            int i = 0;
                            while (!nick.equals(clients.get(i).getNick()) && i < clients.size() - 1) {
                                i++;
                            }
                            if (nick.equals(clients.get(i).getNick())) {
                                clients.get(i).sendMessage(message);

                            } else {
                                System.out.println("Получено сообщение: " + msg); //проверка на случай если мы дошли до последней ячейки
                                server.broadcast(msg);
                            }
                        } else {
                            System.out.println("Получено сообщение: " + msg);
                            server.broadcast(msg);
                            s.close();
                        }
                    }
                } else {
                    System.out.println("Получено сообщение: " + msg);
                    server.broadcast(msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void authenticate() {   // ждем пока пользователь правльно не авторизуется
        while (true) {
            try {
                String msg = in.readUTF();
                if (msg.startsWith("/auth")) {
                    String[] s = msg.split(" "); // распилить там где пробел
                    String login = s[1];
                    String password = s[2];
                    this.login = login;     // НОВОЕ
                    String nick = authService.getNickByLoginAndPassword(login, password);
                    if (nick != null) {
                        if (server.isNickBusy(nick)) {
                            sendMessage("Пользователь уже авторизован");
                            continue;
                        }
                        sendMessage("/authok " + nick); // авторизация клиента отсюда
                        this.nick = nick;
                        server.broadcast("Пользователь " + nick + " вошел в чат");
                        isAuthenticated = true;
                        server.subscribe(this); // подписать (то есть ClientHandler клиента добавить в список активных ClientHandlerов)
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void closeConnection() {
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
                server.unsubscribe(this);
                socket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка отключения", e);
        }
    }

    public void sendMessage(String message) {
        try {
            System.out.println("Отправляю сообщение: " + message);
            if (nick != null && message.startsWith("/")) {
                message = this.nick + ": " + message;
            }
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String getNick() {
        return nick;
    }
}