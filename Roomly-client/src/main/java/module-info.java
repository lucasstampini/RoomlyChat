module com.example.roomlyclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.roomlyclient to javafx.fxml;
    exports com.example.roomlyclient;
}