/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.springsimulation;

import eu.mihosoft.vrl.workflow.fx.NodeUtil;
import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import jfxtras.labs.util.event.MouseControlUtil;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;

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
    private boolean done = true;


    public SpringSimulation() {
        ode = new SpringODE();
    }

    public void start(double dt) {

        if (isRunning()) {
            throw new RuntimeException("Stop simulation before restarting it!");
        }

        boolean isViewRegistered = root != null;

        if (isViewRegistered) {
            startVisual(dt);
        } else {
            new Thread(() -> {
                startNonVisual(dt);
            }).start();
        }
    }

    public void stop() {
        done = true;
    }

    public boolean isRunning() {
        return !done;
    }

    private void startNonVisual(double dt) {
        throw new UnsupportedOperationException("implementation missing!");
    }

    private void startVisual(double dt) {

        // integrator
//        integrator = new ClassicalRungeKuttaIntegrator(dt);
        integrator = new DormandPrince853Integrator(1e-6, 1.0, 1e-4, 1e-4);

        double[] y = new double[]{400, 200, 0, 0}; // initial state
        double[] yPrev = new double[y.length]; // previous simulation state

        interpolatedY = new double[y.length];

        ode.uxProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                y[0] = t1.doubleValue();
            }
        });

        ode.uyProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                y[1] = t1.doubleValue();
            }
        });

        // create frame listener 
        AnimationTimer frameListener = new AnimationTimer() {

            @Override
            public void handle(long now) {

                // thanks to http://gafferongames.com/game-physics/fix-your-timestep/
                // measure elapsed time between last and current pulse (frame)
                double frameDuration = (now - lastTimeStamp) / 1e9;
                lastTimeStamp = now;

//                System.out.println("frameDuration: " + frameDuration + ", t: " + t);
                // we don't allow frame durations above 2*dt
                if (frameDuration > 2 * dt) {
                    frameDuration = 2 * dt;
                }

                // add elapsed time to remaining simulation interval
                remainingSimulationTime += frameDuration;

                // copy current state to prev state
                System.arraycopy(y, 0, yPrev, 0, yPrev.length);

                // simulate remaining interval
                while (remainingSimulationTime >= dt) {

                    double tPlusDt = t + dt;

                    // integrate
                    try {
                        integrator.integrate(ode, t * 10, y, tPlusDt * 10, y);

                    } catch (Exception ex) {
                        ex.printStackTrace(System.err);
                    }
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

        Circle anchor = new Circle(10);
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

        ode.txProperty().bind(anchor.layoutXProperty());
        ode.tyProperty().bind(anchor.layoutYProperty());

        MouseControlUtil.makeDraggable(anchor);

        MouseControlUtil.makeDraggable(blob);

        blob.layoutXProperty().bind(xProperty);
        blob.layoutYProperty().bind(yProperty);

        blob.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent t) {

                blob.layoutXProperty().unbind();
                blob.layoutYProperty().unbind();

                ode.uxProperty().bind(blob.layoutXProperty());
                ode.uyProperty().bind(blob.layoutYProperty());
            }
        });

        blob.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent t) {
                ode.uxProperty().unbind();
                ode.uyProperty().unbind();

                blob.layoutXProperty().bind(xProperty);
                blob.layoutYProperty().bind(yProperty);
            }
        });
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
