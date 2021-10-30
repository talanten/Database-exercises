package dbs;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;


/*
 * The classes in this template are as follows:
 * 
 * SlottedPageExercise
 * This class (which is not to be changed, unless specified otherwise) encapsulates the whole exercise, it contains the main method, useful constants, and helper methods.
 * 
 * FakeBlockStorage
 * This class simulates a block storage of a DBS. 
 * It can store and retrieve pages in form of byte[]
 * 
 * TupleIdentifier
 * A simple tuple identifier consisting of a page ID and a page pointer index.
 * 
 * Order
 * The actual tuple data to be stored.
 * The only relevant method is getAsByteArray, which returns a given order as byte[]
 * 
 * 
 * Your task is to implement the SlottedPageExercise.storeOrder, SlottedPageExercise.getOrder, SlottedPageExercise.unusedBytes, and SlottedPageExercise.getSpaceUtilization methods.
 * 
 * To execute the code use a IDE or execute the file with 
 * 
 * `java SlottedPageExercise.java`
 * 
 * The orders_small.tbl has to be in the same directory
 */


/*
 * Our page layout is as follows: 
 *  - 5 (initially empty) pointer values of type int (4 bytes)  
 * Hence, the header needs 5*4 = 20 bytes
 * - The tuple data 
 *   - The tuple data starts with one int (4 bytes) storing the length of the tuple record 
 *   - And then the actual tuple byte data
 */


public class SlottedPageExercise {

	private FakeBlockStorage storage;
	int currentPage;

	/*
	 * Constants, use these in your code instead of literal numbers! 
	 */
	// The byte size of one "int" value in Java
	static final int INT_SIZE = 4;
	// The number of pointers in the header of a page
	static final int NUM_POINTERS = 5;
	// The byte size of a page header
	static final int HEADER_SIZE = INT_SIZE * NUM_POINTERS;

	public SlottedPageExercise() {
		storage = new FakeBlockStorage();
		currentPage = storage.initNewPage();
	}

	/*
	 * Utility methods
	 */

	/**
	 * Writes the content array into the page array, starting at the given offset.	  
	 * @param page The page to write to
	 * @param offset The pointer where the content will start to be written.
	 * @param content The content to be written to the page
	 */
	private void writeToPage(byte[] page, int offset, byte[] content) {
		java.lang.System.arraycopy(content, 0, page, offset, content.length);
	}

	/**
	 * Utility function that takes an order object, serializes it to a byte[] and write the
	 * byte[] at the position specified into the page's byte[]
	 * @param page the page content to be modified
	 * @param offset the offset within the page where the tuple's data should be put
	 * @param o the tuple that is to be written.
	 * @throws Exception
	 */
	private void writeTuple(byte[] page, int offset, Order o) throws Exception {
		byte[] orderBytes = o.getAsByteArray(); // Transform tuple to bytes
		// Get [TupleSize, <tuple>] array
		byte[] tupleBytes = ByteBuffer.allocate(orderBytes.length + INT_SIZE).putInt(orderBytes.length).put(orderBytes)
				.array();
		writeToPage(page, offset, tupleBytes); // Write tuple to page
	}

/**
     * Modifies the given page byte[] by writing a pointer value to the page at a given index
     * @param page The content of the page (byte[])
     * @param pointerIndex The index of the pointer (starting at 0)
     * @param pointer The pointer to be stored (an offset within the page byte[])
     * @throws Exception
     */
    private void writePointer(byte[] page, int pointerIndex, int pointer) throws Exception {
        byte[] pointerBytes = ByteBuffer.allocate(INT_SIZE).putInt(pointer).array();
        writeToPage(page, pointerIndex * INT_SIZE, pointerBytes); // Write pointer to page
    }

	
	/**
	 * Stores the given order object.
	 * @param o The object to be stored
	 * @return the TupleIdentifier pointing to the tuple 
	 * @throws Exception
	 */
	public TupleIdentifier storeOrder(Order o) throws Exception {
		//first of all we get the object in byte[] representation
		//this content we need to put into a free slot of a page that has 
		//enough free space to hold the tuple's content
		
		byte[] order = o.getAsByteArray();
		
		//we need to return a tuple identifier in the end, here we initialize it with null to make the file compile
		TupleIdentifier tid = null;
		
		///////////////////////////////////////////////
		//TODO - Solution code here
		// 1. Sequentially check all pages for available free slot and stop on 1st available page
		Set<Integer> allPgIds = storage.getAllPageIds();

		int pgId2Use = -1;

		for (int i : allPgIds) {
			if (order.length <= unusedBytes(i)) {
				pgId2Use = i;
				break;
			} else continue;
		}

		// 2. If no page available with free slot, then create new page
		if (pgId2Use < 0) {
			pgId2Use = storage.initNewPage();
		}

		// 3. Save order obj in given page
		??
		///////////////////////////////////////////////////
		//END OF TODO
		
		return tid;
	}

