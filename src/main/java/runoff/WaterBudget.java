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

package runoff;

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
 * The Class WaterBudget solves the water budget equation for the groudwater layer.
 * The input s the recharge from the root zone and the output is the discharge, 
 * modeled with a non linear reservoir model.
 * 
 * @author Marialaura Bancheri
 */
public class WaterBudget{


	@Description("Input recharge Hashmap")
	@In
	public HashMap<Integer, double[]> inHMRechargeValues;

	@Description("Input CI Hashmap")
	@In
	public HashMap<Integer, double[]>initialConditionS_i;


	@Description("Time Step simulation")
	@In
	public int timeStep;


	@Description("Coefficient of the non-linear Reservoir model ")
	@In
	public double c ;


	@Description("Exponent of non-linear reservoir")
	@In
	public double d;

	@Description("The area of the HRUs in km2")
	@In
	public double A;

	@Description("Smax")
	@In
	public double s_RootZoneMax=10;


	@Description("ODE solver model: dp853, Eulero ")
	@In
	public String solver_model;

	
	@Description("The output HashMap with the Water Storage")
	@Out
	public HashMap<Integer, double[]> outHMStorage= new HashMap<Integer, double[]>() ;

	@Description("The output HashMap with the discharge")
	@Out
	public HashMap<Integer, double[]> outHMDischarge= new HashMap<Integer, double[]>() ;

	@Description("The output HashMap with the discharge in mm")
	@Out
	public HashMap<Integer, double[]> outHMDischarge_mm= new HashMap<Integer, double[]>() ;

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
		//checkNull(inHMRechargeValues);


		// reading the ID of all the stations 
		Set<Entry<Integer, double[]>> entrySet = inHMRechargeValues.entrySet();



		// iterate over the station
		for( Entry<Integer, double[]> entry : entrySet ) {
			Integer ID = entry.getKey();

			if(step==0){
				System.out.println("RO--c:"+c+"-d:"+d+"-s_GroundWaterMax:"+s_RootZoneMax);

				if(initialConditionS_i!=null){
					CI=initialConditionS_i.get(ID)[0];
					if (isNovalue(CI)) CI= 0;					
				}else{
					CI=0;
				}
			}

			/**Input data reading*/
			double recharge = inHMRechargeValues.get(ID)[0];
			if (isNovalue(recharge)) recharge= 0;
			if(step==0&recharge==0)recharge= 1;


			double waterStorage=computeS(recharge,CI,c);
			double discharge_mm=computeQ(waterStorage,c);

			double discharge=discharge_mm/1000*A*Math.pow(10, 6)/(60*timeStep);		

			/** Save the result in  hashmaps for each station*/
			storeResult_series(ID,waterStorage,discharge,discharge_mm);

			CI=waterStorage;

		}


		step++;

	}

	/**
	 * Compute the water storage
	 *
	 * @param totalInputFluxes: input total input fluxes
	 * @param the initial condition of the storage
	 * @return the water storage, according to the model and the layer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double computeS(double recharge, double S_i,double c) throws IOException {


		
		/** Creation of the differential equation*/
		FirstOrderDifferentialEquations ode=new waterBudgetODE(recharge,c,d,s_RootZoneMax);			

		/** Boundaries conditions*/
		double[] y = new double[] { S_i, s_RootZoneMax };

		/** Choice of the ODE solver */	
		SolverODE solver;
		solver=SimpleIntegratorFactory.createSolver(solver_model, 1, ode, y);

		/** result of the resolution of the ODE*/
		S_i=solver.integrateValues();

		/** Check of the Storage values: they cannot be negative*/
		if (S_i<0) S_i=0;

		//System.out.println("ro"+c+"-"+d+"-"+s_GroundWaterMax);


		return S_i;
	}


	/**
	 *
	 * @param S_i: the actual storage 
	 * @return the double value of the simulated discharge
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double computeQ(double S_i, double tau_ro) throws IOException {
		double Q=c*Math.pow(S_i/s_RootZoneMax,d);
		return Q;
	}




	/**
	 * Store of the results in hashmaps 
	 *
	 * @param waterStorage is the water storage
	 * @param discharge is the discharge
	 * @param discharge is the discharge in mm
	 * @throws SchemaException the schema exception
	 */

	private void storeResult_series(int ID, double waterStorage,double discharge, double discharge_mm)
			throws SchemaException {

		outHMStorage.put(ID, new double[]{waterStorage});
		outHMDischarge.put(ID, new double[]{discharge});
		outHMDischarge_mm.put(ID, new double[]{discharge_mm});


	}


}