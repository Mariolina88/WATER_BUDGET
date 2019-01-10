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

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;

// TODO: Auto-generated Javadoc
/**
 * A simple factory pattern that create concrete integrators
 * for the resolution of the ODE: dp853 and Eulero. Inputs are:
 * the string type containing the chosen integrator, the 
 * integration time dt, the ODE and the boundary conditions
 * @author Marialaura Bancheri
 */
public class SimpleIntegratorFactory {
	
	/**
	 * Creates two integrator object.
	 *
	 * @param type: is the chosen integrator
	 * @param dt: is the integration time 
	 * @param ode: is the ODE equation
	 * @param y: is the vector with the boundary conditions
	 * @return the chosen solver 
	 */
	public static SolverODE createSolver(String type, double dt, 
			FirstOrderDifferentialEquations ode,double[] y){
		SolverODE solver=null;
		if (type.equals("dp853")){
			solver=new Dp853(dt,ode,y);
		} 

		if (type.equals("Eulero")){
			solver=new Eulero(dt,ode,y);
		}
		return solver;

	}

}
