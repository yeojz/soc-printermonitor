/*
 *	PrintMonitor Modified
 *	Version 1.0
 *
 *	Based on original PrintMonitor by ~hirman
 *
 *
 *	This will be the main driver file for the application
 *	The PrintMonitor will load the config file and populate a list of printers.
 *	Will execute the lpq command and dumps the result into a list/stack of printer classes
 *
 */


import java.util.*;
import java.io.*;


public class PrintMonitor {



/*
 ***************************************
 *
 *	Variables
 *
 ***************************************
 */

	private static List<PrinterGroup> printers;


/*
 ***************************************
 *
 *	Print Monitor Main 
 *
 ***************************************
 */
	
	public static void main(String[] args) throws Exception {

		long now, startTime;
		now = startTime = System.currentTimeMillis();

		PrintConfig.load("config-siglabs.txt");
		int runMinutes = PrintConfig.getRunMinutes();
		int sleepTime = PrintConfig.getSleepTime();
		long endTime = startTime + runMinutes * 60000;


		printers = new ArrayList<PrinterGroup>();

		for (String printerName : PrintConfig.getPrinterNames()) {
			printers.add( new PrinterGroup(printerName) );
		}


		do {
			updatePrinters();
			writeOutputToFile();
			Thread.sleep(sleepTime);
			now = System.currentTimeMillis();
			//System.out.println(now);

		} while (now < endTime);


	}



/*
 ***************************************
 *
 *	Helpers
 *
 ***************************************
 */

	public static void updatePrinters() throws Exception {
		for (Printer printer : printers) {
			printer.update();
		}
	}


	/*
	 ***************************************
	 *	JSON formatter
	 * 	Takes current results, formats  
	 * 	in JSON style and returns
	 ***************************************
	 */
	public static String parseToJSON(int type) throws Exception{
		String jsonResult = "{";
		jsonResult += "\"printmonitor\" : [";



		/* Output foreach.

				{group: psts, total: 0, types: [
					{name: psts, count: 0, queue: [
						{id: 0, file: this, size: 0}
					]},
					{name: psts-dx, count: 0, queue: [
						{id: 0, file: this, size: 0}
					]},
				]}
		*/

		for (PrinterGroup printerName : printers) {
			jsonResult += "{";
			jsonResult += "\"group\": \"" + printerName.getName() + "\",";
			jsonResult += "\"total\": \"" + printerName.getTotal() + "\",";
			jsonResult += "\"types\" : [";

			//Sub Printers
			List<Printer> queueList = printerName.getQueue();
			for (Printer childP : queueList) {
				jsonResult += parseP(childP, type);
			}
			jsonResult = jsonResult.substring(0, jsonResult.length()-1);
			jsonResult += "]";
			jsonResult += "},";
		}
		jsonResult = jsonResult.substring(0, jsonResult.length()-1);

		jsonResult += "],";
		jsonResult += "\"time\" : \"" + System.currentTimeMillis() + "\"";
		jsonResult += "}";
		return jsonResult;
	}




	public static String parseP(Printer childP, int type){
		String jsonResult = "";
		
		jsonResult += "{ \n";		
		jsonResult += "\"pname\": \"" +  childP.getName() +"\" ,";	
		jsonResult += "\"count\": \"" + childP.getJobCount() +"\"";	

		if (type == 1){	
			jsonResult += ",";
			jsonResult += "\"queue\": [ ";	
			//Job Queue
			List<PrintJob> jobList = childP.getJobDetails();
			for (PrintJob jobQ : jobList) {
				jsonResult += "{";
				jsonResult += "\"id\": \"" + jobQ.getID() + "\",";
				jsonResult += "\"owner\": \"" + jobQ.getOwner() + "\",";
				jsonResult += "\"file\": \"" + jobQ.getFile() + "\",";
				jsonResult += "\"size\": \"" + jobQ.getSize() + "\"";
				jsonResult += "},";
			}

			jsonResult = jsonResult.substring(0, jsonResult.length()-1);
			jsonResult += "]";
		}

		
		if (type == 2){	
			jsonResult += ",";
			jsonResult += "\"queue\": [ ";	
			//Job Queue
			List<PrintJob> jobList = childP.getJobDetails();
			for (PrintJob jobQ : jobList) {
				jsonResult += "{";
				jsonResult += "\"id\": \"" + jobQ.getID() + "\",";
				jsonResult += "\"owner\": \"" + jobQ.getOwner() + "\",";
				jsonResult += "\"size\": \"" + jobQ.getSize() + "\"";
				jsonResult += "},";
			}

			jsonResult = jsonResult.substring(0, jsonResult.length()-1);
			jsonResult += "]";
		}		
		
		
		jsonResult += "},";
		
		return jsonResult;
	}






	/*
	 ***************************************
	 *	Output Writer
	 ***************************************
	 */
	public static void writeOutputToFile() throws Exception {
		PrintWriter summary = new PrintWriter( PrintConfig.getOutputFile() );
		//summary.print( parseToJSON(1) );
		//summary.close();

		//summary = new PrintWriter( PrintConfig.getOutputFileLimited() );
		//summary.print( parseToJSON(0) );
		//summary.close();

		//summary = new PrintWriter( PrintConfig.getOutputFileTest() );
		summary.print( parseToJSON(2) );
		summary.close();
	}
}
