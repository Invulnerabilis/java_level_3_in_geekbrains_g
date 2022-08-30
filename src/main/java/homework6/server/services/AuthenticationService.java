package homework6.server.services;

import homework6.server.models.User;

import java.util.List;

public interface AuthenticationService {

    boolean changeUsername(String lastUsername, String newUsername);

    boolean changePassword(String username, String lastPassword, String newPassword);

    String getUsernameByLoginAndPassword(String login, String password);

    String registration(String login, String password, String username);

    List<User> getAll();
}
