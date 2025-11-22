package aplicacao;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class configuracoes {

    @FXML private TextField focoField;
    @FXML private TextField pausaCurtaField;
    @FXML private TextField pausaLongaField;

    @FXML private CheckBox autoCicloCheck;
    @FXML private CheckBox sempreVisivelCheck;

    @FXML
    private void salvar() throws IOException {
        String foco = focoField.getText();
        String pausaCurta = pausaCurtaField.getText();
        String pausaLonga = pausaLongaField.getText();

        boolean auto = autoCicloCheck.isSelected();
        boolean sempre = sempreVisivelCheck.isSelected();

        System.out.println("Configurações salvas:");
        System.out.println("Foco: " + foco + " min");
        System.out.println("Pausa curta: " + pausaCurta + " min");
        System.out.println("Pausa longa: " + pausaLonga + " min");
        System.out.println("Auto ciclo: " + auto);
        System.out.println("Sempre visível: " + sempre);

        App.setRoot("main"); // volta para tela principal
    }

    @FXML
    private void voltar() throws IOException {
        App.setRoot("main");
    }
}