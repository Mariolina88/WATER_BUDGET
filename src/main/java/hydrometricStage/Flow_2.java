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
package hydrometricStage;

import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class Flow_2 implements Model{


	/** The a parameter of the model  */
	double a;

	/** The b parameter of the model */
	double b;

	/** The c parameter of the model */
	double c;

	/** The d parameter of the model */
	double d;

	/** The value of discharge */
	double discharge;

	/** The value of zero stage */
	double H0;
	
	Logger logger = LogManager.getLogger(Flow_2.class);


	public Flow_2(double discharge,double a, double b, double c, double d, double H0){

		this.a=a;
		this.b=b;
		this.c=c;
		this.d=d;
		this.discharge=discharge;
		this.H0=H0;
	}

	/**Flow2: Q=(a*(h+H0)^b)*(c*(h+H0)+d)*/
	@Override
	public double stageValue() {
		
		String log4jConfPath = "lib/log4j.properties";
		PropertyConfigurator.configure(log4jConfPath);

		UnivariateFunction f = new UnivariateFunction() {
			public double value(double x) {

				return (a*Math.pow(x+H0,b))*(c*(x+H0)+d)-discharge;
			}
		};

		double stage=H0;
		try{
			UnivariateSolver solver = new BrentSolver();
			stage = solver.solve(100, f, -H0, 20);

		} catch (Exception e) {
			
			logger.error(e);
			logger.info("Il solutore numerico non ha trovato una soluzione: h=H0");

		}

		return stage;
	}

}
