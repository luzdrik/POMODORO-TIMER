module aplicacao {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    opens aplicacao to javafx.fxml;
    exports aplicacao;
}
