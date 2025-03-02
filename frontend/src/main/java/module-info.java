module com {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires okhttp3;
    requires org.json;
    requires java.prefs;
    requires java.desktop;
    requires java.logging;

    opens com to javafx.fxml;

    exports com;
}