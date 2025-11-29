package aplicacao;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;


public class configuracoes {

    @FXML private TextField ciclosField;
    @FXML private TextField focoField;
    @FXML private TextField pausaCurtaField;
    @FXML private TextField pausaLongaField;
    @FXML private TextField cicloPausaLonga;

    @FXML private CheckBox autoCicloCheck;
    @FXML private CheckBox sempreVisivelCheck;

    @FXML
    private void initialize() {
        ciclosField.setText(String.valueOf(App.configGlobal.getCiclos()));
        focoField.setText(String.valueOf(App.configGlobal.getFoco()));
        pausaCurtaField.setText(String.valueOf(App.configGlobal.getPausaCurta()));
        pausaLongaField.setText(String.valueOf(App.configGlobal.getPausaLonga()));
        cicloPausaLonga.setText(String.valueOf(App.configGlobal.getCicloPausaLonga()));

        autoCicloCheck.setSelected(App.configGlobal.isAutoCiclo());
        sempreVisivelCheck.setSelected(App.configGlobal.isSempreVisivel());
    }

    @FXML
    private void salvar() {
        try {
            int ciclos = Integer.parseInt(ciclosField.getText());
            int foco = Integer.parseInt(focoField.getText());
            int pausaCurta = Integer.parseInt(pausaCurtaField.getText());
            int pausaLonga = Integer.parseInt(pausaLongaField.getText());
            int cicloPausaLongaValor = Integer.parseInt(cicloPausaLonga.getText());

            // Condições de validação dos valores

            // Valores mínimos absolutos
            if (foco < 1 || pausaCurta < 1 || pausaLonga < 1) {
                mostrarErro("Todos os tempos devem ter pelo menos 1 minuto.");
                return;
            }

            // Valor máximo absoluto do ciclo
            if (ciclos > 10 ) {
                mostrarErro("Valor máximo ultrapassado.\nFavor escolher um valor menor para os ciclos.");
                return;
            }
        
            // Valores máximos absolutos
            if (foco > 60 || pausaCurta > 60 || pausaLonga > 60) {
                mostrarErro("Todos os tempos devem ser menores ou iguais a 60 minutos.");
                return;
            }

            // Valor mínimo para ciclos (necessário para existir pausa longa)
            if (ciclos < 4) {
                mostrarErro("O número de ciclos deve ser pelo menos 4.");
                return;
            }

            if (cicloPausaLongaValor < 1) {
                mostrarErro("O valor de 'ciclos até a pausa longa' deve ser pelo menos 1.");
                return;
            }

            boolean auto = autoCicloCheck.isSelected();
            boolean sempre = sempreVisivelCheck.isSelected();

            App.configGlobal.setFoco(foco);
            App.configGlobal.setPausaCurta(pausaCurta);
            App.configGlobal.setPausaLonga(pausaLonga);
            App.configGlobal.setCiclos(ciclos);
            App.configGlobal.setAutoCiclo(auto);
            App.configGlobal.setSempreVisivel(sempre);

            System.out.println("Configurações atualizadas!");

            if (App.principalController != null) {
                App.principalController.recarregarConfiguracoes();
            }

            voltar();
        }
        catch (NumberFormatException e) {
            mostrarErro("Digite apenas números válidos em todos os campos.");
        }
    }

    @FXML
    private void voltar() {
        javafx.stage.Stage stage = (javafx.stage.Stage) focoField.getScene().getWindow();
        stage.close();
    }

    private void mostrarErro(String mensagem) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Valor inválido");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}