module com.example.proyekbasisdata {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.zaxxer.hikari;


    opens com.example.proyekbasisdata to javafx.fxml;
    opens com.example.proyekbasisdata.centralAdmin to javafx.fxml;
    exports com.example.proyekbasisdata;
    exports com.example.proyekbasisdata.centralAdmin;
}