package homework3;

/*
Java. Уровень 3. Урок 3.
Домашнее задание 3. "Средства ввода-вывода".

1. Добавить в сетевой чат запись локальной истории в текстовый файл на клиенте.

2. После загрузки клиента показывать ему последние 100 строк чата.

P.S.: Это только "Server Client" сетевого чата домашнего задания третьего урока.
Сам же "Client Chat" сетевого чата третьего урока находится в отдельном репозитории: java_level_3_in_geekbrains_homework3_client_chat
*/

import homework3.server.MyServer;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ServerApp {

    private static final int DEFAULT_PORT = 8086;
    private static final String CONFIGURE_FILE = "src/main/resources/configs/application.properties";

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
            String dataPath = properties.getProperty("data.dir");
            myServer = new MyServer(port, url, dataPath);
        } catch (IOException e) {
            if (myServer != null)
                myServer.stop();
            e.printStackTrace();
        }

        myServer.start();
    }
}
