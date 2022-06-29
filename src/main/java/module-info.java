module com.example.gbchat {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.gbchat to javafx.fxml;
    exports com.example.gbchat;
}