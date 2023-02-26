package ma.enset.chatapplication;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import ma.enset.chatapplication.blocking.client.Client;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    @FXML
    private Label name;
    @FXML
    private Button send;
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

    private Stage stage;

    private Client client;

    public Client getClient() {
        return client;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
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

            send.setOnAction(actionEvent -> sendMessage());
            message.setOnKeyPressed(keyEvent -> {
                if(keyEvent.getCode() == KeyCode.ENTER){
                    sendMessage();
                }
            });
            disconnect.setOnAction(actionEvent -> {
                try {
                    onDisconnect();
                    back();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            stage.setOnCloseRequest(windowEvent -> onDisconnect());
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
                            "-fx-background-color: #128c7e;" +
                            "-fx-background-radius: 20px;");

            textFlow.setPadding(new Insets(5, 10, 5, 10));
            text.setFill(Color.color(0.934, 0.925, 0.996));

            hBox.getChildren().add(textFlow);
            vBox.getChildren().add(hBox);
            messageToSend = client.getName() + " : " + messageToSend;
            if(to.isEmpty()){
                client.sendMessage(messageToSend);
            } else {
                String preMessage = "";
                for (Client c : to) {
                    preMessage = preMessage + c.getId() + ",";
                }
                messageToSend = preMessage.substring(0, preMessage.length() - 1) + "=>" + messageToSend;
                client.sendMessage(messageToSend);
            }
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

    public void onDisconnect(){
        client.sendMessage("disconnect");
        client.close();
    }

    private void back() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApp.class.getResource("chat_0.fxml"));
        System.out.println(fxmlLoader);
        stage.setScene(new Scene(fxmlLoader.load()));
    }

}
