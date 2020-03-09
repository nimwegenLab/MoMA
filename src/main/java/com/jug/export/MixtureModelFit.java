/*
 * Copyright (C) 2018 Jean Ollion
 *
 * This File is part of BACMMAN
 *
 * BACMMAN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BACMMAN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BACMMAN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jug.export;

import ij.gui.Plot;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;
import java.util.stream.IntStream;

import static com.jug.MoMA.*;

/**
 *
 * @author Jean Ollion
 */
public class MixtureModelFit {
    boolean verbose;

    /**
     * Performs the fluorescence fit around the object {@param bacteria}, see {@link #fitFluo(double[], double, double[], double, double[], int, double)} for the algorithm details
     * @param bacteria observation of a segmented bacteria at a given frame.
     * The SegmentedObject class gives access to:
     * - the tracking links information (previous, next, and trackhead)
     * - the parent (in the case of bacteria the parent is the microchannel object that contains the bacteria) and children if existing (e.g. the bacteria for a microchannel object)
     * - the raw images cropped according to the bounds of the object
     * - the region which contains the spatial information (bounding box, voxels, mask)
     * This method does not return anything, but resulting values are stored in the measurements of the {@param bacteria}
     */
    public double[] performMeasurement(SegmentRecord bacteria, IntervalView<FloatType> columnBoxInChannel) {
        final float[] column_intensities_float = bacteria.computeChannelColumnIntensities(columnBoxInChannel);
        final double[] observedFluo = IntStream.range(0, column_intensities_float.length)
                .mapToDouble(i -> column_intensities_float[i]).toArray();

        // initial parameter values & bounds, as in Kaiser 2018. Might be included as parameters in order to better adapt to other systems (e.g with wider bacteria) ?
        double muStart = observedFluo.length / 2; // middle of peak starts at middle of bacteria object. -xMin because in the fitting function the index is 0-based
        double wStart = INTENSITY_FIT_INITIAL_WIDTH; // in a more general case, should it be a value depending on bacteria width ?
        double[] muBounds = new double[]{muStart - 10, muStart + 10};
        double[] wBounds = new double[] {3, 12};
        double precision = INTENSITY_FIT_PRECISION;
        int maxIterations = INTENSITY_FIT_ITERATIONS;

        // actual call to the fitting method
        double[] fittedParams = fitFluo(observedFluo, muStart, muBounds, wStart, wBounds, maxIterations, precision);
        if (verbose) plot(observedFluo, fittedParams);
        return fittedParams;
    }


    // processing functions

