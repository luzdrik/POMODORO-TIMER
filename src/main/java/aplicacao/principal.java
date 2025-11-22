package aplicacao;

import java.io.IOException;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.shape.Arc;
import javafx.stage.Stage;
import javafx.util.Duration;

public class principal {

    @FXML private Label timerLabel;
    @FXML private Arc progressArc;
    @FXML private Button btnStart;
    @FXML private Button btnPause;

    private Timeline timeline;
    private int tempoTotal = 25 * 60; // 25 minutos em segundos
    private int tempoRestante = tempoTotal;

    @FXML
    public void startTimer() {

        if (timeline != null && timeline.getStatus() == Timeline.Status.RUNNING) {
            return;
        }

        System.out.println("✅ Timer iniciado");

        btnPause.setVisible(true);
        btnStart.setVisible(false);

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            tempoRestante--;

            int minutos = tempoRestante / 60;
            int segundos = tempoRestante % 60;

            timerLabel.setText(String.format("%02d:%02d", minutos, segundos));

            double progresso = (double) tempoRestante / tempoTotal;
            progressArc.setLength(-360 * progresso);

            if (tempoRestante <= 0) {
                timeline.stop();
                System.out.println("⏱ Tempo finalizado!");
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    @FXML
    public void pauseTimer() {
        if (timeline != null) {
            timeline.pause();
            System.out.println("⏸ Timer pausado");

            btnStart.setVisible(true);
            btnPause.setVisible(false);
        }
    }

    @FXML
    public void resetTimer() {
        if (timeline != null) {
            timeline.stop();
        }

        tempoRestante = tempoTotal;
        timerLabel.setText("25:00");
        progressArc.setLength(-360);

        btnStart.setVisible(true);
        btnPause.setVisible(false);

        System.out.println("🔄 Timer reiniciado");
    }


    private void atualizarTimer() {
        int minutos = tempoRestante / 60;
        int segundos = tempoRestante % 60;
        timerLabel.setText(String.format("%02d:%02d", minutos, segundos));
    }

    @FXML
    private void openSettings() {
        System.out.println("Abrir configurações...");

        try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/aplicacao/config.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Configurações");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.show();

        }   
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}