	/**
	 * Returns the Order object corresponding to the given TupleIdentifier.
	 * @param tid The tuple identifier
	 * @return the order object, NULL if the tuple was not found in the page
	 * @throws PageNotFoundException Thrown if the page was not found 
	 * @throws Exception Other exception cases
	 */
	public Order getOrder(TupleIdentifier tid) throws PageNotFoundException, Exception {
		byte[] p = storage.getPage(tid.getPage());
		
		//the object we want to return in the end, just initialized with NULL to make the file compile
		Order ret = null;
		//TODO - Solution code here
		ByteArrayInputStream s = new ByteArrayInputStream(p);
		DataInputStream ds = new DataInputStream(s);

		int slotNo = -1;
		int i = 0;
		// Find slot number of the tuple
		for (i = 0; i <= tid.getSlot(); i++) {
			slotNo = ds.readInt();
		}

		// Skip header 
		while (i < NUM_POINTERS) {
			ds.readInt();
		}

		// Skip bytes before needed tuple
		ds.readNBytes(slotNo-HEADER_SIZE);

		// Read the size of the tuple from 1st 4 bytes
		int tupleSize = ds.readInt();

		// Get tuple in bytes and convert to Order object
		byte[] tuple = ds.readNBytes(tupleSize);
		ret = new Order(tuple);

		//END TODO
		return ret;
	}

