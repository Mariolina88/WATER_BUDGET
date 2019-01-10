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

package canopy;

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
 * The component solves the budget for the outer part of the canopy layer.
 * Inputs are: the rain and the potential evapotranspiration
 * Outputs are: the storage and the throughfall.
 */


public class WaterBudgetCanopy{


	@Description("Input rain Hashmap")
	@In
	public HashMap<Integer, double[]> inHMRain;

	@Description("Input ETp Hashmap")
	@In
	public HashMap<Integer, double[]> inHMETp;

	@Description("ETp: Potential evaopotranspiration value for the given time considered")
	double ETp;

	@Description("Input CI Hashmap")
	@In
	public HashMap<Integer, double[]>initialConditionS_i;


	@Description("Leaf Area Index Hashmap")
	@In
	public  HashMap<Integer, double[]> inHMLAI;

	//double LAI=1;


	@Description("Canopy coefficient ")
	@In
	public double kc_canopy ;



	@Description("Partitioning coefficient free throughfall")
	@In
	public double p;



	@Description("ODE solver model:dp853, Eulero ")
	@In
	public String solver_model;

	@Description("The HashMap with the Actual input of the layer ")
	@Out
	public HashMap<Integer, double[]> outHMActualInput= new HashMap<Integer, double[]>() ;

	@Description("The HashMap with the Actual output of the layer ")
	@Out
	public HashMap<Integer, double[]> outHMActualOutput= new HashMap<Integer, double[]>() ;

	@Description("The output HashMap with the Water Storage  ")
	@Out
	public HashMap<Integer, double[]> outHMStorage= new HashMap<Integer, double[]>() ;

	@Description("The output HashMap with the Throughfall ")
	@Out
	public HashMap<Integer, double[]> outHMThroughfall= new HashMap<Integer, double[]>() ;


	@Description("The output HashMap with the AET ")
	@Out
	public  HashMap<Integer, double[]> outHMAET= new HashMap<Integer, double[]>() ;

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
		//checkNull(inHMRain);



		// reading the ID of all the stations 
		Set<Entry<Integer, double[]>> entrySet = inHMRain.entrySet();



		// iterate over the station
		for( Entry<Integer, double[]> entry : entrySet ) {
			Integer ID = entry.getKey();

			/**Input data reading*/
			double rain = inHMRain.get(ID)[0];
			if (isNovalue(rain)) rain= 0;
			if(step==0&rain==0)rain= 1;



			double LAI= inHMLAI.get(ID)[0];
			if (isNovalue(LAI))	LAI=0.6;		
			LAI=(LAI==0)?0.6:LAI;


			if(step==0){
				System.out.println("C--kc:"+kc_canopy);

				if(initialConditionS_i!=null){
					CI=initialConditionS_i.get(ID)[0];
					if (isNovalue(CI)) CI= kc_canopy*LAI/2;
				}else{
					CI=kc_canopy*LAI/2;
				}
			}

			ETp=0;
			if (inHMETp != null) ETp = inHMETp.get(ID)[0];
			if (isNovalue(ETp)) ETp= 0;


			double waterStorage=computeS((1-p)*rain,CI,LAI);
			double actualInput=(1-p)*rain;
			double actualOutput=computeThroughfall((1-p)*rain,waterStorage,LAI);
			double throughfall=actualOutput+p*rain;			
			double AET=computeAET(waterStorage,LAI);


			
			/** Save the result in  hashmaps for each station*/
			storeResult_series(ID,waterStorage,throughfall, AET, actualInput,actualOutput);

			CI=waterStorage;

		}


		step++;

	}

	/**
	 * Compute the water storage
	 *
	 * @param rain: input rain 
	 * @param S_i : initial condition
	 * @param LAI: leaf area index
	 * @return the water storage, according to the model and the layer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double computeS(double rain, double S_i, double LAI) throws IOException {

		double s_CanopyMax=kc_canopy*LAI;
		

		/** Creation of the differential equation*/
		FirstOrderDifferentialEquations ode=new waterBudgetODE(rain,s_CanopyMax, ETp, S_i);			


		/** Boundaries conditions*/
		double[] y = new double[] {  S_i, s_CanopyMax };



		/** Choice of the ODE solver */	
		SolverODE solver;
		solver=SimpleIntegratorFactory.createSolver(solver_model, 1, ode, y);

		/** result of the resolution of the ODE*/
		S_i=(S_i<0.1&rain==0)?0:solver.integrateValues();

		S_i=(S_i<0)?0:S_i;
		//if (S_i<0.1)System.out.println("canopy"+S_i);

		return S_i;
	}


	/**
	 * Compute the ET
	 *
	 * @param S_i : initial condition
	 * @param LAI: leaf area index
	 * @throws SchemaException the schema exception
	 */

	public double computeAET(double S_i, double LAI) throws IOException {
		//(Brisson et al., 1998):
		double s_CanopyMax=kc_canopy*LAI;


		double AET=Math.max(0, (ETp*Math.min(1,(S_i)/s_CanopyMax)));	
		return AET;
	}

	/**
	 * Compute the Throughfall
	 *
	 * @param S_i : initial condition
	 * @param LAI: leaf area index
	 * @throws SchemaException the schema exception
	 */

	public double computeThroughfall(double rain,double S_i, double LAI) throws IOException {
		//(Brisson et al., 1998):
		double s_CanopyMax=kc_canopy*LAI;

		double throughfall=Math.max(0, S_i-s_CanopyMax);

		throughfall=(s_CanopyMax==0)?rain:throughfall;

		return throughfall;
	}


	/**
	 * Store of the results in hashmaps 
	 *
	 * @param waterStorage is the water storage
	 * @param throughfall is the throughfall
	 * @throws SchemaException the schema exception
	 */

	private void storeResult_series(int ID, double waterStorage,double throughfall, double AET, 
			double actualInput, double actualOutput) throws SchemaException {

		outHMStorage.put(ID, new double[]{waterStorage});
		outHMThroughfall.put(ID, new double[]{throughfall});
		outHMAET.put(ID, new double[]{AET});
		outHMActualInput.put(ID, new double[]{actualInput});
		outHMActualOutput.put(ID, new double[]{actualOutput});

	}


}