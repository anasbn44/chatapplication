module ma.enset.chatapplication {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens ma.enset.chatapplication to javafx.fxml;
    exports ma.enset.chatapplication;
}