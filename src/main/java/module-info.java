module aplicacao {
    requires javafx.controls;
    requires javafx.media;
    requires javafx.fxml;
    requires java.prefs;
    requires com.google.gson;

    opens aplicacao to javafx.fxml, com.google.gson;
    exports aplicacao;
}
