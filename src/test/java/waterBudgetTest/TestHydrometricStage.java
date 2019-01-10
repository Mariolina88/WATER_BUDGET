package waterBudgetTest;


import java.net.URISyntaxException;
import java.util.HashMap;

import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.junit.Test;

import hydrometricStage.HydrometricStage;



public class TestHydrometricStage{

	@Test
	public void testLinear() throws Exception {

		String startDate = "2018-12-20 11:00";
		String endDate = "2018-12-21 03:00";
		int timeStepMinutes = 60;
		String fId = "ID";
		

		String inPathToDischarge= "resources/Input/Idrogramma_4_Cavone.csv";
		String pathToStage= "resources/Output/stage/Stage.csv";
		
		OmsTimeSeriesIteratorReader QReader = getTimeseriesReader(inPathToDischarge, fId, startDate, endDate, timeStepMinutes);

		
		OmsTimeSeriesIteratorWriter writerStage = new OmsTimeSeriesIteratorWriter();



		writerStage.file = pathToStage;
		writerStage.tStart = startDate;
		writerStage.tTimestep = timeStepMinutes;
		writerStage.fileNovalue="-9999";


		HydrometricStage stage =new HydrometricStage();
	
		
		//(18.037*(C12+D9)^1.8306)*(1.2796*(C12+D9)+0.0769)

		while( QReader.doProcess ) {

			stage.a=9.141;
			stage.b=1.767;
			stage.c=0.4696;
			stage.d=-0.2949;
			stage.H0=-0.13;
			stage.model="FRC_VA";

		
			QReader.nextRecord();
			
			HashMap<Integer, double[]> id2ValueMap = QReader.outData;
			stage.inDischargeValues= id2ValueMap;
			


            stage.process();
            
            HashMap<Integer, double[]> outHMStorage = stage.outHMDischargeAndStage;

            
            
			writerStage.inData = outHMStorage ;
			writerStage.writeNextLine();
			
			if (pathToStage != null) {
				writerStage.close();
			}
			


			

		}

        QReader.close();


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