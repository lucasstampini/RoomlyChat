module com.example.roomly {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.roomly to javafx.fxml;
    exports com.example.roomly;
}