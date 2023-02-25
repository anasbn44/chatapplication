package ma.enset.chatapplication;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApp.class.getResource("chat_0.fxml"));
        System.out.println(fxmlLoader);
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("MyChat");
        stage.setScene(scene);
        stage.show();
    }
}
