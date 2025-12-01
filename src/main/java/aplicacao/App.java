package aplicacao;

import java.io.IOException;
import javafx.application.Platform;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class App extends Application {

    private static Scene scene;
    
    public static principal principalController;

    // Carrega tudo do JSON
    public static DataModel data;

    // Resumo e Config vêm de "data"
    public static Resumo resumoGlobal;
    public static Config configGlobal;

    @Override
    public void start(Stage stage) throws IOException {

        // Tela de Splash
        Image logoImage = new Image(getClass().getResource("/imagens/logo.png").toExternalForm());
        ImageView logoView = new ImageView(logoImage);
        logoView.setFitWidth(300);
        logoView.setFitHeight(300);
        
        StackPane splashRoot = new StackPane(logoView);
        Stage splashStage = new Stage(StageStyle.UNDECORATED);
        Scene splashScene = new Scene(splashRoot);
        splashStage.setScene(splashScene);

        splashStage.show();

        // Thread para simular a tela de "carregamento"
        new Thread(() -> {
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            javafx.application.Platform.runLater(() -> {
                try {
                    splashStage.close();

                    scene = new Scene(loadFXML("main"), 640, 480);
                    scene.getStylesheets().add(App.class.getResource("/styles.css").toExternalForm());

                    stage.setTitle("Pomodoro Timer!");
                    stage.setResizable(false);
                    stage.setScene(scene);

                    // Ícone da API <3
                    Image icon1 = new Image(App.class.getResource("/imagens/icone.png").toExternalForm());
                    stage.getIcons().add(icon1);

                    stage.show();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }).start();
    }

    static Object setRoot(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        Parent root = loader.load();
        scene.setRoot(root);
        return loader.getController();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

    static {
        data = JSONManager.carregarTudo();

        // segurança extra (mesmo com JSONManager corrigido)
        if (data == null) data = new DataModel();

        resumoGlobal = data.getResumo();
        configGlobal = data.getConfig();
    }
}