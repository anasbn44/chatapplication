package ma.enset.chatapplication.nonBlocking.server;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.stream.Collectors;

public class NonBlockingChatServer {
    private List<ClientServer> clientServerList = new ArrayList<>();
    private int clientsCount;
    public static void main(String[] args) {
        new NonBlockingChatServer();
    }

    private NonBlockingChatServer(){
        this.run();
    }

    private void run() {
        try{
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
                        handleRead(selectionKey, selector);
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAccept(SelectionKey selectionKey, Selector selector) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel  socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        socketChannel.read(byteBuffer);
        String name = new String(byteBuffer.array());
        clientServerList.add(new ClientServer(++clientsCount, name, socketChannel));
        sendMessage("Welcome", socketChannel);
        //sendClientList();
    }

    private void sendMessage(String message, SocketChannel socketChannel) throws IOException {
        ByteBuffer byteBufferResponse=ByteBuffer.allocate(1024);
        byteBufferResponse.put(message.getBytes());
        byteBufferResponse.flip();
        socketChannel.write(byteBufferResponse);
    }

    private void handleRead(SelectionKey selectionKey, Selector selector) throws IOException {
        System.out.println("A Channel is ready for Read");
        SocketChannel socketChannel= (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer=ByteBuffer.allocate(1024);
        int read = socketChannel.read(byteBuffer);
        if(read == -1){
            onDisconnect(clientServerList.stream().filter(e -> e.getSocketChannel() == socketChannel).findAny().orElse(null));
        } else{
            String request=new String(byteBuffer.array()).trim();
            if(request.length()>0){
                System.out.println(request);
                List<Integer> to = new ArrayList<>();
                String message;
                if(request.contains("=>")){
                    String[] split = request.split("=>");
                    message = split[1];
                    String ids = split[0];
                    if(ids.contains(",")){
                        String[] splitIds = ids.split(",");
                        for (String id : splitIds) {
                            to.add(Integer.parseInt(id));
                        }
                    } else {
                        to.add(Integer.parseInt(ids));
                    }
                } else {
                    to = clientServerList.stream().map(c -> c.getId()).toList();
                    message = request;
                }
                broadcast(message, socketChannel, to);

            }
        }

    }

    private void broadcast(String message, SocketChannel from, List<Integer> to) throws IOException {
        for (ClientServer c : clientServerList){
            SocketChannel socketChannel = c.getSocketChannel();
            int clientId=clientServerList.stream().filter(e -> e.getSocketChannel() == socketChannel).findAny().orElse(null).getId();
            boolean all=to.size()==0;
            if(!socketChannel.equals(from) && (to.contains(clientId) || all)){
                ByteBuffer byteBufferResponse=ByteBuffer.allocate(1024);
                ClientServer fromClient = clientServerList.stream().filter(e -> e.getSocketChannel() == from).findAny().orElse(null);
                String formattedMessage=String.format("%s say : %s",fromClient.getName(),message);
                System.out.println(byteBufferResponse.toString());
                byteBufferResponse.put(formattedMessage.getBytes());
                byteBufferResponse.flip();
                socketChannel.write(byteBufferResponse);
            }
        }
    }

    private void sendClientList() throws IOException {
        Map<Integer, String> map = clientServerList.stream().collect(Collectors.toMap(e -> e.getId(), e -> e.getName()));
        for (ClientServer c : clientServerList) {
            SocketChannel socketChannel = c.getSocketChannel();
            System.out.println(clientServerList);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(map);
            oos.flush();
            byte[] objectBytes = baos.toByteArray();
            String message = "clients," + Base64.getEncoder().encodeToString(objectBytes);
            ByteBuffer byteBufferResponse=ByteBuffer.allocate(1024);
            byteBufferResponse.put(message.getBytes());
            byteBufferResponse.flip();
            socketChannel.write(byteBufferResponse);
        }
    }

    private void onDisconnect (ClientServer from) throws IOException {
        clientServerList.remove(from);
        from.getSocketChannel().close();
        sendClientList();
        String disconnectMessage = from.getName() + " has disconnected";
        sendMessage(disconnectMessage, from.getSocketChannel());
    }
}
