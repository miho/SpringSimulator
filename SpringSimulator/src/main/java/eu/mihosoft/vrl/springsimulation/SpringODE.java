/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.springsimulation;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import static java.lang.Math.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class SpringODE implements FirstOrderDifferentialEquations {

    private double m; // blob mass
    private double k; // spring constant
    private double R; // rest length of spring
    private double b; // damping factor
    private double g; // gravitational constant
    
    private Double uX = null;
    private Double uY = null;
    
    private final DoubleProperty txProperty = new SimpleDoubleProperty(); // x location of anchor point
    private final DoubleProperty tyProperty = new SimpleDoubleProperty(); // y location of anchor point
    
    private final DoubleProperty uxProperty = new SimpleDoubleProperty(); // x location of blob point
    private final DoubleProperty uyProperty = new SimpleDoubleProperty(); // y location of blob point

    public SpringODE() {
        m = 10.0;
        k = 10;
        R = 100;
        b = 2;
        g = 9.81;

        setTx(400);
        setTy(100);
    }

    @Override
    public int getDimension() {
        return 4;
    }

    @Override
    public void computeDerivatives(double t, double[] y, double[] yDot)
            throws MaxCountExceededException, DimensionMismatchException {

        double ux = y[0];
        double uy = y[1];
        
        double vx = y[2];
        double vy = y[3];

        double uxMinusTx = ux - getTx();
        double uyMinusTy = uy - getTy();

        double L = sqrt(uxMinusTx * uxMinusTx + uyMinusTy * uyMinusTy);

        double S = L - R; // spring displacement

        double sinTheta = uxMinusTx / L;
        double cosTheta = uyMinusTy / L;

        // compute new u
        yDot[0] = vx;
        yDot[1] = vy;

        // compute new v
        yDot[2] = -k / m * S * sinTheta - b / m * vx;
        yDot[3] = g - k / m * S * cosTheta - b / m * vy;

    }

    /**
     * @return the Tx
     */
    public double getTx() {
        return txProperty().get();
    }

    /**
     * @param Tx the Tx to set
     */
    public void setTx(double Tx) {
        txProperty().set(Tx);
    }

    /**
     * @return the Ty
     */
    public double getTy() {
        return tyProperty().get();
    }

    /**
     * @param Ty the Ty to set
     */
    public void setTy(double Ty) {
        tyProperty().set(Ty);
    }

    /**
     * @return the txProperty
     */
    public DoubleProperty txProperty() {
        return txProperty;
    }

    /**
     * @return the tyProperty
     */
    public DoubleProperty tyProperty() {
        return tyProperty;
    }

    /**
     * @return the uxProperty
     */
    public DoubleProperty uxProperty() {
        return uxProperty;
    }

    /**
     * @return the uyProperty
     */
    public DoubleProperty uyProperty() {
        return uyProperty;
    }

}
