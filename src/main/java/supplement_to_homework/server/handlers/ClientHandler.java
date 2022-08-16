package supplement_to_homework.server.handlers;

import supplement_to_homework.server.MyServer;
import supplement_to_homework.server.enumeration.Commands;
import supplement_to_homework.server.models.Message;
import supplement_to_homework.server.services.AuthenticationService;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientHandler {
    private static final String AUTH_CMD_PREFIX = "/auth"; // + login + password
    private static final String AUTHOK_CMD_PREFIX = "/authok"; // + username
    private static final String AUTHERR_CMD_PREFIX = "/autherr"; // + error message


    private static final String DATA = "/data";

    private static final String CHANGE_USERNAME = "/change_username";
    private static final String CHANGE_PASSWORD = "/change_password";

    private static final String REG_CMD_PREFIX = "/reg";
    private static final String REGOK_CMD_PREFIX = "/regok";
    private static final String REGERR_CMD_PREFIX = "/regerr";
    private static final String CLIENT_MSG_CMD_PREFIX = "/cMsg"; // + msg
    private static final String SERVER_MSG_CMD_PREFIX = "/sMsg"; // + msg
    private static final String PRIVATE_MSG_CMD_PREFIX = "/pm"; // + username + msg
    private static final String STOP_SERVER_CMD_PREFIX = "/stop";
    private static final String END_CLIENT_CMD_PREFIX = "/end";
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
                    server.broadcastServerMessage(this, "Пользователь " + username + " отключился от чата");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                e.printStackTrace();
            }
        });

        readMessages.start();
    }

    private void sign() throws IOException {
        while (true) {
            String message = in.readUTF();
            if (message.startsWith(AUTH_CMD_PREFIX)) {
                boolean isSuccessAuth = processAuthentication(message);
                if (isSuccessAuth) {
                    break;
                }
            } else if (message.startsWith(REG_CMD_PREFIX)) {
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
            out.writeUTF(AUTHERR_CMD_PREFIX + " Неверная команда аутентификации");
            System.out.println("Неверная команда аутентификации");
            return false;
        }

        String login = parts[1];
        String password = parts[2];

        AuthenticationService auth = server.getAuthenticationService();

        username = auth.getUsernameByLoginAndPassword(login, password);

        if (username != null) {
            if (server.isUsernameBusy(username)) {
                out.writeUTF(AUTHERR_CMD_PREFIX + " Логин уже используется");
                return false;
            }

            out.writeUTF(AUTHOK_CMD_PREFIX + " " + username);
            server.subscribe(this);
            System.out.println("Пользователь " + username + " подключился к чату");

            out.writeUTF(DATA + ' ' + readChatHistoryFromFiles());

            server.broadcastServerMessage(this, "Пользователь " + username + " подключился к чату");

            return true;
        } else {
            out.writeUTF(AUTHERR_CMD_PREFIX + " Неверная комбинация логина и пароля");
            return false;
        }
    }

    private StringBuilder readChatHistoryFromFiles() throws IOException {
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

                try {
                    objectInput.reset();
                } catch (Exception e) {
                }

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
                System.out.println(e.getMessage());
            }
        }

        StringBuilder result = new StringBuilder();
        for (Message message : listMessages) {
            result.append(message).append('\n');
        }
        return result;
    }

    private static String[] takeAllFilesByPath(Path path) {
        File file = path.toFile();
        return file.list();
    }

    private boolean processRegistration(String message) throws IOException {
        String[] parts = message.split("\\s+");
        if (parts.length != 4) {
            out.writeUTF(REGERR_CMD_PREFIX + " Неверная команда регистрации");
            System.out.println("Неверная команда регистрации");
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
            System.out.println("Пользователь " + username + " подключился к чату");

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
            System.out.println("message | " + username + ": " + message);

            String[] parts = message.split("\\s+");
            String typeMessage = parts[0];
            if (!typeMessage.startsWith("/")) {
                System.out.println("Неверный запрос");
            }


            switch (typeMessage) {
                case STOP_SERVER_CMD_PREFIX -> server.stop();
                case END_CLIENT_CMD_PREFIX -> closeConnection();
                case CHANGE_USERNAME -> {
                    if (server.getAuthenticationService().changeUsername(username, parts[1])) {
                        username = parts[1];
                        server.sendAllUsersServerMessage(server.getAllUsers());
                    }
                }
                case CHANGE_PASSWORD -> {
                    if (server.getAuthenticationService().changePassword(username, parts[1], parts[2])) {
                        sendServerMessage("пароль изменен");
                    } else {
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
                default -> System.out.println("Неверная команда");
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
        System.out.println(username + " отключился");
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
