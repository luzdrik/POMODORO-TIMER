package aplicacao;

import java.io.IOException;
import java.time.LocalDate;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.shape.Arc;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.animation.ScaleTransition;

public class principal {
    @FXML private Label timerLabel;
    @FXML private Arc progressArc;
    @FXML private Arc bgArc;
    @FXML private Arc circlefundo1;
    @FXML private Arc circlefundo2;
    @FXML private Button btnStart;
    @FXML private Button btnPause;
    @FXML private Button btnReset;
    @FXML private ImageView statusImage;
    @FXML private Label focoLabel;
    @FXML private ProgressBar dailyProgressBar;
    @FXML private Label dailyProgressLabel;

    private Timeline timeline;
    private ScaleTransition pulseAnimation;

    // Tempo
    private int tempoFoco;
    private int tempoPausaCurta;
    private int tempoPausaLonga;
    private int ciclosTotal;

    // Resumo e Barra de progresso diário
    @FXML private TextFlow resumoFlow;
    private Resumo resumo = new Resumo();
    private final int meta = 60;

    // Tarefas
    @FXML private VBox taskList;
    @FXML private TextField taskInput;

    // Controle do timer
    private int tempoTotal;
    private int tempoRestante;

    private int etapa = 0;
    private int cicloAtual = 1;

    // Nova flag
    private boolean incrementarNoProximoFoco = false;

    // Fim de todos os ciclos
    @FXML private VBox endMessagePane;

    @FXML
    public void initialize() {

        App.principalController = this;
        
        // Carrega as configurações salvas
        tempoFoco = App.configGlobal.getFoco() * 60;
        tempoPausaCurta = App.configGlobal.getPausaCurta() * 60;
        tempoPausaLonga = App.configGlobal.getPausaLonga() * 60;
        ciclosTotal = App.configGlobal.getCiclos();
        int ciclosParaPausaLonga = App.configGlobal.getCicloPausaLonga();

        if (ciclosParaPausaLonga <= 0) {
            ciclosParaPausaLonga = ciclosTotal;
            App.configGlobal.setCicloPausaLonga(ciclosParaPausaLonga);
        }
        
        // Barra de progresso diário inicial
        updateDailyProgress(0);

        // Seta o primeiro tempo
        tempoTotal = tempoFoco;
        tempoRestante = tempoTotal;

        atualizarTimer();
        atualizarArcos();
        atualizarFocoLabel();
        atualizarResumo();

        // Imagem inicial da nuvem do cronometro
        statusImage.setImage(imgParado);
        
        // Animação de pulso da nuvem
        pulseAnimation = new ScaleTransition(Duration.seconds(1), statusImage);
        pulseAnimation.setFromX(1.0);
        pulseAnimation.setFromY(1.0);
        pulseAnimation.setToX(1.15);
        pulseAnimation.setToY(1.15);
        pulseAnimation.setCycleCount(ScaleTransition.INDEFINITE);
        pulseAnimation.setAutoReverse(true);
    }

    private void atualizarArcos() {
        double progresso = (double) tempoRestante / tempoTotal;
        double angulo = -360 * progresso;

        progressArc.setLength(angulo);
        bgArc.setLength(angulo);
    }

    private Image imgParado = new Image(getClass().getResource("/imagens/nuvem-limpa.png").toExternalForm());
    private Image imgRodando = new Image(getClass().getResource("/imagens/nuvem-chuva.png").toExternalForm());

