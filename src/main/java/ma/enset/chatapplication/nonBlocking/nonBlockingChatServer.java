package ma.enset.chatapplication.nonBlocking;

import ma.enset.chatapplication.blocking.server.BlockingChatServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.stream.Collectors;

public class nonBlockingChatServer {
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress("0.0.0.0", 1997));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            int channel = selector.select();
            if (channel == 0) continue;
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isAcceptable()){
                    handleAccept(selectionKey, selector);
                } else if (selectionKey.isReadable()){
                    handleReadWrite(selectionKey, selector);
                }
            }

        }
    }

    private static void handleReadWrite(SelectionKey selectionKey, Selector selector) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int readSize = socketChannel.read(byteBuffer);
        if (readSize == -1){
            System.out.println("client disconnected " + socketChannel.getRemoteAddress());
        }
        String request = new String(byteBuffer.array());
        //response
        String response = "";
        ByteBuffer byteBufferResponse = ByteBuffer.allocate(1024);
        byteBufferResponse.put(response.getBytes());
        byteBufferResponse.flip();
        socketChannel.write(byteBuffer);

    }

    private static void handleAccept(SelectionKey selectionKey, Selector selector) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel  socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_READ);

    }

    private void broadcast(String message, SocketChannel from, List<Integer> to){


    }

    private void sendClientList(){

    }

    private void onDisconnect () {

    }
}
