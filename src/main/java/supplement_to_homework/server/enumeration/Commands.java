package supplement_to_homework.server.enumeration;

public enum Commands {

    AUTH_CMD_PREFIX("/auth"), // + login + password
    AUTHOK_CMD_PREFIX("/authok"), // + username
    AUTHERR_CMD_PREFIX("/autherr"), // + error message

    REG_CMD_PREFIX("/reg"),
    REGOK_CMD_PREFIX("/regok"),
    REGERR_CMD_PREFIX("/regerr"),

    DATA("/data"),

    CHANGE_USERNAME("/change_username"),

    CHANGE_PASSWORD("/change_password"),

    CLIENT_MSG_CMD_PREFIX("/cMsg"), // + msg
    SERVER_MSG_CMD_PREFIX("/sMsg"), // + msg
    PRIVATE_MSG_CMD_PREFIX("/pm"), // + username + msg

    CONDITION_USERS("-conditionUsers"), // + msg

    STOP_SERVER_CMD_PREFIX("/stop"),
    END_CLIENT_CMD_PREFIX("/end");

    private final String command;

    Commands(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}

