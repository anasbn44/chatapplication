package ma.enset.chatapplication.nonBlocking.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class SingleThreadNonBlokingServer {
    private Map<SocketChannel,Integer> socketChannels = new HashMap<>();
    private int clientsCount;
    public static void main(String[] args) throws Exception {
        new SingleThreadNonBlokingServer();
    }
    public SingleThreadNonBlokingServer(){
        this.startServer();
    }

    public void startServer(){
        try {
            Selector selector=Selector.open();
            ServerSocketChannel serverSocketChannel=ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress("0.0.0.0",1997));
            serverSocketChannel.configureBlocking(false);
            int validOps = serverSocketChannel.validOps();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            while (true){
                int readyChannels = selector.select();
                if (readyChannels==0) continue;
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()){
                    SelectionKey selectionKey = iterator.next();
                    if(selectionKey.isAcceptable()){
                        handleForAccept(selector,selectionKey);
                    } else if(selectionKey.isReadable()){
                        handleForRead(selector,selectionKey);
                    }else if(selectionKey.isConnectable()){
                        System.out.println("New connection");
                    }else if(selectionKey.isWritable()){
                    }
                    iterator.remove();
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleForAccept(Selector selector, SelectionKey selectionKey) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        ++clientsCount;
        socketChannels.put(socketChannel,clientsCount);
        socketChannel.configureBlocking(false);
        socketChannel.register(selector,SelectionKey.OP_READ);
        String msg = "Welcome, your id is : " + clientsCount;
        sendMessage(msg, socketChannel);

    }

    private void handleForRead (Selector selector, SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int read = socketChannel.read(byteBuffer);
        if (read == -1){
            System.out.println("Client has been disconnected");
            socketChannels.remove(socketChannel);
            socketChannel.close();

            socketChannel.keyFor(selector).channel();
        } else
        {
            String request = new String(byteBuffer.array()).trim();
            if (request.length() > 0){
                System.out.println(request);
                String message = request;
                List<Integer> destinationList = new ArrayList<>();
                String[] requestItems = request.split("=>");
                if(requestItems.length == 2){
                    String destination = requestItems[0];
                    message = requestItems[1];
                    if(destination.trim().contains(",")){
                        String[] destinations = destination.trim().split(",");
                        for (String d : destinations){
                            destinationList.add(Integer.parseInt(d));
                        }
                    } else{
                        destinationList.add(Integer.parseInt(destination));
                    }

                }
                broadCastMessage(message + "\n",socketChannel,destinationList);
            }
        }
    }
    private void onDisconnect(){

    }
    private void broadCastMessage(String message, SocketChannel from, List<Integer> destinations) throws IOException {
        for (SocketChannel socketChannel:socketChannels.keySet()){
            int clientId=socketChannels.get(socketChannel);
            boolean all = destinations.size() == 0;
            if(!socketChannel.equals(from) && (destinations.contains(clientId) || all)){
                ByteBuffer byteBufferResponse=ByteBuffer.allocate(1024);
                int fromId=socketChannels.get(from);
                String formattedMessage=String.format("%s : %s",fromId,message);
                byteBufferResponse.put(formattedMessage.getBytes());
                byteBufferResponse.flip();
                socketChannel.write(byteBufferResponse);
            }
        }
    }
    private void sendMessage(String message, SocketChannel socketChannel) throws IOException {
        ByteBuffer byteBufferResponse=ByteBuffer.allocate(1024);
        byteBufferResponse.put(message.getBytes());
        byteBufferResponse.flip();
        socketChannel.write(byteBufferResponse);
    }
}

