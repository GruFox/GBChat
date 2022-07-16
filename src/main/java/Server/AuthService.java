package Server;

import java.io.Closeable;

public interface AuthService extends Closeable {

    String getNickByLoginAndPassword(String login, String password);

    void start();

    void close();

    void changeNick(String login, String newNick);

}
