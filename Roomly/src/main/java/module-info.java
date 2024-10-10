module com.example.roomly {
    requires transitive javafx.controls; // Mudando para transitive por problemas no VS CODE
    requires transitive javafx.fxml;


    opens com.example.roomly to javafx.fxml;
    exports com.example.roomly;
}