package homework3.server.handlers;

import homework3.server.MyServer;
import homework3.server.services.AuthenticationService;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    private MyServer myServer;
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private Thread readMessages;
    private String username;

    public ClientHandler(MyServer myServer, Socket socket) {
        this.myServer = myServer;
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
                    myServer.unSubscribe(this);
                    myServer.broadcastServerMessage(this, "Пользователь " + username + " отключился от чата");
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

        AuthenticationService auth = myServer.getAuthenticationService();

        username = auth.getUsernameByLoginAndPassword(login, password);

        if (username != null) {
            if (myServer.isUsernameBusy(username)) {
                out.writeUTF(AUTHERR_CMD_PREFIX + " Логин уже используется");
                return false;
            }

            out.writeUTF(AUTHOK_CMD_PREFIX + " " + username);
            myServer.subscribe(this);
            System.out.println("Пользователь " + username + " подключился к чату");

            List<String> listMessages = new ArrayList<>();
            List<String> dates = Arrays.stream(takeAllFilesByPath(Path.of(myServer.pathLocalData)))
                    .sorted((s1, s2) -> {
                        for (int i = 0; i < s1.length(); i++) {
                            if (s1.charAt(i) == s2.charAt(i)) {
                                continue;
                            }
                            if (s1.charAt(i) > s2.charAt(i)) {
                                return -1;
                            } else return 1;
                        }
                        return 0;
                    }).collect(Collectors.toList());

            int messages = 0;
            for (String data : dates) {
                List<String> allMessages = Files.readAllLines(Path.of(myServer.pathLocalData + '/' + data));
                for (long i = allMessages.size() - 1; i >= 0; i--) {
                    listMessages.add(allMessages.get((int) i));
                    messages++;
                    if (messages == 100) {
                        break;
                    }
                }
                if (messages == 100) {
                    break;
                }
            }

            StringBuilder result = new StringBuilder();
            for (int i = listMessages.size() - 1; i >= 0; i--) {
                result.append(listMessages.get(i)).append('\n');
            }

            out.writeUTF(DATA + ' ' + result);

            myServer.broadcastServerMessage(this, "Пользователь " + username + " подключился к чату");

            return true;
        } else {
            out.writeUTF(AUTHERR_CMD_PREFIX + " Неверная комбинация логина и пароля");
            return false;
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
            System.out.println("Неверная команда регистрации");
            return false;
        }

        String loginReg = parts[1];
        String passwordReg = parts[2];
        String usernameReg = parts[3];

        AuthenticationService reg = myServer.getAuthenticationService();

        username = reg.registration(loginReg, passwordReg, usernameReg);

        if (username != null) {
            if (myServer.isUsernameBusy(username)) {
                out.writeUTF(REGERR_CMD_PREFIX + " Логин уже используется");
                return false;
            }

            out.writeUTF(REGOK_CMD_PREFIX + " " + username);
            myServer.subscribe(this);
            System.out.println("Пользователь " + username + " подключился к чату");

            myServer.broadcastServerMessage(this, "Пользователь " + username + " подключился к чату");

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
                case STOP_SERVER_CMD_PREFIX -> myServer.stop();
                case END_CLIENT_CMD_PREFIX -> closeConnection();
                case CHANGE_USERNAME -> {
                    if (myServer.getAuthenticationService().changeUsername(username, parts[1])) {
                        username = parts[1];
                        myServer.sendAllUsersServerMessage(myServer.getAllUsers());
                    }
                }
                case CHANGE_PASSWORD -> {
                    if (myServer.getAuthenticationService().changePassword(username, parts[1], parts[2])) {
                        sendServerMessage("пароль изменен");
                    } else {
                        sendServerMessage("пароль не изменен");
                    }
                }
                case CLIENT_MSG_CMD_PREFIX -> {
                    String[] messageParts = message.split("\\s+", 2);
                    myServer.broadcastMessage(this, messageParts[1]);
                }
                case PRIVATE_MSG_CMD_PREFIX -> {
                    String[] privateMessageParts = message.split("\\s+", 3);
                    String recipient = privateMessageParts[1];
                    String privateMessage = privateMessageParts[2];

                    myServer.sendPrivateMessage(this, recipient, privateMessage);
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

    public void sendMessage(String sender, String message, Boolean isPrivate) throws IOException {
        out.writeUTF(String.format("%s %s %s", isPrivate ?
                        PRIVATE_MSG_CMD_PREFIX
                        : CLIENT_MSG_CMD_PREFIX,
                sender, message));
    }

    public void sendMessage(String sender, String message) throws IOException {
        sendMessage(sender, message, false);
    }

    public String getUsername() {
        return username;
    }
}
