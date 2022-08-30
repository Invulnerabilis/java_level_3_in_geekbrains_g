package homework6.server.handlers;

import homework6.server.MyServer;
import homework6.server.enumeration.Commands;
import homework6.server.models.Message;
import homework6.server.services.AuthenticationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static homework6.server.enumeration.Commands.*;

public class ClientHandler {
    static final Logger loggerInfo = LogManager.getLogger("infoLogger");
    static final Logger loggerWarnings = LogManager.getLogger("warningsLogger");
    static final Logger loggerErrors = LogManager.getLogger("errorsLogger");
    private MyServer server;
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private Thread readMessages;
    private String username;

    private static final int MAX_MESSAGES_HISTORY = 100;

    public ClientHandler(MyServer server, Socket socket) {
        this.server = server;
        clientSocket = socket;
    }

    public void handle() throws IOException {
        in = new DataInputStream(clientSocket.getInputStream());
        out = new DataOutputStream(clientSocket.getOutputStream());

        readMessages = new Thread(() -> {
            try {
                sign();
                readMessage();
            } catch (IOException e) {
                try {
                    server.unSubscribe(this);
                    loggerWarnings.warn("Пользователь " + username + " отключился от чата");
                    server.broadcastServerMessage(this, "Пользователь " + username + " отключился от чата");
                } catch (IOException ex) {
                    loggerErrors.error(ex.getMessage());
                    throw new RuntimeException(ex);
                }
                loggerErrors.error(e);
            }
        });

        readMessages.start();
    }

    private void sign() throws IOException {
        while (true) {
            String message = in.readUTF();
            if (message.startsWith(AUTH_CMD_PREFIX.getCommand())) {
                boolean isSuccessAuth = processAuthentication(message);
                if (isSuccessAuth) {
                    break;
                }
            } else if (message.startsWith(REG_CMD_PREFIX.getCommand())) {
                boolean isSuccessAuth = processRegistration(message);
                if (isSuccessAuth) {
                    break;
                }
            }
        }
    }

    private boolean processAuthentication(String message) throws IOException {
        String[] parts = message.split("\\s+");
        if (parts.length != 3) {
            loggerWarnings.warn(AUTHERR_CMD_PREFIX + "Неверная команда аутентификации");
            out.writeUTF(AUTHERR_CMD_PREFIX + " Неверная команда аутентификации");
            return false;
        }

        String login = parts[1];
        String password = parts[2];

        AuthenticationService auth = server.getAuthenticationService();
        username = auth.getUsernameByLoginAndPassword(login, password);

        if (username != null) {
            if (server.isUsernameBusy(username)) {
                loggerWarnings.warn(AUTHERR_CMD_PREFIX + " Логин уже используется");
                out.writeUTF(AUTHERR_CMD_PREFIX + " Логин уже используется");
                return false;
            }

            out.writeUTF(AUTHOK_CMD_PREFIX + " " + username);
            server.subscribe(this);
            loggerWarnings.warn("Пользователь " + username + " подключился к чату");

            out.writeUTF(DATA + " " + readChatHistoryFromFiles());

            server.broadcastServerMessage(this, "Пользователь " + username + " подключился к чату");

            return true;
        } else {
            loggerWarnings.warn(AUTHERR_CMD_PREFIX + " Неверная комбинация логина и пароля");
            out.writeUTF(AUTHERR_CMD_PREFIX + " Неверная комбинация логина и пароля");
            return false;
        }
    }

    private StringBuilder readChatHistoryFromFiles() {
        List<Message> listMessages = new ArrayList<>();

        List<String> filenames = Arrays.stream(takeAllFilesByPath(Path.of(server.pathToHistoryFile)))
                .sorted((filename1, filename2) -> {
                    for (int i = 0; i < filename1.length(); i++) {
                        if (filename1.charAt(i) == filename2.charAt(i)) {
                            continue;
                        }
                        if (filename1.charAt(i) > filename2.charAt(i)) {
                            return -1;
                        } else return 1;
                    }
                    return 0;
                }).toList();

        int countOfMessages = 0;
        outer:
        for (String filename : filenames) {
            try (ObjectInputStream objectInput = new ObjectInputStream(new FileInputStream(server.pathToHistoryFile + '/' + filename));) {

                resetFile(objectInput);


                Message message = (Message) objectInput.readObject();
                while (message != null) {
                    listMessages.add(message);
                    countOfMessages++;
                    if (countOfMessages == MAX_MESSAGES_HISTORY) {
                        break outer;
                    }
                    message = (Message) objectInput.readObject();
                }

            } catch (Exception e) {
            }
        }

        StringBuilder result = new StringBuilder();
        for (Message message : listMessages) {
            result.append(message).append('\n');
        }
        return result;
    }

