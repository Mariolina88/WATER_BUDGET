package waterBudgetTest;


import java.net.URISyntaxException;
import java.util.HashMap;

import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.junit.Test;

import damModelling.WaterBudgetDam;


public class TestDam{

	@Test
	public void testLinear() throws Exception {

		String startDate = "2014-04-18 00:00";
		String endDate = "2014-08-03 00:00";
		int timeStepMinutes = 60;
		String fId = "ID";

		String inPathToQin = "resources/Input/Idrogramma_input.csv";
		String inPathToErogazioni ="resources/Input/Erogazioni_ENEL.csv";

		String pathToH= "resources/Output/dam/H_2.csv";
		String pathToQ= "resources/Output/dam/Q_2.csv";
		String pathToA= "resources/Output/dam/A_2.csv";
		String pathToSfiori= "resources/Output/dam/Sfiori_2.csv";

		
		OmsTimeSeriesIteratorReader JReader = getTimeseriesReader(inPathToQin, fId, startDate, endDate, timeStepMinutes);

		OmsTimeSeriesIteratorWriter writerS = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerQ = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerA = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerSfiori = new OmsTimeSeriesIteratorWriter();

		writerS.file = pathToH;
		writerS.tStart = startDate;
		writerS.tTimestep = timeStepMinutes;
		writerS.fileNovalue="-9999";
				
		writerQ.file = pathToQ;
		writerQ.tStart = startDate;
		writerQ.tTimestep = timeStepMinutes;
		writerQ.fileNovalue="-9999";
		
		writerA.file = pathToA;
		writerA.tStart = startDate;
		writerA.tTimestep = timeStepMinutes;
		writerA.fileNovalue="-9999";
		
		writerSfiori.file = pathToSfiori;
		writerSfiori.tStart = startDate;
		writerSfiori.tTimestep = timeStepMinutes;
		writerSfiori.fileNovalue="-9999";
		
		WaterBudgetDam waterBudget= new WaterBudgetDam();


		while( JReader.doProcess ) {
		
			waterBudget.solver_model="dp853";
			waterBudget.a_surface=182285;
			waterBudget.b_surface=9E7;
			waterBudget.h_CI=530.38;
			waterBudget.h_sfioro=529.3;
			waterBudget.mu=0.48;
			waterBudget.l=20.5;
			waterBudget.inHMerogazioni=inPathToErogazioni;
            

			
			JReader.nextRecord();
			waterBudget.tCurrent=JReader.tCurrent;
			HashMap<Integer, double[]> id2ValueMap = JReader.outData;
			waterBudget.inHMRechargeValues = id2ValueMap;
			
			
			


            waterBudget.process();
            

            
            HashMap<Integer, double[]> outHMlevels = waterBudget.outHMLevel;
            
            HashMap<Integer, double[]> outHMQ= waterBudget.outHMDischarge;
            
            HashMap<Integer, double[]> outHMA= waterBudget.outHMSurface;
            
            HashMap<Integer, double[]> outHMSfiori= waterBudget.outHMSfiori;
            
			writerS.inData = outHMlevels ;
			writerS.writeNextLine();
			
			if (pathToH != null) {
				writerS.close();
			}
			
			writerSfiori.inData = outHMSfiori ;
			writerSfiori.writeNextLine();
			
			if (pathToSfiori != null) {
				writerSfiori.close();
			}
			
			writerQ.inData = outHMQ;
			writerQ.writeNextLine();
			
			if (pathToQ != null) {
				writerQ.close();
			}
			
			writerA.inData = outHMA;
			writerA.writeNextLine();
			
			if (pathToA != null) {
				writerA.close();
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