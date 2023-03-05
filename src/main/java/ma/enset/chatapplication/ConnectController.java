package ma.enset.chatapplication;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import ma.enset.chatapplication.blocking.client.Client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class ConnectController implements Initializable {
    private Client client;
    @FXML
    private TextField username;
    @FXML
    private Button connect;

    public void onConnect() throws IOException {
        if(username.getText().isEmpty()){
            return;
        }
        client = new Client(new Socket("localhost", 1997));
        client.setName(username.getText());
        OutputStream os = client.getSocket().getOutputStream();
        PrintWriter pw = new PrintWriter(os, true);
        pw.println(client.getName());
        System.out.println(client);
        Stage next = (Stage) connect.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApp.class.getResource("chat_1.fxml"));
        System.out.println(fxmlLoader);
        ClientController clientController = new ClientController();
        clientController.setClient(client);
        clientController.setStage(next);
        fxmlLoader.setController(clientController);
        next.setScene(new Scene(fxmlLoader.load()));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        username.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER){
                try {
                    onConnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