	/**
	 * Returns for a given page id the number of unused bytes in that page
	 * @param pageId the id of the page
	 * @return the number of bytes that are not used in this page
	 * @throws Exception
	 */
	public int unusedBytes(int pageId) throws Exception {
		byte[] page = storage.getPage(pageId);
		int usedBytes = HEADER_SIZE; // Header is always reserved
		
		//TODO - Solution code here
		usedBytes += page.length;
		
		//END OF TODO
		
		return storage.getPageSize() - usedBytes; // Free bytes = PageSize - usedBytes
	}
	
	
	/**
	 * Computes the fraction (between 0 and 1) of the space used vs. allocated.
	 * The larger the fraction is, the less bytes are "wasted".
	 * @return 
	 * @throws Exception
	 */
	public double getSpaceUtilization() throws Exception {
		Set<Integer> idSet = storage.getAllPageIds();
		
		//the object we want to return in the end (just initialized with 0 here to make the entire file compile)
		double ret=0; 
		
		//TODO - Solution code here
		
		
		//end TODO 

		return ret;
	}
	
	
	/**
	 * This is the main method that reads |-separated Order tuples from the provided file.
	 * It does a few simple checks to see if the to be implemented methods work correctly.
	 * @param args
	 */
	public static void main(String[] args) {
		try {

			SlottedPageExercise store = new SlottedPageExercise();
			
			
			
			BufferedReader reader = new BufferedReader(new FileReader("orders_small.tbl"));
			
			String line;

			TupleIdentifier tempTID = null;
			TupleIdentifier previousTID = null;
			Order previousOrder = null;

			
			//insert all tuples from the given file
			
			//after inserting a tuple, we try to load the previously inserted tuple and compare
			//it to the original order object that we created for the insertion
			int inserted = 0;
			int error = 0;
			while ((line = reader.readLine()) != null) {
				Order order = new Order(line);
				tempTID = store.storeOrder(order);
				inserted++;
				if (previousTID!=null) {
					Order whatWeGetNow = store.getOrder(previousTID);
					if (!previousOrder.equals(whatWeGetNow)) {
						//seems there is some bug, let's output some (perhaps) useful information
					
						System.out.println("wanted:\t"+previousOrder.toString());
						System.out.println("got:\t"+whatWeGetNow.toString());
						error++;
					}

				}
				previousTID = tempTID;
				previousOrder = order;
			}
			reader.close();
			System.out.println("Successfully imported/retrieved: " + inserted + " tuples.");
			if(error > 0){
				System.out.println("Failed to import/retrieve: " + error + " tuples.");
			}
			

			//tempTID should point now to the last inserted tuple, let's see
			//if this corresponds to the last tuple in the file we just read from

			boolean check = false;
			Order o = store.getOrder(tempTID);
			Order comp = new Order(
					"4000|69568|F|133466.83|1992-01-04|5-LOW|Clerk#000000339|0|le carefully closely even pinto beans. regular, ironic foxes against the|");
			if (o.equals(comp)) {
				System.out.println("Successfully checked example tuple");
				check = true;
			} else {
				System.out.println("Got: " + o.toString()
						+ ", Wanted: 4000|69568|F|133466.83|1992-01-04|5-LOW|Clerk#000000339|0|le carefully closely even pinto beans. regular, ironic foxes against the|");
			}

			//how large is the space utilization? 1 would mean no byte is left ununsed, 0.5 means that on average
			//each page is filled to 50%, etc...
			
			System.out.println("Space Utilization is: "+store.getSpaceUtilization());

			if (error == 0 && check){
				System.exit(0);
			}
			if (error != 0){
				System.exit(1);
			}
			System.exit(2);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * This class acts like a simple block storage. It can be used to retrieve a
	 * the contents of a page (aka. block) given the id of the page,
	 * to write the page content back given an id, and it can allocate new pages and returns their id.
	 * The content of a page is given as a byte[] of fixed size.
	 * The FakeBlockStorage does not know or care about the actual contents of the page, it just
	 * handles byte arrays.
	 * 
	 *
	 */
	
	private static class FakeBlockStorage {

		/**
		 * Page size in bytes
		 */
		private static int pageSize = 512;
		
		/**
		 * Internally used Map to store the mapping of page ids to byte arrays (i.e., pages).
		 */
		Map<Integer, byte[]> storage;

		/**
		 * Internal counter to keep track about the pages already created.
		 */
		int nextempty;


		/**
		 * Instantiates the FakeBlockStorage.
		 */
		public FakeBlockStorage() {
			//internally pages are kept in a HashHap, that maps page id to the page content 
			storage = new HashMap<Integer, byte[]>();
			//init the next page id to be used to zero
			nextempty = 0;
		}

		/**
		 * Returns the page size in bytes
		 * @return page size 
		 */
		public int getPageSize() {
			return pageSize;
		}

		/**
		 * Returns an array of the ids of pages that were allocated.
		 * @return 
		 */
		public Set<Integer> getAllPageIds() {
			return storage.keySet();
		}

		/**
		 * Creates a new page and returns its id.
		 * @return The id of the created page.
		 */
		public int initNewPage() {
			storage.put(nextempty, new byte[pageSize]);
			return nextempty++;
		}

		/**
		 * Given a page id, this method returns the corresponding byte array of the page content.
		 * If the page id is not known, a PageNotFoundException is thrown.
		 * @param id The id of the page to return
		 * @return The byte[] (page) that corresponds to the given page id
		 * @throws PageNotFoundException
		 */
		public byte[] getPage(int id) throws PageNotFoundException {
			if (!storage.containsKey(id)) {
				throw new PageNotFoundException("page " + id + " not known");
			}
			return storage.get(id);
		}

		/**
		 * Stores a page in the BlockStorage.
		 * @param id The id indicating the page to be overwritten by the given byte array.
		 * @param content The content of the page to be stored for the given id.
		 */
		public void storePage(int id, byte[] content) {
			storage.put(id, content);
		}

		/**
		 * Returns the number of total pages known. 
		 * @return number of total pages known.
		 */
		public int numberOfPagesUsed() {
			return storage.size();
		}

		/**
		 * Calculates and returns the number of bytes allocated in this storage.
		 * It is simply the number of pages known times the size of an individual page.
		 * @return The tocal number of bytes allocated.
		 */
		public int totalBytesAllocated() {
			return storage.size() * pageSize;
		}

	}

	/**
	 * As we have discussed in the lecture, a tuple identifier consists of 
	 *    - the id of the page the tuple is stored in 
	 *    - and the id of the slot within that page
	 * 
	 */
	private static class TupleIdentifier {

		private int page;
		private int slot;

		public TupleIdentifier(int page, int slot) {
			this.page = page;
			this.slot = slot;
		}

		public int getPage() {
			return page;
		}

		public void setPage(int page) {
			this.page = page;
		}

		public int getSlot() {
			return slot;
		}

		public void setSlot(int slot) {
			this.slot = slot;
		}

	}
	
	/**
	 * A simple Exception class that indicates that a requested page was not found in the BlockStorage
	 * 
	 */
	private static class PageNotFoundException extends Exception {

		public PageNotFoundException() {
			super();
		}

		public PageNotFoundException(String s) {
			super(s);
		}

	}
	
	/**
	 * Objects of class Order represent tuples from the TPC-H table orders 
	 *
	 */
	private static class Order {
		

		int o_orderkey;
		int o_custkey;
		char o_orderstatus;
		double o_totalprice;
		String o_orderdate;
		String o_orderpriority;
		String o_clerk;
		int o_shippingpriority;
		String o_comment;

		private Order() {

		};

		/**
		 * Constructur that uses a |-separted string to initialize the Order object.
		 * @param line A pipe (i.e., |) separated string containing a tuple of relation TPC-H table orders. 
		 *        the String is tokenized and the object initialized with the individual attribute values
		 */
		public Order(String line) {
			StringTokenizer tokenizer = new StringTokenizer(line, "|");
			int entry_id = 0;
			while (tokenizer.hasMoreTokens()) {
				String entry = tokenizer.nextToken();
				switch (entry_id) {

				case 0:
					this.o_orderkey = Integer.parseInt(entry);
					break;
				case 1:
					this.o_custkey = Integer.parseInt(entry);
					break;
				case 2:
					this.o_orderstatus = entry.charAt(0);
					break;
				case 3:
					this.o_totalprice = Double.parseDouble(entry);
					break;
				case 4:
					this.o_orderdate = entry;
					break;
				case 5:
					this.o_orderpriority = entry;
					break;
				case 6:
					this.o_clerk = entry;
					break;
				case 7:
					this.o_shippingpriority = Integer.parseInt(entry);
					break;
				case 8:
					this.o_comment = entry;
					break;
				default:
					throw new RuntimeException("something is wrong here");

				}

				entry_id++;
			}
		}

		/**
		 * This constructor takes a byte array that contains the attribute values of an Order object and 
		 * uses a DataInputStream to reconstruct the actual attribute value datatypes from the byte array.
		 * @param o A byte array containing the information of an Order object
		 * @throws IOException
		 */
		public Order(byte[] o) throws IOException {
			ByteArrayInputStream s = new ByteArrayInputStream(o);
			DataInputStream ds = new DataInputStream(s);
			this.o_orderkey = ds.readInt();
			this.o_custkey = ds.readInt();
			this.o_shippingpriority = ds.readInt();
			this.o_orderdate = ds.readUTF();
			this.o_orderpriority = ds.readUTF();
			this.o_clerk = ds.readUTF();
			this.o_comment = ds.readUTF();
			this.o_orderstatus = ds.readChar();
			this.o_totalprice = ds.readDouble();
		}

		/**
		 * This method uses a DataOutputStream to serialize the individual attribute values of this object into a byte array.
		 * @return a byte array containing the attribute values of this Order object in byte form
		 * @throws IOException
		 */
		public byte[] getAsByteArray() throws IOException {
			ByteArrayOutputStream s = new ByteArrayOutputStream();
			DataOutputStream ds = new DataOutputStream(s);
			ds.writeInt(this.o_orderkey);
			ds.writeInt(this.o_custkey);
			ds.writeInt(this.o_shippingpriority);
			ds.writeUTF(this.o_orderdate);
			ds.writeUTF(this.o_orderpriority);
			ds.writeUTF(this.o_clerk);
			ds.writeUTF(this.o_comment);
			ds.writeChar(this.o_orderstatus);
			ds.writeDouble(this.o_totalprice);
			return s.toByteArray();
		}
 

		/**
		 * Compares this object with a given Order object.
		 * @param o The object to compare this object to
		 * @return Returns true if the attribute values of both objects are equal, otherwise returns false.
		 */
		public boolean equals(Order o) {
			boolean intRes = o.o_orderkey == this.o_orderkey
					&& o.o_custkey == this.o_custkey && o.o_shippingpriority == this.o_shippingpriority;
			boolean stringRes = ((o.o_orderdate.compareTo(this.o_orderdate)==0)
					&& (o.o_orderpriority.compareTo(this.o_orderpriority)==0) && (o.o_clerk.compareTo(this.o_clerk)==0)
					&& (o.o_comment.compareTo(this.o_comment)==0));
			boolean charRes = o.o_orderstatus == this.o_orderstatus;
			boolean doubleRes = Math.abs(o.o_totalprice-this.o_totalprice) < 0.001d;
			return intRes && stringRes && charRes && doubleRes;
		}



		/**
		 * Returns a |-separated String representation of this object.
		 */
		public String toString() {
			return this.o_orderkey + "|" + this.o_custkey + "|" + this.o_orderstatus + "|" + this.o_totalprice + "|"
					+ this.o_orderdate + "|" + this.o_orderpriority + "|" + this.o_clerk + "|" + this.o_shippingpriority
					+ "|" + this.o_comment + "|";
		}

	}

	
}
