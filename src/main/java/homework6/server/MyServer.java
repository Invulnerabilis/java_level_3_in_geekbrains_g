package homework6.server;

import homework6.server.handlers.ClientHandler;
import homework6.server.models.Message;
import homework6.server.models.User;
import homework6.server.repository.SQLRepository;
import homework6.server.services.AuthenticationService;
import homework6.server.services.impl.SqlAuthenticationServiceImpl;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MyServer {

    static final Logger loggerInfo = LogManager.getLogger("infoLogger");
    static final Logger loggerWarnings = LogManager.getLogger("warningsLogger");
    static final Logger loggerErrors = LogManager.getLogger("errorsLogger");


    private final ServerSocket serverSocket;
    private final AuthenticationService authenticationService;
    private final SQLRepository SQLRepository;
    private final ArrayList<ClientHandler> clients;
    public final String pathToHistoryFile;
    private final ObjectOutputStream outputStreamToFile;
    private final File localHistory;

    public MyServer(int port, String url, String pathToHistoryFile) throws IOException {
        this.pathToHistoryFile = pathToHistoryFile;
        localHistory = new File(this.pathToHistoryFile, System.currentTimeMillis() + "");
        if (!localHistory.createNewFile()) {
            String message = "File not created";
            loggerErrors.error(message);
            throw new IOException(message);
        }

        outputStreamToFile = new ObjectOutputStream(new FileOutputStream(localHistory));

        serverSocket = new ServerSocket(port);
        clients = new ArrayList<>();

        try {
            SQLRepository = new SQLRepository(url);
        } catch (SQLException e) {
            loggerErrors.error(e);
            throw new IOException(e);
        }

        authenticationService = new SqlAuthenticationServiceImpl(SQLRepository);
    }

    public void start() {
        loggerWarnings.warn("СЕРВЕР ЗАПУЩЕН!");

        try {
            while (true) {
                waitAndProcessNewClientConnection();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void waitAndProcessNewClientConnection() throws IOException {
        loggerInfo.info("Ожидание клиента...");
        Socket socket = serverSocket.accept();
        loggerInfo.info("Клиент подключился!");

        processClientConnection(socket);
    }

    private void processClientConnection(Socket socket) throws IOException {
        ClientHandler clientHandler = new ClientHandler(this, socket);
        clientHandler.handle();
    }

    public synchronized void subscribe(ClientHandler handler) throws IOException {
        clients.add(handler);
        sendAllUsersServerMessage(getAllUsers());
    }

    public synchronized void unSubscribe(ClientHandler handler) throws IOException {
        clients.remove(handler);
        sendAllUsersServerMessage(getAllUsers());
    }

    public String getAllUsers() {
        StringBuilder sb = new StringBuilder(" -conditionUsers ");
        List<String> usernameList = authenticationService.getAll().stream().map(User::getUsername).toList();
        for (String username : usernameList) {
            boolean flag = false;
            for (ClientHandler clientHandler :
                    clients) {
                if (username.equals(clientHandler.getUsername())) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                sb.append("ONLINE------->");
            } else {
                sb.append("OFFLINE------->");
            }
            sb.append(username).append(" ");
        }
        return sb.toString();
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public boolean isUsernameBusy(String username) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public void stop() {
        loggerWarnings.warn("ЗАВЕРШЕНИЕ РАБОТЫ");

        SQLRepository.disconnect();

        System.exit(0);
    }

    public synchronized void broadcastMessage(Message message) throws IOException {
        for (ClientHandler client : clients) {
            outputStreamToFile.writeObject(message);
            client.sendMessage(message);
        }
    }


    public synchronized void sendPrivateMessage(Message privateMessage) throws IOException {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(privateMessage.getConsumerUsername())) {
                client.sendMessage(privateMessage);
            }
        }
    }

    public synchronized void sendAllUsersServerMessage(String message) throws IOException {
        for (ClientHandler client : clients) {
            client.sendServerMessage(message);
        }
    }

    public synchronized void broadcastServerMessage(ClientHandler sender, String message) throws IOException {
        for (ClientHandler client : clients) {
            if (client == sender) {
                continue;
            }
            client.sendServerMessage(message);
        }
    }
}
