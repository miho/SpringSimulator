/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.springsimulation;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math3.ode.nonstiff.EulerIntegrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class SpringSimulation {

    private double[] y;
    private double[] yPrev;
    private FirstOrderIntegrator integrator;
    private SpringODE ode;
    private Pane root;

    private final DoubleProperty xProperty = new SimpleDoubleProperty();
    private final DoubleProperty yProperty = new SimpleDoubleProperty();

    private Timeline timeline;

    private long lastTimeStamp;

    private double remainingSimulationTime;
    private double dt = 0.01;
    private double t = 0;
    private double[] interpolatedY;
    private boolean done;

    public SpringSimulation() {
        ode = new SpringODE();
    }

    public void start() {
//        new Thread(() -> {
        start_();
//        }).start();
    }

    public void start_() {

//        integrator = new DormandPrince853Integrator(1e-6, 1e-4, 1e-3, 1e-3);
//        integrator = new EulerIntegrator(1e-4);

        integrator = new ClassicalRungeKuttaIntegrator(dt);

        y = new double[]{700, 800, 0, 0}; // initial state
        yPrev = new double[y.length];

        boolean isViewRegistered = root != null;

        if (!isViewRegistered) {
            throw new IllegalStateException("");
        }

        interpolatedY = new double[y.length];

        AnimationTimer frameListener = new AnimationTimer() {

            @Override
            public void handle(long now) {
                double frameDuration = (now - lastTimeStamp) / 1e9;
                lastTimeStamp = now;

                if (frameDuration > 1) {
                    frameDuration = 1;
                }

                remainingSimulationTime += frameDuration;

                System.arraycopy(y, 0, yPrev, 0, yPrev.length);

                while (remainingSimulationTime >= dt) {

                    double tPlusDt = t + dt;

                    integrator.integrate(ode, t, y, tPlusDt, y);

                    remainingSimulationTime -= dt;

                    t = tPlusDt;

                }

                // interpolate state
                double alpha = remainingSimulationTime / dt;

                // set interpolated state
                for (int i = 0; i < y.length; i++) {
                    interpolatedY[i] = y[i] * alpha + yPrev[i] * (1.0 - alpha);
                }

                updateView(interpolatedY);
            }
        };

        frameListener.start();

//        testTransition();
//
//        if (true) {
//            return;
//        }
//        integrator = new DormandPrince853Integrator(1e-6, 1e-4, 1e-3, 1e-3);
////        integrator = new EulerIntegrator(1e-4);
//
////        integrator = new ClassicalRungeKuttaIntegrator(1e-4);
////        integrator.setMaxEvaluations(1000000);
//        y = new double[]{700, 800, 0, 0}; // initial state
//        yPrev = new double[y.length];
//
//        boolean isViewRegistered = root != null;
//
//        if (!isViewRegistered) {
//            throw new IllegalStateException("");
//        }
//        AnimationTimer timer = new AnimationTimer() {
//
//            @Override
//            public void handle(long now) {
//
//                double dt = (now - lastTimeStamp) / 1e9;
//
////                System.out.println("dt: " + dt);
//                tPlus1 = t + dt;
//
//                if (lastTimeStamp > 0) {
////                            System.out.println("update: [" + t + ", " + tPlus1 + "]");
//                    integrator.integrate(ode, t, y, tPlus1, y);
//                    t = tPlus1;
//                }
//                lastTimeStamp = now;
//                
//                
////
//                
////                if (lastThread == null || (!lastThread.isAlive() && tPlus1 - t > 0.5)) {
////                    lastThread = new Thread(() -> {
////                        if (lastTimeStamp > 0) {
////
//////                            System.out.println("update: [" + t + ", " + tPlus1 + "]");
////                            integrator.integrate(ode, t, y, tPlus1, y);
////                            t = tPlus1;
////                        }
////
////                        lastTimeStamp = now;
////                    });
////                    lastThread.start();
////                }
//                updateView();
//            }
//        };
//
//        StepHandler stepHandler = new StepHandler() {
//            @Override
//            public void init(double t0, double[] y0, double t) {
////                updateView();
//            }
//
//            @Override
//            public void handleStep(StepInterpolator interpolator, boolean isLast) {
//
//                double interpolatedY[] = interpolator.getInterpolatedState();
//
//                System.arraycopy(interpolatedY, 0, yCurrent, 0, yCurrent.length);
//            }
//        };
//        integrator.addStepHandler(stepHandler);
//        timer.start();
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

//        Platform.runLater(() -> {
        root.getChildren().addAll(anchor, blob, p);
//        });

        blob.layoutXProperty().bind(xProperty);
        blob.layoutYProperty().bind(yProperty);
    }

    private void updateView(double[] state) {
//        xProperty.set(yCurrent[0]);
//        yProperty.set(yCurrent[1]);

//        xProperty.set(y[0]);
//        yProperty.set(y[1]);
        double xPos = state[0];
        double yPos = state[1];

        xProperty.set(xPos);
        yProperty.set(yPos);

//        if (timeline != null) {
//            timeline.stop();
//        }
//        
//        timeline = new Timeline(
//                new KeyFrame(
//                        Duration.ZERO,
//                        new KeyValue(xProperty, xProperty.get()),
//                        new KeyValue(yProperty, yProperty.get())),
//                new KeyFrame(
//                        Duration.seconds(10),
//                        new KeyValue(xProperty, xPos),
//                        new KeyValue(yProperty, yPos)));
//
////        timeline.
//        timeline.play();
    }

    public void testTransition() {
        timeline = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(xProperty, xProperty.get()),
                        new KeyValue(yProperty, yProperty.get())),
                new KeyFrame(
                        Duration.seconds(10),
                        new KeyValue(xProperty, 600),
                        new KeyValue(yProperty, 600)));

        timeline.play();
    }

}