    @FXML
    public void startTimer() {

        if (timeline != null && timeline.getStatus() == Timeline.Status.RUNNING) return;

        System.out.println("Timer iniciado.");

        statusImage.setImage(imgRodando);
        pulseAnimation.play();

        btnPause.setVisible(true);
        btnStart.setVisible(false);

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {

            tempoRestante--;

            atualizarTimer();
            atualizarArcos();

            if (tempoRestante <= 0) {
                 timeline.stop();

                // Animação volta para o estado parado
                statusImage.setImage(imgParado);
                pulseAnimation.stop();
                statusImage.setScaleX(1);
                statusImage.setScaleY(1);

                btnPause.setVisible(false);
                btnStart.setVisible(true);

                if (etapa == 2) { 
                    // Fim da pausa longa, registra no resumo o ciclo (que é igual a 4 pomodoros completos)
                    resumo.registrarCiclo();
                    atualizarResumo();
                }

                proximaEtapa(); // Confere na parte lógica se deve ir para a próxima etapa (sistema de avanço automático)
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    @FXML
    public void pauseTimer() {
        if (timeline != null) {
            timeline.pause();
            btnStart.setVisible(true);
            btnPause.setVisible(false);
        }

        statusImage.setImage(imgParado);
        pulseAnimation.stop();
        statusImage.setScaleX(1);
        statusImage.setScaleY(1);
    }

    @FXML
    public void resetTimer() {
        if (timeline != null)
            timeline.stop();

        tempoRestante = tempoTotal;
        atualizarTimer();
        atualizarArcos();

        statusImage.setImage(imgParado);
        pulseAnimation.stop();

        btnStart.setVisible(true);
        btnPause.setVisible(false);
    }

    private void atualizarTimer() {
        int minutos = tempoRestante / 60;
        int segundos = tempoRestante % 60;
        timerLabel.setText(String.format("%02d:%02d", minutos, segundos));
    }

    // Lógica para avançar para a próxima etapa do Pomodoro

    private void proximaEtapa() {
        int limite = App.configGlobal.getCicloPausaLonga();

        if (limite <= 0) {
            limite = App.configGlobal.getCiclos(); // Evita divisão por zero
        } 

        if (etapa == 0) { 
            // Fim do foco

            // Atualiza o resumo, colocando o registro do foco
            resumo.registrarFoco(tempoFoco / 60);  
            atualizarResumo();
            
            if (cicloAtual < limite) {
                etapa = 1; // pausa curta
                incrementarNoProximoFoco = false;
            }           
            else {
                etapa = 2; // pausa longa
                incrementarNoProximoFoco = false;
            }
        }

        else if (etapa == 1) {            
            // Fim da pausa curta
            etapa = 0;      // Volta para o foco
            resumo.registrarPomodoro();
            incrementarNoProximoFoco = true;
            atualizarResumo();
        }

        else if (etapa == 2) {
            // Fim da pausa longa
            cicloAtual = 1;
            etapa = 0;
            resumo.registrarPomodoro();
            atualizarResumo();
            
            System.out.println("Todos os ciclos concluídos!");

            // Para animação
            statusImage.setImage(imgParado);
            pulseAnimation.stop();
            statusImage.setScaleX(1);
            statusImage.setScaleY(1);

            // Botões
            btnPause.setVisible(false);
            btnStart.setVisible(true);

            Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION
                );
                alert.setTitle("Pomodoro concluído!");
                alert.setHeaderText("Todos os ciclos foram finalizados.");
                alert.showAndWait();

                // Mostrar a tela final após o alerta
                mostrarMensagemFinal();
                endMessagePane.setVisible(true);
            });

            return;
        }

        definirTempoDaEtapa();
        atualizarFocoLabel();

        if (App.configGlobal.isAutoCiclo()) {
        startTimer();
        }

        else {
            statusImage.setImage(imgParado);
            pulseAnimation.stop();
            statusImage.setScaleX(1);
            statusImage.setScaleY(1);

            btnPause.setVisible(false);
            btnStart.setVisible(true);
        }
    }

    private void definirTempoDaEtapa() {
        switch (etapa) {
            case 0: 
            // Entrou em foco
            tempoTotal = tempoFoco;
            
            if (incrementarNoProximoFoco) {
                cicloAtual++;
                incrementarNoProximoFoco = false;
            }
            break;

            case 1:
            tempoTotal = tempoPausaCurta;
            break;

            case 2:
            tempoTotal = tempoPausaLonga;
            break;
        }

        tempoRestante = tempoTotal;
        atualizarTimer();
        atualizarArcos();
    }

    private void atualizarFocoLabel() {
        focoLabel.setText((etapa == 0 ? "Foco" : etapa == 1 ? "Pausa Curta" : "Pausa Longa")
                          + " - Ciclo " + cicloAtual + "/" + ciclosTotal);
    }

    // Abrir as configurações em uma nova janela

    @FXML
    private void openSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/aplicacao/config.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Configurações");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void reiniciarPomodoro() {

        endMessagePane.setVisible(false);

