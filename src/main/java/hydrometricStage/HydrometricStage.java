package hydrometricStage;

import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;


import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Out;
import oms3.annotations.Unit;


import org.geotools.feature.SchemaException;




public class HydrometricStage {

	@Description("The Hashmap with the discharge values")
	@In
	@Unit ("m3/s")
	public HashMap<Integer, double[]> inDischargeValues;

	@Description("The double value with the hydrometric zero")
	@In
	@Unit ("m")
	public double H0=0;

	@Description("The double value of the coefficient")
	@In
	public double a;

	@Description("The double value of the coefficient")
	@In
	public double b;

	@Description("The double value of the second coefficient")
	@In
	public double c=0;

	@Description("The double value of the coefficient")
	@In
	public double d=0;

	@Description("String containing the name of the model: "
			+ " FRC_Qh: a*(h+H0)^b;"
			+ " FRC_VA: (a*(h+H0)^b)*(c*(h+H0)+d"
			+ " NO_FRC")
	@In
	public String model;


	@Description("the output hashmap with the discharge and the hydrometric discharge")
	@Out
	public HashMap<Integer, double[]> outHMDischargeAndStage= new HashMap<Integer, double[]>();

	Model modelFRC;


	/**
	 * Process.
	 *
	 * @throws Exception the exception
	 */
	@Execute
	public void process() throws Exception { 



		double stage;
		double [] result=new double [2];
		Integer [] resultID=new Integer [2];
		
		// reading the ID of all the stations 
		Set<Entry<Integer, double[]>> entrySet = inDischargeValues.entrySet();

		for (Entry<Integer, double[]> entry : entrySet) {
			Integer ID = entry.getKey();

			double discharge=inDischargeValues.get(ID)[0];
			if (isNovalue(discharge)) {
				stage = Double.NaN;
			}

			else{

				modelFRC=SimpleModelFactory.createModel(model,discharge,a,b,c,d,H0);
				stage=modelFRC.stageValue();	

			}

			result[0]=discharge;
			result[1]=stage;

			resultID[0]=ID;
			resultID[1]=10000+ID;

			/**Store results in Hashmaps*/
			storeResult(resultID,result);

		}


	}




	private void storeResult(Integer [] ID,double [] result) 
			throws SchemaException {

		for(int i=0;i<result.length;i++){
			outHMDischargeAndStage.put(ID[i], new double[]{result[i]});
		}

	}

}
