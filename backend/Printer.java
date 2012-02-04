/*
 * Defines details of each Printers
 *
 * Contains main deviation from original ~hirman code.
 *
 * This class will contain a master list of jobQueue as well as child printers.
 * In general, 3 lists will be created: normal, dx, sx.
 */

import java.util.*;
import java.io.*;


public class Printer {


/*
 ***************************************
 *
 *	Variables
 *
 ***************************************
 */
	
	//standard
	private String name;
	private List<PrintJob> jobQueue;
	
	//misc
	private String status;


/*
 ***************************************
 *
 *	Constructor / Destructors
 *
 ***************************************
 */


	public Printer(String name) throws Exception {
		this.name = name;
	}




/*
 ***************************************
 *
 *	Populate and other mojos.
 *
 ***************************************
 */
	

	/*
	 ***************************************
	 *	Updates the job list for printer
	 ***************************************
	 */

	public void update() throws Exception {

		//refresh queue
		this.jobQueue = new ArrayList<PrintJob>();


		//command execute and dump into a string
		String command = String.format("/usr/local/bin/lpq -P%s", this.name);
		Process process = Runtime.getRuntime().exec(command);

		//start processing command dump
		int rankLine = -1;
		int lineNumber = 0;
		String line;
		List<String> lines = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));


		/* 
		 *	Eg: 
		 *	---------------------------------------------------------------------------   
		 *	Rank   Owner      Job  Files                                 Total Size  
		 *	active u0900000   1    Microsoft Word - Document1            847 bytes
		 *	1st    u0900000   2    Microsoft Word - Document1            847 bytes
		 */

		//pushing each line into arraylist
		while ( (line = reader.readLine()) != null ) {
			line = line.trim();

			if (line.equals("")) {
				continue;
			}

			if (line.startsWith("Rank")) {
				rankLine = lineNumber;
			}

			lines.add(line);
			lineNumber++;
		}

		//no output
		if (lines.size() == 0) {
			return;
		}

		//no queue (type 1)
		if (rankLine == -1) {
			return;
		}

		//no queue (type 2)
		if (lines.get(0).equals("no entries") || lines.get(lines.size()-1).equals("no entries")) {
			return;
		}


		//no idea what this does...
		if ( lines.get(0).startsWith("Owner") ) {
			try {
				status = lines.get(1).substring(37,54);
			} catch (Exception e) {
				System.err.println("lines:");
				for (String l : lines) {
					System.err.println(l);
				}
				throw e;
			}
		}


		//parsing each line and inserting into a list of jobs
		for (int i = rankLine+1; i < lines.size(); i++) {
			line = lines.get(i);
			String[] words = line.split("\\s+");

			String owner = words[1];
			int ID = Integer.valueOf(words[2]);
			String file = words[3];

			for (int j = 4; j < words.length - 2; j++) {
				file += " " + words[j];
			}

			int bytes = Integer.valueOf(words[words.length-2]);

			jobQueue.add( new PrintJob(ID, owner, file, bytes) );
		}


	}





/*
 ***************************************
 *
 *	Accessors to private variables
 *
 ***************************************
 */


	public String getName(){
		return name;
	}

	public int getJobCount(){
		return jobQueue.size();
	}


	public List<PrintJob> getJobDetails(){
		return jobQueue;
	}

}
