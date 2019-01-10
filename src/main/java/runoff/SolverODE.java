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

// TODO: Auto-generated Javadoc
/**
 * The Interface SolverODE, for all the ode considered.
 * Just one method is considered. It returns the
 * values of variable S after the integration of the ODE
 * @author Marialaura Bancheri
 */
public interface SolverODE {

	/**
	 * Integrate values.
	 *
	 * @return the double value from the integration of the ODE
	 */
	abstract public double integrateValues ();


}
