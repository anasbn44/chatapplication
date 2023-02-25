package ma.enset.chatapplication.blocking.client;

import java.io.Serializable;
import java.net.Socket;

public class Conversation implements Serializable {
    private int clientId;
    private String clientName;
    private Socket socket;

    public Conversation(int clientId, Socket socket, String name) {
        this.clientId = clientId;
        this.socket = socket;
        this.clientName = name;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }


    public Socket getSocket() {
        return socket;
    }

    @Override
    public String toString() {
        return clientName;
    }
}
