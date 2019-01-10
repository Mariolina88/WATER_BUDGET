/*
 * This file is part of JGrasstools (http://www.jgrasstools.org)
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * JGrasstools is free software: you can redistribute it and/or modify
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
package saturationDegree;

import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

import java.util.Set;

import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Out;


import org.geotools.coverage.grid.GridCoverage2D;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.jgrasstools.gears.utils.RegionMap;
import org.jgrasstools.gears.utils.coverage.CoverageUtilities;



@Description("Calculate the saturation degree as the ratio between the Storage and the"
		+ "Maximum Storage")
@Author(name = "Marialaura Bancheri")


public class SaturationDegree extends JGTModel {


	@Description("The actual storage of the reservoir")
	@In
	public HashMap<Integer, double[]> inHMStorage;	

	@Description("The maximum storage of the reservoir")
	@In
	public double Smax_saturation_degree;

	@Description("The raster file with the subbasins")
	@In
	public GridCoverage2D inSubbasins;
	
	
	@Description("The raster file with the saturation degree")
	@Out
	public GridCoverage2D outSaturationDataGrid;


	int step;
	WritableRaster SubbasinsMap;



	@Execute
	public void process() throws Exception {


			checkNull(inSubbasins);

			// transform the GrifCoverage2D maps into writable rasters
			SubbasinsMap=mapsReader(inSubbasins);


			// get the dimension of the maps
			RegionMap regionMap = CoverageUtilities.getRegionParamsFromGridCoverage(inSubbasins);
			int cols = regionMap.getCols();
			int rows = regionMap.getRows();


			// create the output maps with the right dimensions
			WritableRaster outWritableRaster= CoverageUtilities.createDoubleWritableRaster(cols, rows, null, null, null);
			WritableRandomIter iter = RandomIterFactory.createWritable(outWritableRaster, null);


			// reading the ID of all the stations 
			Set<Entry<Integer, double[]>> entrySet = inHMStorage.entrySet();


			// iterate over the station
			for( Entry<Integer, double[]> entry : entrySet ) {
				Integer ID = entry.getKey();

				//System.out.println(ID);

				/**Input data reading*/
				double storage = inHMStorage.get(ID)[0];
				if (isNovalue(storage)) storage= 0;

				// iterate over the entire domain and compute for each pixel the SWE
				for( int r = 1; r < rows - 1; r++ ) {
					for( int c = 1; c < cols - 1; c++ ) {


						int IDsub=(int) SubbasinsMap.getSampleDouble(c, r, 0);


						if(ID==IDsub){
							iter.setSample(c, r, 0, storage/Smax_saturation_degree);
						}else if(IDsub==-9999){
							iter.setSample(c, r, 0, IDsub);
						}else{}

						//System.out.println("");
					}
				}



			}


			CoverageUtilities.setNovalueBorder(outWritableRaster);
			outSaturationDataGrid = CoverageUtilities.buildCoverage("S", outWritableRaster, 
					regionMap, inSubbasins.getCoordinateReferenceSystem());
			step++;




	}




	/**
	 * Maps reader transform the GrifCoverage2D in to the writable raster and
	 * replace the -9999.0 value with no value.
	 *
	 * @param inValues: the input map values
	 * @return the writable raster of the given map
	 */
	private WritableRaster mapsReader ( GridCoverage2D inValues){	
		RenderedImage inValuesRenderedImage = inValues.getRenderedImage();
		WritableRaster inValuesWR = CoverageUtilities.replaceNovalue(inValuesRenderedImage, -9999);
		inValuesRenderedImage = null;
		return inValuesWR;
	}


}