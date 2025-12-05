module com.hanoi {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    // Allow JavaFX to access the Main class for launching
    opens com.hanoi to javafx.graphics, javafx.fxml;

    // Allow FXML loader to inject fields into controllers
    opens com.hanoi.controller to javafx.fxml;

    exports com.hanoi;
    exports com.hanoi.controller;
    exports com.hanoi.model;
    exports com.hanoi.db;
}