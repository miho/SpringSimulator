package eu.mihosoft.vrl.springsimulation;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Main_old extends Application {

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

        new Thread(() -> {
            initSimulation(root);
        }).start();
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

    private void initSimulation(Pane root) {

        SpringODE ode = new SpringODE();

        Circle anchor = new Circle(5);
        anchor.setLayoutX(ode.getTx());
        anchor.setLayoutY(ode.getTy());
        anchor.setFill(Color.WHITE);

        Circle blob = new Circle(5);
        blob.setLayoutX(0);
        blob.setLayoutY(0);
        blob.setFill(Color.WHITE);

        MoveTo from = new MoveTo();
        from.xProperty().bind(anchor.layoutXProperty());
        from.yProperty().bind(anchor.layoutYProperty());

        LineTo to = new LineTo();
        to.xProperty().bind(blob.layoutXProperty());
        to.yProperty().bind(blob.layoutYProperty());

        Path p = new Path(from,to);
        p.setStroke(Color.WHITE);
        p.setStrokeWidth(1);

        Platform.runLater(() -> {
            root.getChildren().addAll(anchor, blob, p);
        });

        final FirstOrderIntegrator integrator = new DormandPrince853Integrator(1e-3, 1e-2, 1e-4, 1e-4);

//        integrator.setMaxEvaluations(10000);
        double[] y = new double[]{700, 800, 0, 0}; // initial state

        final double yCurrent[] = new double[y.length];

        AnimationTimer timer = new AnimationTimer() {

            @Override
            public void handle(long l) {
                blob.setLayoutX(yCurrent[0]);
                blob.setLayoutY(yCurrent[1]);
            }
        };

        timer.start();

        StepHandler stepHandler = new StepHandler() {
            @Override
            public void init(double t0, double[] y0, double t) {
                blob.setLayoutX(y0[0]);
                blob.setLayoutY(y0[1]);
            }

            @Override
            public void handleStep(StepInterpolator interpolator, boolean isLast) {

                double interpolatedY[] = interpolator.getInterpolatedState();

                System.arraycopy(interpolatedY, 0, yCurrent, 0, yCurrent.length);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main_old.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        integrator.addStepHandler(stepHandler);
        integrator.integrate(ode, 0, y, 100, y);

    }
}
