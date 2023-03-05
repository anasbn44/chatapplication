module ma.enset.chatapplication {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    exports ma.enset.chatapplication.blocking.client;
    opens ma.enset.chatapplication.blocking.client to javafx.fxml;
    exports ma.enset.chatapplication;
    opens ma.enset.chatapplication to javafx.fxml;
}