module com {
    requires javafx.controls;
    requires javafx.fxml;
    requires okhttp3;
    requires org.json;

    opens com to javafx.fxml;
    exports com;
}
