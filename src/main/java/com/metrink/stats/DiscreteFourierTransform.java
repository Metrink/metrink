package com.metrink.stats;

import org.apache.commons.math3.util.FastMath;


/**
 * This is the full O(n^2) implementation of the DFT, keeping <b>only</b> the real parts.
 */
public class DiscreteFourierTransform {

    public static double[] forward(double[] arg) {
        if(arg == null || arg.length == 0) {
            throw new IllegalArgumentException("Empty array");
        }
        
        final double[] ret = new double[arg.length];

        for(int k=0; k < arg.length; ++k) {
            ret[k] = 0.0;
            
            double x = (-2.0 * FastMath.PI * k) / arg.length;
            
            for(int n=0; n < arg.length; ++n) {
                ret[k] += arg[n] * FastMath.cos(x * n); // we only care about the real part
            }
        }
        
        return ret;
    }

    public static double[] inverse(double[] arg) {
        if(arg == null || arg.length == 0) {
            throw new IllegalArgumentException("Empty array");
        }
        
        final double[] ret = new double[arg.length];

        for(int k=0; k < arg.length; ++k) {
            ret[k] = 0.0;
            
            double x = (2.0 * FastMath.PI * k) / arg.length;
            
            for(int n=0; n < arg.length; ++n) {
                ret[k] += arg[n] * FastMath.cos(x * n); // we only care about the real part
            }
            
            ret[k] /= arg.length;
        }
        
        return ret;
    }
}
