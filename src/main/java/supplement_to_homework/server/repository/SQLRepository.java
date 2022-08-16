package supplement_to_homework.server.repository;

import supplement_to_homework.server.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLRepository {

    private static Statement statement;
    private static Connection connection;

/*    private static final List<User> clients = List.of(
            new User("martin", "1", "Martin_Superstar"),
            new User("batman", "1", "Брюс_Уэйн"),
            new User("gena", "1", "Гендальф_Серый"),
            new User("mario", "1", "Super_Mario"),
            new User("bender", "1", "Bender"),
            new User("ezhik", "1", "Super_Sonic")
    );*/

    private static final String SELECT_ALL_USERS = "SELECT * FROM users";
    private static final String INSERT_USER_SQL = "INSERT INTO users" +
            "  ( username, login, password) VALUES " +
            " ( ?, ?, ?);";

    public SQLRepository(String url) throws SQLException {
        connection = DriverManager.getConnection(url);
        statement = connection.createStatement();
        createTableIfNotExists();
    }

    public String getUsernameByLoginAndPassword(String login, String password) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT username FROM users WHERE login = ? AND password = ?");
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean changeUsername(String lastUsername, String newUsername) {
        if (findByUsername(lastUsername) != null) {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET username = ? WHERE username = ?");
                preparedStatement.setString(1, newUsername);
                preparedStatement.setString(2, lastUsername);
                preparedStatement.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public boolean isRightPassword(String username, String password) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT password FROM users WHERE username = ?");
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.getString("password").equals(password)) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean changePassword(String username, String lastPassword, String newPassword) {
        if (findByUsername(username) != null) {
            if (isRightPassword(username, lastPassword)) {
                try {
                    PreparedStatement preparedStatement = connection.prepareStatement("UPDATE users SET password = ? WHERE username = ?");
                    preparedStatement.setString(1, newPassword);
                    preparedStatement.setString(2, username);
                    preparedStatement.executeUpdate();
                    return true;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    public boolean createUser(String username, String login, String password) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USER_SQL);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, login);
            preparedStatement.setString(3, password);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_USERS);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                userList.add(new User(resultSet.getString("login"), resultSet.getString("password"), resultSet.getString("username")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

    public User findByLogin(String login) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE login = ?");
            preparedStatement.setString(1, login);
            ResultSet resultSet = preparedStatement.executeQuery();
            return new User(resultSet.getString("login"), resultSet.getString("password"), resultSet.getString("username"));
        } catch (SQLException e) {
            //e.printStackTrace();
            return null;
        }
    }

    public User findByUsername(String username) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            return new User(resultSet.getString("login"), resultSet.getString("password"), resultSet.getString("username"));
        } catch (SQLException e) {
            //e.printStackTrace();
            return null;
        }
    }

    public boolean createTableIfNotExists() {
        try {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users " +
                    "(\n" +
                    " id INTEGER PRIMARY KEY NOT NULL,\n" +
                    " username TEXT NOT NULL,\n" +
                    " login TEXT NOT NULL, \n" +
                    " password TEXT NOT NULL\n" +
                    " );");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public void disconnect() {
        System.out.println("Database closed.");
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
