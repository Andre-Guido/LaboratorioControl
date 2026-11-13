package ni.edu.uam.laboratoriocontrol;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class CatalogoApplication {
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CatalogoApplication.class.getResource("laboratorio-control-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Laboratorio");
        stage.setScene(scene);
        stage.show();
    }
}
