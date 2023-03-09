package ma.enset.chatapplication.nonBlocking.server;

import java.io.Serializable;
import java.nio.channels.SocketChannel;

public class ClientServer implements Serializable {
    private int id;
    private String name;
    private SocketChannel socketChannel;

    public ClientServer() {
    }

    public ClientServer(int id, String name, SocketChannel socketChannel) {
        this.id = id;
        this.name = name;
        this.socketChannel = socketChannel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }
}
