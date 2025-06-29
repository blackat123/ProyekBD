module com.example.proyekbasisdata {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.zaxxer.hikari;


    opens com.example.proyekbasisdata to javafx.fxml;
    exports com.example.proyekbasisdata;
    opens com.example.proyekbasisdata.centralAdmin to javafx.fxml;
    exports com.example.proyekbasisdata.centralAdmin;
    opens com.example.proyekbasisdata.dtos to javafx.base;
    exports com.example.proyekbasisdata.dtos;
    opens com.example.proyekbasisdata.Customer to javafx.fxml;
    exports com.example.proyekbasisdata.Customer;
}