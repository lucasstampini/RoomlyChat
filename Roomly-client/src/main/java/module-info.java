module com.example.roomlyclient {
    requires transitive javafx.controls; // Mudando para transitive por problemas no VS Code
    requires transitive javafx.fxml;


    opens com.example.roomlyclient to javafx.fxml;
    exports com.example.roomlyclient;
}