    /**
     * Fits fluorescence profile {@param c_i} with a Lorenzian distribution using expectation maximization procedure as described in: Kaiser, Matthias, Florian Jug, Thomas Julou, Siddharth Deshpande, Thomas Pfohl, Olin K. Silander, Gene Myers, and Erik van Nimwegen. 2018. “Monitoring Single-Cell Gene Regulation Under Dynamically Controllable Conditions with Integrated Microfluidics and Software.” Nature Communications 9 (1):212. https://doi.org/10.1038/s41467-017-02505-0.
     * ci = noise + B + A / ( (1 + i - mu) / w )^2
     * @param c_i observed fluorescence profile
     * @param wStart starting value for width of distribution
     * @param muBounds range [muMin, muMax] for mu optimization
     * @param muStart starting value for center of peak
     * @param wBounds range [wMin, wMax] for w optimization
     * @param maxIterationNumber maximum iteration number
     * @param precision precision of parameter search
     * @return array with fitted values of  A, B, i_mid, w
     */
    public static double[] fitFluo(double[] c_i, double muStart, double[] muBounds, double wStart, double[] wBounds, int maxIterationNumber, double precision) {
        // step 1
        double cMax = Arrays.stream(c_i).max().getAsDouble();
        double cMin = Arrays.stream(c_i).min().getAsDouble();
        // step 2
        double[] params = new double[] {cMax - cMin, cMin, muStart, wStart};
        double[] ro_i = new double[c_i.length];
        List<Double> diffList = new ArrayList<>(maxIterationNumber);
        int iteration = 0;
        double diff = Double.POSITIVE_INFINITY;
        while (iteration++ < maxIterationNumber && diff > precision) diffList.add(diff = iterate(c_i, ro_i, params, muBounds, wBounds, precision));

        //Utils.plotProfile("Convergence", ArrayUtil.toPrimitive(diffList), "iteration number", "sum of parameter relative error");
        return params;
    }
    /**
     * Updates {@param parameters} array after one round of parameter fitting
     * @param c_i observed values
     * @param ro_i estimated values of ro_i. ro_i = 1 / (1 + ( (i - mu) / w )^2 )
     * @param parameters parameter array (0=A, 1=B, 2=iMid (mu), 3=w)
     * @param muBounds range for mu optimisation
     * @param wBounds range for w optimisation
     * @param precision precision for mi and w search
     */
    private static double iterate(double[] c_i, double[] ro_i, double[] parameters, double[] muBounds, double[] wBounds, double precision) {
        // update ro_i array using previous parameters
        IntStream.range(0, c_i.length).forEach(i -> ro_i[i] = 1/(1+Math.pow((i-parameters[2])/parameters[3], 2)));

        // step 3: compute ro
        Function<IntToDoubleFunction, Double> sum = fun -> IntStream.range(0, c_i.length).mapToDouble(fun).sum();
        double ro = Arrays.stream(ro_i).sum();

        // step 4: compute new value of B
        double B =  1d/c_i.length * sum.apply(i -> parameters[1] * c_i[i] / (parameters[1] + parameters[0] * ro_i[i]) );

        // step 5: compute new value of A
        double A =  1d/ro * sum.apply(i -> parameters[0] * c_i[i] * ro_i[i] / (parameters[1] + parameters[0] * ro_i[i]) );

        // step 6
        // find mu using root of derivative function with updated values of A and B
        DoubleBinaryOperator ro_f_i_mu = (i, mu) -> 1/(1+Math.pow((i-mu)/parameters[3], 2));
        DoubleUnaryOperator d_mu = mu -> sum.apply(i-> {
            double ro_i_ = ro_f_i_mu.applyAsDouble(i, mu);
            return (i-mu)*ro_i_*ro_i_ * (c_i[i] - (B + A * ro_i_) );
        } );
        double mu = getRootBisection(d_mu, 1000, precision, muBounds);

        // step 7 find new w using root of derivative function with updated values of A, B, and mu
        DoubleBinaryOperator ro_f_i_w = (i, w) -> 1/(1+Math.pow((i-mu)/w, 2));
        DoubleUnaryOperator d_w = w -> sum.apply(i-> {
            double ro_i_ = ro_f_i_w.applyAsDouble(i, w);
            return ro_i_ * (1 - ro_i_) * (-1 + c_i[i] / (B + A * ro_i_) );
        });
        double w = getRootBisection(d_w, 1000, precision, wBounds);

        // compute parameter relative error to estimate fit precision
        double diff = Math.abs((A-parameters[0])/(A+parameters[0])) + Math.abs((B-parameters[1])/(B+parameters[1])) + Math.abs((mu-parameters[2]) / (mu+parameters[2])) + Math.abs((w-parameters[3]) / (w+parameters[3]));

        // update parameters
        parameters[0] = A;
        parameters[1] = B;
        parameters[2] = mu;
        parameters[3] = w;
        //logger.debug("fit: Bck{} Fluo: {}, mu: {}, width: {}, ro: {}", B, A, mu, w, ro);
        return diff;
    }

    /**
     * Finds a root of {@param function} within range {@param bounds} using bisection method
     * @param function
     * @param maxIteration maximum number of iteration
     * @param precision precision of the search
     * @param bounds range of the search [left bound, right bound]
     * @return a root of {@param function}
     */
    public static double getRootBisection(DoubleUnaryOperator function, int maxIteration, double precision, double[] bounds) {
        double x=bounds[0], xLeft = bounds[0], xRight = bounds[1];
        double error = xRight-xLeft;
        int iter = 0;

        double fLeft = function.applyAsDouble(xLeft);
        double fx = fLeft;
        while (Math.abs(error) > precision && iter<maxIteration && fx!=0 ) {
            x = ((xLeft+xRight)/2);
            fx = function.applyAsDouble(x);
            if ((fLeft*fx) < 0) {
                xRight  = x;
                error = xRight-xLeft;
            } else {
                xLeft = x;
                error = xRight-xLeft;
                fLeft = fx;
            }
            ++iter;
        }
        return x;
    }

    public MixtureModelFit setVerbose(boolean verbose) {
        this.verbose= verbose;
        return this;
    }

    // methods to plot for debugging purpose
    private static void plot(String title, DoubleUnaryOperator function, double[] x) {
        double[] values = Arrays.stream(x).map(function).toArray();
//        Utils.plotProfile(title, values, x, null, null);
        Plot plot = new Plot(title, "x", "y");
        plot.add("profile", x, values);
        plot.show();
    }
    private static void plot(double[] observed, double[] parameters) {
        double[] estimated_c_i = IntStream.range(0, observed.length).mapToDouble(i -> parameters[1] + parameters[0] / (1 + Math.pow((i - parameters[2])/parameters[3], 2) )).toArray();
        double[] x = IntStream.range(0, observed.length).mapToDouble(i -> i).toArray(); // centered on iMid
//        Utils.plotProfile("observed and estimated fluo", estimated_c_i, x, observed, x);
        Plot plot = new Plot("Mixture Model Fit", "x", "y");
        plot.add("line", x, estimated_c_i);
        plot.add("circle", x, observed);
        plot.show();
    }
}
