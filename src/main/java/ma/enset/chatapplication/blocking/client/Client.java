package ma.enset.chatapplication.blocking.client;

import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import ma.enset.chatapplication.ClientController;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class Client {
    private int id;
    private String name;
    private Socket socket;
    private Map<Integer, String> myClients;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public Client(Socket socket) {
        try {
            this.socket = socket;
            myClients = new HashMap<>();
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Client(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Socket getSocket() {
        return socket;
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

    public void receiveMessage(VBox vBox, ListView<Client> clientList){
        new Thread(() -> {
            while(socket.isConnected()){
                try{
                    String message = bufferedReader.readLine();
                    if(message.contains("clients,")){
                        String[] split = message.split(",");
                        System.out.println(message +"*****"+split[1]);
                        byte[] objectBytes = Base64.getDecoder().decode(split[1]);
                        ByteArrayInputStream bais = new ByteArrayInputStream(objectBytes);
                        ObjectInputStream ois = new ObjectInputStream(bais);
                        myClients = (Map<Integer, String>) ois.readObject();
                        List<Client> cs = new ArrayList<>();
                        for (Map.Entry<Integer, String> e :myClients.entrySet()) {
                            cs.add(new Client(e.getKey(), e.getValue()));
                        }
                        ClientController.addClient(clientList, cs);
                    }else{
                        ClientController.addLabel(message, vBox);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    close();
                    break;
                }
            }
        }).start();
    }

    public void sendMessage(String message){
        try {
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close(){
        try {
            if(bufferedReader != null)
                bufferedReader.close();
            if (bufferedWriter != null)
                bufferedWriter.close();
            if (socket != null)
                socket.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
