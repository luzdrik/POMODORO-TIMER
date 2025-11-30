package aplicacao;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;


public class App extends Application {

    private static Scene scene;
    
    public static Config configGlobal = new Config();
    public static principal principalController;
    public static Resumo resumoGlobal = JSONManager.carregarResumo();


    @Override
    public void start(Stage stage) throws IOException {

        scene = new Scene(loadFXML("main"), 640, 480);

        scene.getStylesheets().add(
            App.class.getResource("/styles.css").toExternalForm()
        );

        stage.setTitle("Pomodoro Timer!");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

        // Ícone da API <3
        Image icon = new Image(App.class.getResource("/imagens/icone.png").toExternalForm());
        stage.getIcons().add(icon);

        stage.setScene(scene);
        stage.show();
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
}