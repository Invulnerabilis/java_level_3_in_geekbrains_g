package supplement_to_homework;

/*
Java. Уровень 3. Rest period. Supplement to homework. Server Chat project.

Дополнение к домашней работе. "Мелкие доработки проекта сетевого чата".

Это только "Server Client" проекта сетевого чата с дополнением к домашней работе.
Сам же "Client Chat" проекта сетевого чата с дополнением к домашней работе
ранее задаваемой на период перерыва в обучение со стороны GeekBrains находится в отдельном репозитории:
java_level_3_in_geekbrains_restperiod_supplementtohomework_client_chat
*/

import supplement_to_homework.server.MyServer;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ServerApp {

    private static final int DEFAULT_PORT = 8086;
    private static final String CONFIGURE_FILE = "src/main/resources/configs/application.properties";

    public static void main(String[] args) {

        Properties properties = new Properties();
        MyServer server = null;
        try {
            properties.load(new FileReader(CONFIGURE_FILE));
            int port;
            try {
                port = Integer.parseInt(properties.getProperty("server.port"));
            } catch (NumberFormatException e) {
                System.out.println("Не указан порт. Используется дефолтный ---->" + DEFAULT_PORT);
                port = DEFAULT_PORT;
            }

            String sqlUrl = properties.getProperty("sql.url");
            String pathToHistoryFile = properties.getProperty("data.dir");
            server = new MyServer(port, sqlUrl, pathToHistoryFile);
        } catch (IOException e) {
            if (server != null)
                server.stop();
            e.printStackTrace();
        }

        server.start();
    }
}
