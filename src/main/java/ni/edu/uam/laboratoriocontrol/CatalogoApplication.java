package ni.edu.uam.laboratoriocontrol;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ni.edu.uam.laboratoriocontrol.database.JPAUtil;

import java.io.IOException;

public class CatalogoApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CatalogoApplication.class.getResource("laboratorio-control-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Control de Equipos de Laboratorio");
        stage.setScene(scene);
        stage.show();
        // Evita que la ventana se achique más de su tamaño inicial
        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());
    }

    @Override
    public void stop() {
        // Cierra el EntityManagerFactory al salir de la aplicación
        JPAUtil.cerrar();
    }
}