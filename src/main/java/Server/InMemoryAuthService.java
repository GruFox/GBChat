package Server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InMemoryAuthService implements AuthService {

    /* private List<UserData> users;

    private class UserData {
        private String login;
        private String password;
        private String nick;

        public UserData(String login, String password, String nick) {
            this.login = login;
            this.password = password;
            this.nick = nick;
        }
   } */

    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        try {
            String url = "C:\\Users\\Пользователь\\Documents\\Программирование\\Обучение\\gbchat\\src\\main\\resources\\com\\example\\gbchat\\authenticate.db";
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection(url);
            PreparedStatement statement = connection.prepareStatement("SELECT nick FROM users WHERE (login = ? AND password = ?)");
            statement.setString(1, login);
            statement.setString(2, password);
            ResultSet result = statement.executeQuery();                           // execute означает выполнить
            // в result содержится таблица:
            // NICK <- название столбца
            // nick2
            result.next();  // перемещаемся во второй ряд (строку)
            String nick = result.getString(1);  // возвращаем содержимое второго ряда -- "nick2"; нужен столбец 1
            return nick;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;

//        for (UserData user : users) {
//            if (user.login.equals(login) && user.password.equals(password)) {
//                return user.nick;
//            }
//        }
//        return null;
    }

    @Override
    public void start() {
        try {
            String url = "C:\\Users\\Пользователь\\Documents\\Программирование\\Обучение\\gbchat\\src\\main\\resources\\com\\example\\gbchat\\users.db";
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection(url);
            PreparedStatement statement = connection.prepareStatement("INSERT INTO users (login, password, nick) VALUES (?, ?, ?)");
            for (int i = 1; i < 6; i++) {
                statement.setString(1, "'login" + i + "'");     // 'login2'
                statement.setString(2, "'pass" + i + "'");
                statement.setString(3, "'nick" + i + "'");
                statement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

//        users = new ArrayList<>();
//        for (int i = 0; i < 5; i++) {
//            users.add(new UserData("login" + i, "pass" + i, "nick" + i));
//
//        }

    }


    public void changeNick(String login, String newNick) {
        try {
            String url = "C:\\Users\\Пользователь\\Documents\\Программирование\\Обучение\\gbchat\\src\\main\\resources\\com\\example\\gbchat\\users.db";
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection(url);
            PreparedStatement statement = connection.prepareStatement("INSERT INTO users(nick) VALUES (?) WHERE login = ?");
            statement.setString(1, "'" + newNick + "'");
            statement.setString(2, "'" + login + "'");
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        System.out.println("Сервис аутентификации остановлен");
    }
}
