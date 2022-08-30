package homework6;

/*
Java. Уровень 3. Урок 6.
Домашнее задание 6. "Обзор средств разработки. Логирование".

Добавить на серверную сторону чата логирование, с выводом информации о действиях на сервере (запущен, произошла ошибка, клиент подключился, клиент прислал сообщение/команду).
Текст самих сообщений чата не обязательно сохранять в логе (по желанию).
Избавиться от всех System.out.println и заменить их где сочтёте нужным на вывод в консоль ваших логов где сами сочтёте необходимым.
Возможно даже в отдельный файл лога.
Пусть отдельно логируются все критические ошибки, критически важные исключения (типа warning) или события: смена имени, регистрация, аутентификация и т.д.).
Можно добавить логирование чата (подключение/отключение пользователей) отдельным файлом.
Все исключения должны логироваться только без System.out.println и без принтстрим прайсов.

P.S.: Это только "Server Client" проекта сетевого чата домашнего задания третьего урока.
Сам же "Client Chat" проекта сетевого чата третьего урока находится в отдельном репозитории: java_level_3_in_geekbrains_homework6_client_chat
*/

import homework6.server.MyServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
