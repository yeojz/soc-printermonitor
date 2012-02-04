/*
 * This contains the top level printer and a list of sub queues.
 * This class will contain a master list of jobQueue as well as it's sub queues.
 *
 * Contains main deviation from original ~hirman code.
 * Logically groups sub queues into this class group.
 *
 * In general,
 *  - this will contain master queue...
 *  - queueList contains -dx and -sx queues
 */



import java.util.*;
import java.io.*;


public class PrinterGroup extends Printer {


/*
 ***************************************
 *
 *	Variables
 *
 ***************************************
 */

	private List<Printer> queueList;
	private int totalCount = 0;


/*
 ***************************************
 *
 *	Constructor / Destructors
 *
 ***************************************
 */


	public PrinterGroup(String name) throws Exception {
		super(name);

		this.queueList = new ArrayList<Printer>();

		for (String printQueueName : PrintConfig.getPrintQueueNames(name)) {
			queueList.add( new Printer(printQueueName) );
		}

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
	 *	Updates the child printer list
	 ***************************************
	 */	
	public void addQueue(String name) throws Exception {
		queueList.add( new Printer(name) );
	}



	/*
	 ***************************************
	 *	Updates the job list for each printer
	 ***************************************
	 */

	public void update() throws Exception {

		totalCount = 0;

		for (Printer printer : queueList) {
			printer.update();
			totalCount += printer.getJobCount();
		}


	}



/*
 ***************************************
 *
 *	Other accessors
 *
 ***************************************
 */

	public int getTotal(){
		return totalCount;
	}



	public List<Printer> getQueue(){
		return queueList;
	}

}
