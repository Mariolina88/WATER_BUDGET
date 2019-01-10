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

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;

// TODO: Auto-generated Javadoc
/**
 * The Class FirstLayer implements the FirstOrderDifferentialEquations interface
 * and solves the water budget equation .
 * @author Marialaura Bancheri
 */
public class waterBudgetODE implements FirstOrderDifferentialEquations{

	public double s_CanopyMax;

	public double rain;

	public double ETp;
	
	public double S_i;
	


	/**
	 * Instantiates the first layer parameters .
	 *
	 * @param rain: precipitation value
	 * @param ETp: the modeled ET value
	 * @param throughfall: the modeled throughfall value
	 */
	public waterBudgetODE(double rain, double s_CanopyMax, double ETp, double S_i) {
		this.s_CanopyMax=s_CanopyMax;
		this.rain=rain;
		this.ETp=ETp;
		this.S_i=S_i;

	}
	
	/* (non-Javadoc)
	 * @see org.apache.commons.math3.ode.FirstOrderDifferentialEquations#getDimension()
	 */
	public int getDimension() {
		return 2;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.commons.math3.ode.FirstOrderDifferentialEquations#computeDerivatives(double, double[], double[])
	 */
	public void computeDerivatives(double t, double[] y, double[] yDot)
			throws MaxCountExceededException, DimensionMismatchException {

		
		
		yDot[0] =rain-Math.max(0, (y[0]-s_CanopyMax))- Math.max(0, (ETp*Math.min(1,(y[0])/s_CanopyMax)));
		

	}

}
