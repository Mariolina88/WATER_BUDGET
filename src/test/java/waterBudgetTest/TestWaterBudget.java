package waterBudgetTest;


import java.net.URISyntaxException;
import java.util.HashMap;

import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.jgrasstools.gears.libs.monitor.PrintStreamProgressMonitor;
import org.junit.Test;


import waterBudget.WaterBudget;

public class TestWaterBudget{

	@Test
	public void testLinear() throws Exception {

		String startDate = "1994-01-01 00:00";
		String endDate = "1994-01-02 00:00";
		int timeStepMinutes = 60;
		String fId = "ID";

		PrintStreamProgressMonitor pm = new PrintStreamProgressMonitor(System.out, System.out);

		String inPathToPrec = "resources/Input/rainfall.csv";
		String inPathToET ="resources/Input/ET.csv";
		String pathToS= "resources/Output/waterBudget/S.csv";
		String pathToQ= "resources/Output/waterBudget/Q.csv";
		String pathToET= "resources/Output/waterBudget/ET.csv";
		String pathToQuick= "resources/Output/waterBudget/Quick.csv";
		String pathToR= "resources/Output/waterBudget/R_drain.csv";

		
		OmsTimeSeriesIteratorReader JReader = getTimeseriesReader(inPathToPrec, fId, startDate, endDate, timeStepMinutes);
		//OmsTimeSeriesIteratorReader dischargeReader = getTimeseriesReader(inPathToDischarge, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader ETReader = getTimeseriesReader(inPathToET, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorWriter writerS = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerQ = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerET = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerQuick = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerR = new OmsTimeSeriesIteratorWriter();


		writerS.file = pathToS;
		writerS.tStart = startDate;
		writerS.tTimestep = timeStepMinutes;
		writerS.fileNovalue="-9999";
		
		writerQ.file = pathToQ;
		writerQ.tStart = startDate;
		writerQ.tTimestep = timeStepMinutes;
		writerQ.fileNovalue="-9999";
		
		writerET.file = pathToET;
		writerET.tStart = startDate;
		writerET.tTimestep = timeStepMinutes;
		writerET.fileNovalue="-9999";
		
		writerQuick.file = pathToQuick;
		writerQuick.tStart = startDate;
		writerQuick.tTimestep = timeStepMinutes;
		writerQuick.fileNovalue="-9999";
		
		writerR.file = pathToR;
		writerR.tStart = startDate;
		writerR.tTimestep = timeStepMinutes;
		writerR.fileNovalue="-9999";
		

		
		WaterBudget waterBudget= new WaterBudget();


		while( JReader.doProcess ) {
		
			waterBudget.solver_model="dp853";
			waterBudget.Q_model="NonLinearReservoir";
			waterBudget.ET_model="AET";
			waterBudget.A=115.4708483;
			waterBudget.a=752.3543670;
			waterBudget.b=1.75744;
			waterBudget.s_max=0.005704;
			waterBudget.Re=0.2;
			waterBudget.nZ=1;
			
			JReader.nextRecord();
			
			HashMap<Integer, double[]> id2ValueMap = JReader.outData;
			waterBudget.inRainValues = id2ValueMap;
			

            
            ETReader.nextRecord();
            id2ValueMap = ETReader.outData;
            waterBudget.inETvalues = id2ValueMap;

            waterBudget.pm = pm;
            waterBudget.process();
            
            HashMap<Integer, double[]> outHMStorage = waterBudget.outHMStorage;
            HashMap<Integer, double[]> outHMDischarge = waterBudget.outHMDischarge;
            HashMap<Integer, double[]> outHMET = waterBudget.outHMEvapotranspiration;
            HashMap<Integer, double[]> outHMQuick = waterBudget.outHMQuick;
            
            HashMap<Integer, double[]> outHMR = waterBudget.outHMR;
            
			writerS.inData = outHMStorage ;
			writerS.writeNextLine();
			
			if (pathToS != null) {
				writerS.close();
			}
			
			writerQ.inData = outHMDischarge;
			writerQ.writeNextLine();
			
			if (pathToQ != null) {
				writerQ.close();
			}
			
			writerET.inData = outHMET;
			writerET.writeNextLine();
			
			if (pathToET != null) {
				writerET.close();
			}
			
			writerQuick.inData = outHMQuick;
			writerQuick.writeNextLine();
			
			if (pathToQuick != null) {
				writerQuick.close();
			}
			
			writerR.inData = outHMR;
			writerR.writeNextLine();
			
			if (pathToR != null) {
				writerR.close();
			}
            
		}
		JReader.close();
        //dischargeReader.close();
        //ETReader.close();

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