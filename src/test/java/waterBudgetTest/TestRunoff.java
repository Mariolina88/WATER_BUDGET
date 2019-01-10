package waterBudgetTest;


import java.net.URISyntaxException;
import java.util.HashMap;

import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.junit.Test;

import runoff.WaterBudget;


public class TestRunoff{

	@Test
	public void testLinear() throws Exception {

		String startDate = "1994-01-01 21:00";
		String endDate = "1994-01-04 08:00";
		int timeStepMinutes = 60;
		String fId = "ID";

		String inPathToPrec = "resources/Input/rainfall.csv";
		//String inPathToCI ="resources/Input/S_gw.csv";

		String pathToS= "resources/Output/runoff/S_ro.csv";
		String pathToR= "resources/Output/runoff/Q_ro.csv";

		
		OmsTimeSeriesIteratorReader JReader = getTimeseriesReader(inPathToPrec, fId, startDate, endDate, timeStepMinutes);
		//OmsTimeSeriesIteratorReader CIReader = getTimeseriesReader(inPathToCI, fId, startDate, startDate, timeStepMinutes);

		OmsTimeSeriesIteratorWriter writerS = new OmsTimeSeriesIteratorWriter();

		OmsTimeSeriesIteratorWriter writerQ = new OmsTimeSeriesIteratorWriter();


		writerS.file = pathToS;
		writerS.tStart = startDate;
		writerS.tTimestep = timeStepMinutes;
		writerS.fileNovalue="-9999";
		

		
		writerQ.file = pathToR;
		writerQ.tStart = startDate;
		writerQ.tTimestep = timeStepMinutes;
		writerQ.fileNovalue="-9999";
		

		
		WaterBudget waterBudget= new WaterBudget();


		while( JReader.doProcess ) {
		
			waterBudget.solver_model="dp853";
			waterBudget.c=0.39;
			waterBudget.d=1;
			waterBudget.timeStep=60;
			waterBudget.A=5.2092;
			waterBudget.s_RootZoneMax=1;
			

			
			JReader.nextRecord();
			
			HashMap<Integer, double[]> id2ValueMap = JReader.outData;
			waterBudget.inHMRechargeValues = id2ValueMap;
			
            /**CIReader.nextRecord();
            id2ValueMap = CIReader.outData;
            waterBudget.initialConditionS_i = id2ValueMap;*/
			


            waterBudget.process();
            
            HashMap<Integer, double[]> outHMStorage = waterBudget.outHMStorage;
            
            HashMap<Integer, double[]> outHMQ= waterBudget.outHMDischarge;
            
			writerS.inData = outHMStorage ;
			writerS.writeNextLine();
			
			if (pathToS != null) {
				writerS.close();
			}
			
		
			writerQ.inData = outHMQ;
			writerQ.writeNextLine();
			
			if (pathToR != null) {
				writerQ.close();
			}
            
		}
		JReader.close();


	}


	private OmsTimeSeriesIteratorReader getTimeseriesReader( String inPath, String id, String startDate, String endDate,
			int timeStepMinutes ) throws URISyntaxException {
		OmsTimeSeriesIteratorReader reader = new OmsTimeSeriesIteratorReader();
		reader.file = inPath;
		reader.idfield = "ID";
		reader.tStart = startDate;
		reader.tTimestep = timeStepMinutes;
		reader.tEnd = endDate;
		reader.fileNovalue = "-9999";
		reader.initProcess();
		return reader;
	}
}