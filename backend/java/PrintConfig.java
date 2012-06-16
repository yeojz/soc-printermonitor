/*
 * This will load the configuration file.
 * 
 * Reorganized code from original ~hirman
 *
 *
 * In essence, 
 * 1st line: path to save file
 * 2nd line: start time
 * 3rd line: end time
 * 4th line onwards: 
 *  - first block: Printer Group + master list itself
 *  - Subsequent block: child printers
 *
*/

import java.util.*;
import java.io.*;

public class PrintConfig {


/*
 ***************************************
 *
 *	Variables
 *
 ***************************************
 */


	private static boolean isLoaded = false;
	private static List<String> printerNames;
	private static Map<String, List<String>> printQueueNames;
	private static String outputFile, outputFileLimited, outputFileTest;
	private static int runMinutes, sleepTime;




/*
 ***************************************
 *
 *	Constructor / Destructors / main
 *
 ***************************************
 */


	public static void main(String[] args) throws Exception {
		PrintConfig.load("config-siglabs.txt");
		for (String printerName : PrintConfig.getPrinterNames()) {

			System.out.println(printerName);
			for (String printQueueName : PrintConfig.getPrintQueueNames(printerName)) {
				System.out.format("\t%s\n", printQueueName);
			}

		}
	}




/*
 ***************************************
 *
 *	Loader
 *
 ***************************************
 */

	public static void load(String filename) throws Exception {
		printerNames = new ArrayList<String>();
		printQueueNames = new HashMap<String, List<String>>();
		
		String filepath = "/home/stuproj/siglabs/public_prog/printermonitor/"+filename;
		Scanner fileScanner = new Scanner(new File(filepath));		


		//General Options
		outputFile = fileScanner.nextLine().trim();
		//outputFileLimited = fileScanner.nextLine().trim();
		//outputFileTest = fileScanner.nextLine().trim();
		runMinutes = Integer.valueOf(fileScanner.nextLine().trim());
		sleepTime  = Integer.valueOf(fileScanner.nextLine().trim());
	

		//Printers	
		while (fileScanner.hasNextLine()) {
			String line = fileScanner.nextLine();

			Scanner lineScanner = new Scanner(line);
			if (!lineScanner.hasNext())
				continue;
			
			//Main Queue
			String printerName = lineScanner.next();
			printerNames.add(printerName);
			

			//Child Queues
			List<String> printQueueName = new ArrayList<String>();

			printQueueName.add(printerName);

			while (lineScanner.hasNext()) {
				printQueueName.add(lineScanner.next());
			}

			printQueueNames.put(printerName, printQueueName);
		}

		isLoaded = true;
	}





/*
 ***************************************
 *
 *	Accessors
 *
 ***************************************
 */

	/*
	 ***************************************
	 *	Return list of printers
	 ***************************************
	 */	
	public static List<String> getPrinterNames() throws Exception {
		if (!isLoaded) {
			throw new Exception("Config not loaded");
		}

		return printerNames;
	}



	public static List<String> getPrintQueueNames(String printerName) throws Exception {
		if (!isLoaded) {
			throw new Exception("Config not loaded");
		}

		if (!printQueueNames.containsKey(printerName)) {
			throw new Exception(String.format("Printer %s not found", printerName));
		}

		return printQueueNames.get(printerName);
	}



	/*
	 ***************************************
	 *	Private variable readers
	 ***************************************
	 */	
	public static String getOutputFile() {
		return outputFile;
	}

	public static String getOutputFileLimited() {
		return outputFileLimited;
	}

	public static String getOutputFileTest() {
		return outputFileTest;
	}

	public static int getRunMinutes() {
		return runMinutes;
	}

	public static int getSleepTime() {
		return sleepTime;
	}


}
