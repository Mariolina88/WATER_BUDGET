package runoff;


import org.jgrasstools.gears.libs.modules.ModelsEngine;
import org.joda.time.Duration;


/**
 * The Class WFIUHKinematic.
 */
public class WFIUHKinematic {

	/** The width function */
	double [][] widthFunction;

	/** The  fluxes in input  */
	double inputFluxes;

	/** The input time step in minutes */
	int inTimestep;

	/** The area. */
	double area;

	/** The celerity of the channel */
	double pCelerity;

	/** The vector with discharge values at previous time step */
	double [] Q_i;

	/** The length of the input time series */
	int dim;

	/** The actual step. */
	int step;
	
    private double[][] Qtot = null;

	/**
	 * Instantiates a new IUH kinematic.
	 *
	 * @param widthFunction is the width function matrix
	 * @param inputFluxes are the totalInputFluxes for the time step
	 * @param inTimestep is the input time step in minutes
	 * @param area is the area
	 * @param pCelerity is the celerity of the channel
	 * @param Q_i the discharge at the prevoius time step
	 * @param dim is the length of the input time series
	 * @param step the actual computation step
	 */
	public WFIUHKinematic(double [][] widthFunction, double inputFluxes, int inTimestep, 
			double area, double pCelerity, double [] Q_i, int dim, int step){
		this.inputFluxes=inputFluxes;
		this.widthFunction=widthFunction;
		this.inTimestep=inTimestep;
		this.area=area;
		this.pCelerity=pCelerity;
		this.Q_i=Q_i;
		this.dim=dim;
		this.step=step;
	}

	/**
	 * Calculate the discharge time series.
	 *
	 * @return the double[]
	 */
	public double [] calculateQ() {

		// tcorr is the concentration time in seconds
		int tcorr =(int) widthFunction[widthFunction.length - 1][0];
		int tcorr_h=tcorr/3600;

		// is the duration of the precipitation in seconds
		int tpmax =inTimestep*60;
		int tpmax_h =inTimestep/60;
		tpmax_h=(tpmax_h>tcorr_h)?tcorr_h:tpmax_h;
		


		//is the discharge computed in m^3/s according to the WFIUH at time step i+1 (i1)
		double[] Q_i1 = new double[(int) tcorr_h + tpmax_h + 1];


		for( int t = 1; t <= tcorr_h; t += 1 ) {

			if (t <= tpmax_h) {

				Q_i1[t]=(double) (inputFluxes* area*ModelsEngine.width_interpolate(widthFunction, t*3600, 0, 2));



			} else {
				Q_i1[t]= (double) (inputFluxes *area* (ModelsEngine.width_interpolate(widthFunction, t*3600, 0, 2) - ModelsEngine
						.width_interpolate(widthFunction, t*3600 - tpmax, 0, 2)));
			}

		}
		
		
		
        for( int t = tcorr_h; t < (tcorr_h + tpmax_h); t += 1 ) {
            
        	Q_i1[t] = (double) (inputFluxes * area*(widthFunction[widthFunction.length - 1][2] 
            		- ModelsEngine.width_interpolate(widthFunction, t*3600 - tpmax, 0, 2)));
        }
        

		// is the average discharge in m^3/s over 60 minutes 
		//double [] Q=computeMean(Q_i1);


		// sum of the different contributes of the discharge (previous time step and actual time step)
		// where the two time series overlap
		for( int t = 0; t <  Q_i1.length; t ++) {	
			Q_i[t]= Q_i[t]+Q_i1[t];
			//System.out.println(Q_i[t]);
		}

		
		return Q_i;


	}


	/**
	 * Compute the mean of the discharge computed each second,
	 * in order to pass from seconds to minutes and have a faster code.
	 *
	 * @param runoff is the runoff computed each second with the WFIUH
	 * @return the double[] vector of the average runoff in a minute
	 */

	/**
	public double [] computeMean (double [] runoff){

		int step=60;


		double [] sum=new double [(runoff.length/step)];

		for(int t=0; t<sum.length;t++){
			for (int i=t*step;i<step*(t+1)-1;i++){
				runoff[t*step]=runoff[t*step]+runoff[i+1];	
			}
			sum[t]=runoff[t*step]/step;
		}

		return sum;


	}
	 */
}
