/*
 * GNU GPL v3 License
 *
 * Copyright 2015 Marialaura Bancheri
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package rootZone;


import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Out;

import org.geotools.feature.SchemaException;


import java.io.IOException;

import org.apache.commons.math3.ode.*;


/**
 * The Class WaterBudget solves the water budget equation for the root zone layer.
 * 
 * @author Marialaura Bancheri
 */
public class WaterBudgetRootZone{


	@Description("Input rain Hashmap")
	@In
	public HashMap<Integer, double[]> inHMRain;	

	@Description("Input ET wet canopy Hashmap")
	@In
	public HashMap<Integer, double[]> inHMEwc;

	@Description("Input ET Hashmap")
	@In
	public HashMap<Integer, double[]> inHMETp;

	@Description("Input CI Hashmap")
	@In
	public HashMap<Integer, double[]>initialConditionS_i;

	@Description("The maximum storage capacity")
	@In
	public double pCmax;

	@Description("Coefficient of the non-reservoir")
	@In
	public double a;

	@Description("Exponential of non-linear reservoir")
	@In
	public double b;

	@Description("Degree of spatial variability of the soil moisture capacity")
	@In
	public Double pB_soil;
	
	@Description("Initial saturation_degree")
	@In
	public Double sat_degree=0.6;


	@Description("partitioning coefficient between the root zone and the runoff reservoirs")
	@Out
	public double alpha;


	@Description("Maximum value of the water storage, needed for the"
			+ "computation of the Actual EvapoTraspiration")
	@In
	@Out
	public double s_RootZoneMax;
	
	@Description("CI of the water storage")
	@In
	@Out
	public double s_RootZoneCI;



	@Description("ODE solver model: dp853, Eulero")
	@In
	public String solver_model;


	@Description("The area of the HRUs in km2")
	@In
	public double A;

	@Description("Time step")
	@In
	public double inTimestep;


	@Description("The HashMap with the Actual input of the layer ")
	@Out
	public HashMap<Integer, double[]> outHMActualInput= new HashMap<Integer, double[]>() ;

	@Description("The output HashMap with the Water Storage  ")
	@Out
	public HashMap<Integer, double[]> outHMStorage= new HashMap<Integer, double[]>() ;


	@Description("The output HashMap with the AET ")
	@Out
	public HashMap<Integer, double[]> outHMEvaporation = new HashMap<Integer, double[]>() ;


	@Description("The output HashMap with the outflow which drains to the lower layer")
	@Out
	public HashMap<Integer, double[]> outHMR= new HashMap<Integer, double[]>() ;

	@Description("The output HashMap with the quick outflow ")
	@Out
	public HashMap<Integer, double[]> outHMquick= new HashMap<Integer, double[]>() ;

	@Description("The output HashMap with the quick outflow in millimeter")
	@Out
	public HashMap<Integer, double[]> outHMquick_mm= new HashMap<Integer, double[]>() ;


	int step;

	double CI;





	/**
	 * Process: reading of the data, computation of the
	 * storage and outflows
	 *
	 * @throws Exception the exception
	 */
	@Execute
	public void process() throws Exception {


		// reading the ID of all the stations 
		Set<Entry<Integer, double[]>> entrySet = inHMRain.entrySet();




		// iterate over the station
		for( Entry<Integer, double[]> entry : entrySet ) {
			Integer ID = entry.getKey();

			if(step==0){
				System.out.println("RZ--a:"+a+"-b:"+b+"-Smax:"+s_RootZoneMax+"-pB_soil:"+pB_soil);

				if(initialConditionS_i!=null){
					CI=initialConditionS_i.get(ID)[0];	
					if (isNovalue(CI)) CI= s_RootZoneMax*sat_degree;	
					
				}else{
					CI=s_RootZoneMax*sat_degree;
				}
			}

			//System.out.println(ID);

			/**Input data reading*/
			double rain = inHMRain.get(ID)[0];
			if (isNovalue(rain)) rain= 0;
			if(step==0&rain==0)rain= 1;



			double alpha=(rain<0.001)?0:alpha(CI,rain,s_RootZoneMax);

			//System.out.println("alpha: "+ alpha);


			double actualInput=(1-alpha)*rain;

			double quick=alpha*rain/1000*A*Math.pow(10, 6)/(inTimestep*60);

			//System.out.println("RZmax:"+s_RootZoneMax );





			double ETp=0;
			if (inHMETp != null) ETp = inHMETp.get(ID)[0];
			if (isNovalue(ETp)) ETp= 0;

			double Ewc=0;
			if (inHMEwc != null) Ewc = inHMEwc.get(ID)[0];
			if (isNovalue(Ewc)) Ewc= 0;

			double ETpNet=ETp-Ewc;

			double waterStorage=computeS(actualInput,CI, ETpNet);

			double evapotranspiration=computeAET(waterStorage, ETpNet);

			double drainage=computeR(waterStorage);



			/** Save the result in  hashmaps for each station*/
			storeResult_series(ID,actualInput,waterStorage,evapotranspiration,drainage,quick,alpha*rain);

			//initialConditionS_i.put(ID,new double[]{waterStorage});

			CI=waterStorage;


		}


		step++;

	}