    private void resetFile(ObjectInputStream objectInput) {
        try {
            objectInput.reset();
        } catch (Exception e) {
        }
    }

    private static String[] takeAllFilesByPath(Path path) {
        File file = path.toFile();
        return file.list();
    }

    private boolean processRegistration(String message) throws IOException {
        String[] parts = message.split("\\s+");
        if (parts.length != 4) {
            out.writeUTF(REGERR_CMD_PREFIX + " Неверная команда регистрации");
            loggerInfo.info("Неверная команда регистрации");
            return false;
        }

        String loginReg = parts[1];
        String passwordReg = parts[2];
        String usernameReg = parts[3];

        AuthenticationService reg = server.getAuthenticationService();

        username = reg.registration(loginReg, passwordReg, usernameReg);

        if (username != null) {
            if (server.isUsernameBusy(username)) {
                out.writeUTF(REGERR_CMD_PREFIX + " Логин уже используется");
                return false;
            }

            out.writeUTF(REGOK_CMD_PREFIX + " " + username);
            server.subscribe(this);
            loggerInfo.info("Пользователь " + username + " подключился к чату");

            server.broadcastServerMessage(this, "Пользователь " + username + " подключился к чату");

            return true;
        } else {
            out.writeUTF(REGERR_CMD_PREFIX + " Неверная комбинация логина, пароля и имени пользователя");
            return false;
        }
    }

    private void readMessage() throws IOException {
        while (true) {
            String message = in.readUTF();
            String[] parts = message.split("\\s+");
            String typeMessage = parts[0];
            loggerInfo.info("Username:  " + username + " | Command: " + typeMessage + " + ...");

            Commands command = Commands.getCommand(typeMessage);

            switch (Objects.requireNonNull(command)) {
                case STOP_SERVER_CMD_PREFIX -> server.stop();
                case END_CLIENT_CMD_PREFIX -> closeConnection();
                case CHANGE_USERNAME -> {
                    if (server.getAuthenticationService().changeUsername(username, parts[1])) {
                        loggerWarnings.warn("Изменено имя с " + username + " на " + parts[1]);
                        username = parts[1];
                        server.sendAllUsersServerMessage(server.getAllUsers());
                    }
                }
                case CHANGE_PASSWORD -> {
                    if (server.getAuthenticationService().changePassword(username, parts[1], parts[2])) {
                        loggerWarnings.warn("Изменен пароль пользователя " + username);
                        sendServerMessage("пароль изменен");
                    } else {
                        loggerWarnings.warn("Не изменен пароль пользователя " + username);
                        sendServerMessage("пароль не изменен");
                    }
                }
                case CLIENT_MSG_CMD_PREFIX -> {
                    String[] messageParts = message.split("\\s+", 2);
                    Message msg = new Message(LocalDateTime.now(), username, null, Commands.CLIENT_MSG_CMD_PREFIX, messageParts[1]);
                    server.broadcastMessage(msg);
                }
                case PRIVATE_MSG_CMD_PREFIX -> {
                    String[] privateMessageParts = message.split("\\s+", 3);
                    Message msg = new Message(LocalDateTime.now(), username, privateMessageParts[1], Commands.PRIVATE_MSG_CMD_PREFIX, privateMessageParts[2]);
                    server.sendPrivateMessage(msg);
                }
                default -> loggerWarnings.warn("Неверная команда");
            }

        }
    }

    private void closeConnection() {
        readMessages.interrupt();
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        loggerWarnings.warn(username + " отключился");
    }

    public void sendServerMessage(String message) throws IOException {
        out.writeUTF(String.format("%s %s", SERVER_MSG_CMD_PREFIX, message));
    }

    public void sendMessage(Message message) throws IOException {
        out.writeUTF(String.format("%s %s %s", message.getConsumerUsername() != null ?
                        PRIVATE_MSG_CMD_PREFIX
                        : CLIENT_MSG_CMD_PREFIX,
                message.getPublisherUsername(), message.getMessage()));
    }

    public String getUsername() {
        return username;
    }
}
