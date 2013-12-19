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

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class SpringODE_old implements FirstOrderDifferentialEquations {

    private double m; // blob mass
    private double k; // spring constant
    private double R; // rest length of spring
    private double b; // damping factor
    private double g; // gravitational constant
    private double Tx; // x location of anchor point
    private double Ty; // y location of anchor point

    public SpringODE_old() {
        m = 10.0;
        k = 1;
        R = 100;
        b = 0.1;
        g = 9.81;
        Tx = 400;
        Ty = 200;
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

        double uxMinusTx = ux - Tx;
        double uyMinusTy = uy - Ty;

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
        return Tx;
    }

    /**
     * @param Tx the Tx to set
     */
    public void setTx(double Tx) {
        this.Tx = Tx;
    }

    /**
     * @return the Ty
     */
    public double getTy() {
        return Ty;
    }

    /**
     * @param Ty the Ty to set
     */
    public void setTy(double Ty) {
        this.Ty = Ty;
    }

}
