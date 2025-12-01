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

    // Resumo, Barra de progresso diário e sequência de dias
    @FXML private TextFlow resumoFlow;
    private Resumo resumo = new Resumo();
    private final int meta = 60;
    private StreakManager streakManager = new StreakManager();
    @FXML private Label sequenciaLabel; // Você ajusta para o ID correto do FXML

    // Tarefas
    @FXML private VBox taskList;
    @FXML private TextField taskInput;
    private HBox tarefaSelecionada = null;
    private String textoTarefaSelecionada = null;
    private boolean timerRodando = false;
    private boolean carregando = false;
    
    // Controle do timer
    private int tempoTotal;
    private int tempoRestante;

    private int etapa = 0;
    private int cicloAtual = 1;

    // JSON
    private DataModel data;

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

        data = JSONManager.carregarTudo();
        resumo = data.resumo;

        data.tasks = data.tasks.stream()
        .distinct()
        .collect(java.util.stream.Collectors.toList());
        JSONManager.salvarTudo(data);
        
        resumo.verificarViradaDia();
        setResumoText(
            resumo.getTotalFoco() + " minutos",
            String.valueOf(resumo.getCiclosHoje()),
            String.valueOf(resumo.getPomodorosHoje())
        );

        dailyProgressBar.setProgress(resumo.getProgressoDiario());
        dailyProgressLabel.setText("Concluído: " + (int)(resumo.getProgressoDiario() * 100) + "%");

        // Carrega streak
        streakManager = new StreakManager(data.streak, data.ultimoDiaCompleto);
        sequenciaLabel.setText(String.valueOf(streakManager.getStreak()));

        // Carrega tarefas
        carregando = true;
        for (String t : data.tasks) {
            HBox item = addTaskItem(t);

            if (data.tasksConcluidas.contains(t)) {
                Label label = (Label) item.getChildren().get(0);
                label.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-strikethrough: true;");
            }
        }
        carregando = false;

        // Sequência de dias
        sequenciaLabel.setText(String.valueOf(streakManager.getStreak()));

        // Seta o primeiro tempo
        tempoTotal = tempoFoco;
        tempoRestante = tempoTotal;

        atualizarTimer();
        atualizarArcos();
        atualizarFocoLabel();

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

        timerRodando = true;

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

                    // Atualiza a sequência de dias
                    streakManager.registerCycleToday();

                    data.streak = streakManager.getStreak();
                    data.ultimoDiaCompleto = (streakManager.getLastDay() == null ? null : streakManager.getLastDay().toString());
                    JSONManager.salvarTudo(data);
                    
                    sequenciaLabel.setText(String.valueOf(streakManager.getStreak()));
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
            timerRodando = false;
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
        if (timeline != null) timeline.stop();
        timerRodando = false;

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

    // Avançar para a próxima etapa do Pomodoro
    private void proximaEtapa() {
        if (etapa == 0) { 
            // Fim do foco
            resumo.registrarFoco(tempoFoco / 60);
            atualizarResumo();

            if (cicloAtual % 4 == 0) {
                etapa = 2; // pausa longa
                incrementarNoProximoFoco = false;
            }
            
            else {
                etapa = 1; // pausa curta
                incrementarNoProximoFoco = false;
            }
        }

        else if (etapa == 1) {            
            // Fim da pausa curta
            etapa = 0;
            resumo.registrarPomodoro();
            incrementarNoProximoFoco = true;
            atualizarResumo();
        }

        else if (etapa == 2) {
            // Fim da pausa longa
            etapa = 0;
            resumo.registrarPomodoro();
            incrementarNoProximoFoco = true;
            atualizarResumo();
        }

        if (tarefaSelecionada != null) {
            taskList.getChildren().remove(tarefaSelecionada);

            if (textoTarefaSelecionada != null) {
                data.tasks.removeIf(t -> t.equals(textoTarefaSelecionada));
                data.tasksConcluidas.removeIf(t -> t.equals(textoTarefaSelecionada));
            }

            JSONManager.salvarTudo(data);

            tarefaSelecionada = null;
            textoTarefaSelecionada = null;
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
            if (incrementarNoProximoFoco) {
                cicloAtual++;
                incrementarNoProximoFoco = false;
            }

            tempoTotal = tempoFoco;
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
        String base = (etapa == 0 ? "Foco" : etapa == 1 ? "Pausa Curta" : "Pausa Longa")
            + " - Ciclo " + cicloAtual + "/" + ciclosTotal;

        focoLabel.setText(base);
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

            // Ícone de Configurações <3
            Image icon2 = new Image(App.class.getResource("/imagens/engrenagem.png").toExternalForm());
            stage.getIcons().add(icon2);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void finalizarPomodoro() {
        System.out.println("Todos os ciclos concluídos!");

        // Registrar ciclo final (se fizer sentido)
        resumo.registrarCiclo();
        atualizarResumo();

        // Atualiza streak
        streakManager.registerCycleToday();
        data.streak = streakManager.getStreak();
        data.ultimoDiaCompleto = (streakManager.getLastDay() == null ? null : streakManager.getLastDay().toString());
        JSONManager.salvarTudo(data);
        sequenciaLabel.setText(String.valueOf(streakManager.getStreak()));

        // Para animação
        if (pulseAnimation != null) pulseAnimation.stop();
        statusImage.setImage(imgParado);
        statusImage.setScaleX(1);
        statusImage.setScaleY(1);

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

        resumo.setProgressoDiario(value);
        JSONManager.salvarTudo(data);
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
    dailyProgressLabel.setText("Concluído: " + (int)(progresso * 100) + "%");
    
    resumo.setProgressoDiario(progresso);

    data.resumo = resumo;
    JSONManager.salvarTudo(data);
    }

    private HBox addTaskItem(String texto) {
        HBox item = new HBox(10);
        item.setStyle("-fx-padding: 5; -fx-background-color: #ffffff; -fx-background-radius: 8;");
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label label = new Label(texto);
        label.setStyle("-fx-font-size: 12px;");

        Button concluir = new Button("✔");
        concluir.setOnAction(e -> {
            label.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-strikethrough: true;");

            if (!data.tasksConcluidas.contains(texto)) {
                data.tasksConcluidas.add(texto);
                JSONManager.salvarTudo(data);
            }
        });

        if (!carregando && !data.tasks.contains(texto)) {
            data.tasks.add(texto);
            JSONManager.salvarTudo(data);
        }


        Button remover = new Button("🗑");
        remover.setOnAction(e -> {
            if (timerRodando) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING
                );
                alert.setTitle("Ação não permitida");
                alert.setHeaderText("Você não pode remover uma tarefa enquanto o cronômetro está ativo.");
                alert.show();
                return;
            }
            
            taskList.getChildren().remove(item);
            data.tasks.removeIf(t -> t.equals(texto));

            JSONManager.salvarTudo(data);
            
            if (tarefaSelecionada == item) {
                tarefaSelecionada = null;
                textoTarefaSelecionada = null;

                atualizarFocoLabel();
            }
        });

        item.setOnMouseClicked(e -> {
             if (timerRodando) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING
                );
                alert.setTitle("Ação não permitida");
                alert.setHeaderText("Você não pode selecionar/trocar de tarefa enquanto o cronômetro está ativo.");
                alert.show();
                return;
            }

            if (tarefaSelecionada == item) {
                item.setStyle("-fx-padding: 5; -fx-background-color: #ffffff;");
                tarefaSelecionada = null;
                textoTarefaSelecionada = null;
                atualizarFocoLabel();
                return;
            }

            // Se tinha outra selecionada, tira o destaque dela
            if (tarefaSelecionada != null) {
                tarefaSelecionada.setStyle("-fx-padding: 5; -fx-background-color: #ffffff;");
            }

            // Marca a nova
            tarefaSelecionada = item;
            textoTarefaSelecionada = texto;
            item.setStyle("-fx-padding: 5; -fx-background-color: #c7f2ff;");

            atualizarFocoLabel();
        });

        item.getChildren().addAll(label, concluir, remover);
        taskList.getChildren().add(item);
        return (item);
    }

    @FXML
    private void addTask() {
        String texto = taskInput.getText().trim();

        if (texto.isEmpty()) return;
        
        if (texto.length() > 20) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.WARNING
            );
            alert.setTitle("Texto muito longo");
            alert.setHeaderText("A tarefa deve ter no máximo 20 caracteres.");
            alert.show();
            return;
        }

        addTaskItem(texto);
        taskInput.clear();
    }

    public void setData(DataModel data) {
        this.data = data;
    }
}