/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.springsimulation;

import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class SpringSimulation {

    private FirstOrderIntegrator integrator;
    private SpringODE ode;
    private Pane root;

    private final DoubleProperty xProperty = new SimpleDoubleProperty();
    private final DoubleProperty yProperty = new SimpleDoubleProperty();

    private long lastTimeStamp;

    private double remainingSimulationTime;

    private double t = 0;
    private double[] interpolatedY;
    private boolean done;

    public SpringSimulation() {
        ode = new SpringODE();
    }

    public void start(double dt) {
//        new Thread(() -> {
//        start_();
//        }).start();

        boolean isViewRegistered = root != null;

        if (isViewRegistered) {
            startVisual(dt);
        } else {
            new Thread(() -> {
                startNonVisual(dt);
            }).start();
        }
    }

    private void startNonVisual(double dt) {
        throw new UnsupportedOperationException("implementation missing!");
    }

    private void startVisual(double dt) {

//        integrator = new DormandPrince853Integrator(1e-6, 1e-4, 1e-3, 1e-3);
//        integrator = new EulerIntegrator(1e-4);
//        
        // integrator
        integrator = new ClassicalRungeKuttaIntegrator(dt);

        double[] y = new double[]{700, 800, 0, 0}; // initial state
        double[] yPrev = new double[y.length]; // previous simulation state

        interpolatedY = new double[y.length];

        // create frame listener 
        AnimationTimer frameListener = new AnimationTimer() {

            @Override
            public void handle(long now) {

                // measure elapsed time between last and current pulse (frame)
                double frameDuration = (now - lastTimeStamp) / 1e9;
                lastTimeStamp = now;

                // we don't allow frame durations above 2*dt
                if (frameDuration > 2 * dt) {
                    frameDuration = 2 * dt;
                }

                // add elapsed time to remaining simulation interval
                remainingSimulationTime += frameDuration;

                System.arraycopy(y, 0, yPrev, 0, yPrev.length);

                // simulate remaining interval
                while (remainingSimulationTime >= dt) {

                    double tPlusDt = t + dt;

                    // integrate
                    integrator.integrate(ode, t, y, tPlusDt, y);

                    // remove integrated interval from remaining simulation time
                    remainingSimulationTime -= dt;

                    // update t
                    t = tPlusDt;
                }

                // interpolate state
                double alpha = remainingSimulationTime / dt;

                // set interpolated state
                for (int i = 0; i < y.length; i++) {
                    interpolatedY[i] = y[i] * alpha + yPrev[i] * (1.0 - alpha);
                }

                // update properties for visualization
                updateView(interpolatedY);
            }
        };

        // finally, start the framle listener
        frameListener.start();
    }

    public void setView(Pane root) {
        this.root = root;

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

        Path p = new Path(from, to);
        p.setStroke(Color.WHITE);
        p.setStrokeWidth(1);

        root.getChildren().addAll(anchor, blob, p);

        blob.layoutXProperty().bind(xProperty);
        blob.layoutYProperty().bind(yProperty);
    }

    private void updateView(double[] state) {

        double xPos = state[0];
        double yPos = state[1];

        xProperty.set(xPos);
        yProperty.set(yPos);
    }
}







//    public void testTransition() {
//        timeline = new Timeline(
//                new KeyFrame(
//                        Duration.ZERO,
//                        new KeyValue(xProperty, xProperty.get()),
//                        new KeyValue(yProperty, yProperty.get())),
//                new KeyFrame(
//                        Duration.seconds(10),
//                        new KeyValue(xProperty, 600),
//                        new KeyValue(yProperty, 600)));
//
//        timeline.play();
//    }