	/**
	 * Compute alpha according to the Hymod model
	 *
	 * @return the double value of alpha
	 */

	private double alpha( double S_i, double Pval, double S_max) {
		double pCmax=S_max *(pB_soil+1);
		double coeff1 = 1.0 - ((pB_soil + 1.0) * (S_i) / pCmax);
		double exp = 1.0 / (pB_soil + 1.0);
		double ct_prev = pCmax * (1.0 - Math.pow(coeff1, exp));
		double UT1 = Math.max((Pval - pCmax + ct_prev), 0.0);
		//Pval = Pval - UT1;
		double dummy = Math.min(((ct_prev + Pval- UT1) / pCmax), 1.0);
		double coeff2 = (1.0 - dummy);
		double exp2 = (pB_soil + 1.0);
		double xn = (pCmax / (pB_soil + 1.0)) * (1.0 - (Math.pow(coeff2, exp2)));
		double UT2 = Math.max(Pval- UT1 - (xn - S_i), 0);
		alpha=(UT1+UT2)/Pval;
		if (isNovalue(alpha)) alpha= 1;
		return alpha;



	}



	/**
	 * Compute the water storage
	 *
	 * @param actualInput: input fluxes  
	 * @param S_i is the initial condition of the storage
	 * @param ETp: input potential ET
	 * @return the water storage, according to the model and the layer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double computeS(double actualInput, double S_i, double ETp) throws IOException {


		/** Creation of the differential equation*/
		FirstOrderDifferentialEquations ode=new waterBudgetODE(actualInput,s_RootZoneMax,a,b,ETp);			

		/** Boundaries conditions*/
		double[] y = new double[] { S_i, s_RootZoneMax };

		/** Choice of the ODE solver */	
		SolverODE solver;
		solver=SimpleIntegratorFactory.createSolver(solver_model, 1, ode, y);

		/** result of the resolution of the ODE*/
		S_i=solver.integrateValues();


		/** Check of the Storage values: they cannot be negative*/
		S_i=(S_i<0)?0:S_i;

		//if(S_i<0.5)System.out.println("rootzone"+"-"+s_RootZoneMax+"-"+Pmax+"-"+b_rz);


		return S_i;
	}



	/**
	 * Compute the outflow toward the lower layer
	 *
	 * @param S_i: the actual storage value
	 * @return the double value of the outflow toward the lower layer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double computeR(double S_i) throws IOException {
		double Rg=a*Math.pow(S_i/s_RootZoneMax, b);
		return Rg;
	}


	/**
	 * Compute the AET
	 * @param S_i: the actual storage value
	 * @param ETp: the input potential ET
	 * @return the double value of the AET
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double computeAET(double S_i, double ETp) throws IOException {
		double AET=Math.max(0, (ETp*Math.min(1,1.33*S_i/s_RootZoneMax)));
		return AET;
	}





	private void storeResult_series(int ID, double actualInput, double waterStorage,
			double evapotranspiration,double drainage, double quick, double quick_mm) throws SchemaException {

		outHMActualInput.put(ID, new double[]{actualInput});
		outHMStorage.put(ID, new double[]{waterStorage});
		outHMEvaporation.put(ID, new double[]{evapotranspiration});
		outHMR.put(ID, new double[]{drainage});
		outHMquick.put(ID, new double[]{quick});
		outHMquick_mm.put(ID, new double[]{quick_mm});

	}


}