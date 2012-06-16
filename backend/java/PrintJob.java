/*
 * An object containing details of each printjob.
 */

import java.util.*;
import java.io.*;

public class PrintJob {


/*
 ***************************************
 *
 *	Variables
 *
 ***************************************
 */

	private int ID;
	private String owner;
	private String file;
	private int bytes;



/*
 ***************************************
 *
 *	Constructor / Destructors
 *
 ***************************************
 */


	public PrintJob(int ID, String owner, String file, int bytes) {
		this.ID = ID;
		this.owner = owner;
		this.file = file;
		this.bytes = bytes;
	}


/*
 ***************************************
 *
 *	Accessors to private variables
 *
 ***************************************
 */

	public String getFile(){
		return file;
	}
	
	public int getSize(){
		return bytes;
	}

	public int getID(){
		return ID;
	}

	public String getOwner(){
		return owner;
	}




}
