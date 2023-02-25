package ma.enset.chatapplication;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import ma.enset.chatapplication.blocking.client.Client;
import ma.enset.chatapplication.blocking.client.Conversation;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    @FXML
    private Label name;
    @FXML
    private Button send;
    @FXML
    private Button broadcast;
    @FXML
    private Button disconnect;
    @FXML
    private TextField message;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox vBox;
    @FXML
    private ListView<Client> clientList;

    private Client client;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println(client);
        if (client != null){
            name.setText(client.getName());
            clientList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            System.out.println(name.getText());
            vBox.heightProperty().addListener((observableValue, number, t1) -> {
                scrollPane.setVvalue((Double) t1);
            });
            client.receiveMessage(vBox, clientList);

//            clientList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Client>() {
//                @Override
//                public void changed(ObservableValue<? extends Client> observableValue, Client client, Client t1) {
//                    System.out.println(t1);
//                    ObservableList<Client> clientObservableList = clientList.getSelectionModel().getSelectedItems();
//
//                    send.setOnAction(actionEvent -> sendMessage(clientObservableList));
//                    message.setOnKeyPressed(keyEvent -> {
//                        if(keyEvent.getCode() == KeyCode.ENTER){
//                            sendMessage(clientObservableList);
//                        }
//                    });
//                }
//            });
            send.setOnAction(actionEvent -> sendMessage());
            message.setOnKeyPressed(keyEvent -> {
                if(keyEvent.getCode() == KeyCode.ENTER){
                    sendMessage();
                }
            });
            broadcast.setOnAction(actionEvent -> sendMessageBroadcast());
            disconnect.setOnAction(actionEvent -> {
                try {
                    onDisconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        }
    }

    public void sendMessage(){
        String messageToSend = message.getText();
        ObservableList<Client> to = clientList.getSelectionModel().getSelectedItems();
        System.out.println(to);
        if (!messageToSend.isEmpty()) {
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_RIGHT);

            hBox.setPadding(new Insets(5, 5, 5, 10));
            Text text = new Text(messageToSend);
            TextFlow textFlow = new TextFlow(text);
            textFlow.setStyle(
                    "-fx-color: rgb(239, 242, 255);" +
                            "-fx-background-color: rgb(15, 125, 242);" +
                            "-fx-background-radius: 20px;");

            textFlow.setPadding(new Insets(5, 10, 5, 10));
            text.setFill(Color.color(0.934, 0.925, 0.996));

            hBox.getChildren().add(textFlow);
            vBox.getChildren().add(hBox);
            String preMessage = "";
            for (Client c : to) {
                preMessage = preMessage + c.getId() + ",";
            }

            System.out.println(preMessage.substring(0, preMessage.length() - 1));
            client.sendMessage(preMessage.substring(0, preMessage.length() - 1) + "=>" + messageToSend);
            message.clear();
        }
    }

    public void sendMessageBroadcast(){
        String messageToSend = message.getText();
        if (!messageToSend.isEmpty()) {
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_RIGHT);

            hBox.setPadding(new Insets(5, 5, 5, 10));
            Text text = new Text(messageToSend);
            TextFlow textFlow = new TextFlow(text);
            textFlow.setStyle(
                    "-fx-color: rgb(239, 242, 255);" +
                            "-fx-background-color: #128c7e;" +
                            "-fx-background-radius: 20px;");

            textFlow.setPadding(new Insets(5, 10, 5, 10));
            text.setFill(Color.color(0.934, 0.925, 0.996));

            hBox.getChildren().add(textFlow);
            vBox.getChildren().add(hBox);

            client.sendMessage(messageToSend);
            message.clear();
        }
    }

    public static void addLabel(String messageFromServer, VBox vBox){
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(messageFromServer);
        TextFlow textFlow = new TextFlow(text);

        textFlow.setStyle(
                "-fx-background-color: rgb(233, 233, 235);" +
                        "-fx-background-radius: 20px;");

        textFlow.setPadding(new Insets(5, 10, 5, 10));
        hBox.getChildren().add(textFlow);

        Platform.runLater(() -> vBox.getChildren().add(hBox));
    }

    public static void addClient(ListView<Client> myClients, List<Client> clientIds){
        myClients.getItems().clear();
        Platform.runLater(()->{
            for (Client client: clientIds) {
                myClients.getItems().add(client);
            }
        });
    }

    public void onDisconnect() throws IOException {
        client.sendMessage("disconnect");
        client.close();
        Stage next = (Stage) disconnect.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApp.class.getResource("chat_0.fxml"));
        System.out.println(fxmlLoader);
        next.setScene(new Scene(fxmlLoader.load()));

    }

}
