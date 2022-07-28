package homework2;

/*
Домашнее задание. Java. Уровень 3. Урок 2. "Базы данных".

1. На сервер добавить в сетевой чат возможность подключения, авторизацию через базу данных SQLite.
Плюс, возможность изменить User Name (можно добавить и смену пароля).

2. На клиент добавить поле для смены пароля.

3. Добавить регистрацию новых пользователей.
И в качестве регистрации иметь возможность добавлять новых пользователей на сервер, изменять им User Name, пароль.

Примечание!
Смена username и password пока доступно только через ввод команд: /change_username и /change_password

Внимание!
Это только "Server Client" сетевого чата домашнего задания второго урока.
Сам же "Client Chat" сетевого чата второго урока находится в отдельном репозитории:
java_level_3_in_geekbrains_homework2_client_chat
*/

import homework2.server.MyServer;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ServerApp {

    private static final int DEFAULT_PORT = 8086;
    private static final String CONFIGURE_FILE = "src/main/resources/configs/application-dev.properties";

    public static void main(String[] args) {

        Properties properties = new Properties();
        MyServer myServer = null;
        try {
            properties.load(new FileReader(CONFIGURE_FILE));
            int port;
            try {
                port = Integer.parseInt(properties.getProperty("server.port"));
            } catch (NumberFormatException e) {
                System.out.println("Не указан порт. Используется дефолтный ---->" + DEFAULT_PORT);
                port = DEFAULT_PORT;
            }

            String url = properties.getProperty("sql.url");
            myServer = new MyServer(port, url);
        } catch (IOException e) {
            if (myServer != null)
                myServer.stop();
            e.printStackTrace();
        }

        myServer.start();
    }
}
