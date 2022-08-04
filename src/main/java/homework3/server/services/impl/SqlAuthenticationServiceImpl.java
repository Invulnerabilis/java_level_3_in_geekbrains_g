package homework3.server.services.impl;

import homework3.server.models.User;
import homework3.server.repository.SQLRepository;
import homework3.server.services.AuthenticationService;

import java.util.List;

public class SqlAuthenticationServiceImpl implements AuthenticationService {

    SQLRepository sqlRepository;

    public SqlAuthenticationServiceImpl(SQLRepository SQLRepository) {
        this.sqlRepository = SQLRepository;
    }

    @Override
    public boolean changeUsername(String lastUsername, String newUsername){
        return sqlRepository.changeUsername(lastUsername, newUsername);
    }

    @Override
    public boolean changePassword(String username, String lastPassword, String newPassword) {
        return sqlRepository.changePassword(username, lastPassword, newPassword);
    }

    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        return sqlRepository.getUsernameByLoginAndPassword(login, password);
    }

    @Override
    public String registration(String login, String password, String username) {
        if (sqlRepository.findByLogin(login) != null) {
            return null;
        }

        if (sqlRepository.createUser( username, login, password)) {
            return username;
        } else return null;
    }

    @Override
    public List<User> getAll() {
        return sqlRepository.getAllUsers();
    }
}
