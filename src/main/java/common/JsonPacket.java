package common;

/**
 * Represents the standard data transfer object (DTO) for network communication.
 * All messages exchanged between client and server are serialized into this format using JSON.
 */
public class JsonPacket {
    private String command; // The action identifier (e.g., "CONNECT", "CHAT", "MOVE")
    private String content; // The payload or raw data associated with the command.
    private String sender;  // The identifier of the client who sent the packet.

    public JsonPacket() {
        // Default constructor required for JSON deserialization.
    }

    public JsonPacket(String command, String content, String sender) {
        this.command = command;
        this.content = content;
        this.sender = sender;
    }

    // Getters and Setters.
    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
}
