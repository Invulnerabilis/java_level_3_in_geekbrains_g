package supplement_to_homework.server.models;

import supplement_to_homework.server.enumeration.Commands;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
public class Message implements Serializable {
    private LocalDateTime localDateTime;
    private String publisherUsername;

    private String consumerUsername;
    private Commands commands;
    private String message;

    public Message(String publisherUsername, String clientText) {
        localDateTime = LocalDateTime.now();
        localDateTime.format(DateTimeFormatter.ofPattern("MMM dd, HH::mm"));
        this.publisherUsername = publisherUsername;
        String[] parts = clientText.split("\\s+");
        commands = Commands.valueOf(parts[0]);
        if (parts.length > 1) {
            message = parts[1];
        }
    }

    @Override
    public String toString() {
        return "[" + localDateTime.format(DateTimeFormatter.ofPattern("MMM dd, HH::mm")) + "] "
                + publisherUsername + ": " + message;
    }
}
