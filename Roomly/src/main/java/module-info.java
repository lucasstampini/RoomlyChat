module com.example.roomly {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.apache.commons.io;


    opens com.example.roomly to javafx.fxml;
    exports com.example.roomly;
}