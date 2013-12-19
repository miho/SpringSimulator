package eu.mihosoft.vrl.springsimulation;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {

        Pane root = new Pane();
        root.setPrefSize(800, 600);
        root.setStyle("-fx-background-color: black;");

        System.out.println("-> strting simulation");

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Spring Physics");
        primaryStage.setScene(scene);
        primaryStage.show();

        SpringSimulation simulation = new SpringSimulation();
        
        simulation.setView(root);
        
        simulation.start(0.005);
        
        System.out.println(" -> running");
        
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent t) {
                System.out.println("STOP");
//                simulation.stop();
                System.exit(0);
            }
        });
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    
}