        progressArc.setVisible(true);
        bgArc.setVisible(true);
        circlefundo1.setVisible(true);
        circlefundo2.setVisible(true);
        statusImage.setVisible(true);
        timerLabel.setVisible(true);

        focoLabel.setVisible(true);

        btnStart.setVisible(true);
        btnPause.setVisible(false);
        btnReset.setVisible(true);
        
        etapa = 0;
        cicloAtual = 1;

        tempoTotal = tempoFoco;
        tempoRestante = tempoTotal;

        atualizarTimer();
        atualizarArcos();
        atualizarFocoLabel();

        btnStart.setVisible(true);
        btnReset.setVisible(true);
    }

    public void recarregarConfiguracoes() {
        // Recarrega os valores da config
        tempoFoco = App.configGlobal.getFoco() * 60;
        tempoPausaCurta = App.configGlobal.getPausaCurta() * 60;
        tempoPausaLonga = App.configGlobal.getPausaLonga() * 60;
        ciclosTotal = App.configGlobal.getCiclos();

        // Reinicia o estado do timer
        if (timeline != null) {
            timeline.stop();
        }

        etapa = 0;
        cicloAtual = 1;

        tempoTotal = tempoFoco;
        tempoRestante = tempoFoco;

        // Restaura a UI
        atualizarTimer();
        atualizarArcos();
        atualizarFocoLabel();

        statusImage.setImage(imgParado);
        if (pulseAnimation != null) {
            pulseAnimation.stop();

            btnStart.setVisible(true);
            btnPause.setVisible(false);

            System.out.println("Configurações recarregadas com sucesso!");
        }
    }

    private void mostrarMensagemFinal() {
        // Esconde apenas o conteúdo interno
        progressArc.setVisible(false);
        bgArc.setVisible(false);
        circlefundo1.setVisible(false);
        circlefundo2.setVisible(false);
        statusImage.setVisible(false);
        timerLabel.setVisible(false);

        btnStart.setVisible(false);
        btnPause.setVisible(false);
        btnReset.setVisible(false);

        focoLabel.setVisible(false);

        // Mostra a mensagem final
        endMessagePane.setVisible(true);
    }

    private void updateDailyProgress(double value) {

    // Texto da barra de meta diária - porcentagem
        int porcentagem = (int) (value * 100);
        dailyProgressLabel.setText("Concluído: " + porcentagem + "%");
    }

    private void setResumoText(String foco, String ciclos, String pomodoros) {
        resumoFlow.getChildren().clear();

        Text t1 = new Text("Tempo total de foco: ");
        t1.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        Text v1 = new Text(foco + "\n");
        v1.setStyle("-fx-font-size: 12px;");

        Text t2 = new Text("Ciclos concluídos no dia: ");
        t2.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        Text v2 = new Text(ciclos + "\n");
        v2.setStyle("-fx-font-size: 12px;");

        Text t3 = new Text("Pomodoros feitos hoje: ");
        t3.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        Text v3 = new Text(pomodoros);
        v3.setStyle("-fx-font-size: 12px;");

        resumoFlow.getChildren().addAll(t1, v1, t2, v2, t3, v3);
    }


    private void atualizarResumo() {
        setResumoText(
            resumo.getTotalFoco() + " minutos",
            String.valueOf(resumo.getCiclosHoje()),
            String.valueOf(resumo.getPomodorosHoje())
        );

    double progresso = (double) resumo.getTotalFoco() / meta;
    if (progresso > 1) progresso = 1;

    dailyProgressBar.setProgress(progresso);
    updateDailyProgress(progresso);
    }

    private void addTaskItem(String texto) {
        HBox item = new HBox(10);
        item.setStyle("-fx-padding: 5; -fx-background-color: #ffffff; -fx-background-radius: 8;");
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label label = new Label(texto);
        label.setStyle("-fx-font-size: 12px;");

        Button concluir = new Button("✔");
        concluir.setOnAction(e -> {
            label.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-strikethrough: true;");
        });

        Button remover = new Button("🗑");
        remover.setOnAction(e -> taskList.getChildren().remove(item));

        item.getChildren().addAll(label, concluir, remover);

        taskList.getChildren().add(item);
    }

    @FXML
    private void addTask() {
        String texto = taskInput.getText().trim();

        if (texto.isEmpty()) return;
        
        addTaskItem(texto);
        taskInput.clear();
    }
}