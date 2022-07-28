/*
package lesson8.server.services.impl;

import lesson8.server.models.User;
import lesson8.server.services.AuthenticationService;

import java.util.List;

public class SimpleAuthenticationServiceImpl implements AuthenticationService {

    private static final List<User> clients = List.of(
            new User("martin", "1", "Martin_Superstar"),
            new User("batman", "1", "Брюс_Уэйн"),
            new User("gena", "1", "Гендальф_Серый"),
            new User("mario", "1", "Super_Mario"),
            new User("bender", "1", "Bender"),
            new User("ezhik", "1", "Super_Sonic")
    );

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        for (User client : clients) {
            if (client.getLogin().equals(login) && client.getPassword().equals(password)) {
                return client.getUsername();
            }
        }
        return null;
    }

    @Override
    public List<User> getAll() {
        return clients;
    }


}
*/
