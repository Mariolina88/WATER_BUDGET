package waterBudgetTest;


import java.net.URISyntaxException;
import java.util.HashMap;

import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.junit.Test;


import rootZone.WaterBudgetRootZone;

public class TestRootZone{

	@Test
	public void testLinear() throws Exception {

		String startDate = "1994-01-01 00:00";
		String endDate = "1994-02-01 00:00";
		int timeStepMinutes = 60;
		String fId = "ID";

		String inPathToPrec ="resources/Input/rainfall.csv";
		String inPathToET ="resources/Input/ET.csv";
		String inPathToEwc ="resources/Input/ET.csv";
		String inPathToCI ="resources/Input/S_OUT_rz.csv";
		String pathToS=  "resources/Output/rootZone/S_OUT_rz.csv";
		String pathToET= "resources/Output/rootZone/ET_rz.csv";
		String pathToR= "resources/Output/rootZone/R_drain_rz.csv";

		
		OmsTimeSeriesIteratorReader JReader = getTimeseriesReader(inPathToPrec, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader CIReader = getTimeseriesReader(inPathToCI, fId, startDate, startDate, timeStepMinutes);

		OmsTimeSeriesIteratorReader EwcReader = getTimeseriesReader(inPathToEwc, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader ETReader = getTimeseriesReader(inPathToET, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorWriter writerS = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerET = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerR = new OmsTimeSeriesIteratorWriter();


		writerS.file = pathToS;
		writerS.tStart = startDate;
		writerS.tTimestep = timeStepMinutes;
		writerS.fileNovalue="-9999";

		
		writerET.file = pathToET;
		writerET.tStart = startDate;
		writerET.tTimestep = timeStepMinutes;
		writerET.fileNovalue="-9999";
	
		
		writerR.file = pathToR;
		writerR.tStart = startDate;
		writerR.tTimestep = timeStepMinutes;
		writerR.fileNovalue="-9999";
		

		
		WaterBudgetRootZone waterBudget= new WaterBudgetRootZone();


		while( JReader.doProcess ) {
		
			waterBudget.solver_model="dp853";

			
			waterBudget.s_RootZoneMax=4;
			waterBudget.a=0.000958;
			waterBudget.b=1.83;
			waterBudget.pB_soil=0.33;
			waterBudget.inTimestep=60;
			waterBudget.A=3.79;
			//waterBudget.sat_degree=0.8;
			

			
			JReader.nextRecord();
			
			HashMap<Integer, double[]> id2ValueMap = JReader.outData;
			waterBudget.inHMRain = id2ValueMap;
			
            /**CIReader.nextRecord();
            id2ValueMap = CIReader.outData;
            waterBudget.initialConditionS_i = id2ValueMap;*/
			

            
            ETReader.nextRecord();
            id2ValueMap = ETReader.outData;
            waterBudget.inHMETp = id2ValueMap;
            
            EwcReader.nextRecord();
            id2ValueMap = EwcReader.outData;
            waterBudget.inHMEwc = id2ValueMap;

            waterBudget.process();
            
            HashMap<Integer, double[]> outHMStorage = waterBudget.outHMStorage;
            HashMap<Integer, double[]> outHMET = waterBudget.outHMEvaporation;
            
            HashMap<Integer, double[]> outHMR = waterBudget.outHMR;
            
			writerS.inData = outHMStorage ;
			writerS.writeNextLine();
			
			if (pathToS != null) {
				writerS.close();
			}
			

			
			writerET.inData = outHMET;
			writerET.writeNextLine();
			
			if (pathToET != null) {
				writerET.close();
			}
			
			
			
			writerR.inData = outHMR;
			writerR.writeNextLine();
			
			if (pathToR != null) {
				writerR.close();
			}
            
		}
		JReader.close();
        //dischargeReader.close();
        ETReader.close();
        EwcReader.close();

	}


	private OmsTimeSeriesIteratorReader getTimeseriesReader( String inPath, String id, String startDate, String endDate,
			int timeStepMinutes ) throws URISyntaxException {
		OmsTimeSeriesIteratorReader reader = new OmsTimeSeriesIteratorReader();
		reader.file = inPath;
		reader.idfield = "ID";
		reader.tStart = startDate;
		reader.tTimestep = 60;
		reader.tEnd = endDate;
		reader.fileNovalue = "-9999";
		reader.initProcess();
		return reader;
	}
}