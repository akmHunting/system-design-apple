package main;

import java.io.*;
import java.util.*;

public class SystemDesign {

	public final static String CMD_INSTALL = "INSTALL";
	public final static String CMD_REMOVE = "REMOVE";
	public final static String CMD_LIST = "LIST";
	public final static String CMD_END = "END";
	public final static String CMD_DEPEND = "DEPEND";

	protected HashMap<String, Item> itemDependencyMap;
	protected HashMap<String, DependencyGraphManager> installDependencyManifest;

	public SystemDesign() {
		itemDependencyMap = new HashMap<>();
		installDependencyManifest = new HashMap<>();
	}

	private boolean isValidLine(String line) {
		if (line == null) return false;
		return 		(line.startsWith(CMD_END) 
				|| 	line.startsWith(CMD_DEPEND) 
				|| 	line.startsWith(CMD_INSTALL)
				|| 	line.startsWith(CMD_REMOVE) 
				|| 	line.startsWith(CMD_LIST));
	}
	
	private boolean isStopLine(String line) {
		return line != null && line.startsWith(CMD_END);
	}

	private void printConsole(String str) {
		System.out.print(str);
	}
	private void processLine(String line) {
		if (line == null || line.length() == 0)	return;

		if (isStopLine(line)) return;

		if (!isValidLine(line)) return;

		if (line.startsWith((CMD_DEPEND))) {
			processDependencyLine(line);
		} else if (line.startsWith(CMD_INSTALL)) {
			processInstallationLine(line, "commandline");
		} else if (line.startsWith(CMD_LIST)) {
			processListOfItemLine(line);
		} else if (line.startsWith(CMD_REMOVE)) {
			processRemoveLine(line);
		}

	}

	private int processDependencyItem(String itemName) {

		if (itemName == null || itemName.length() == 0) {
			return -1;
		}
		if (!itemDependencyMap.containsKey(itemName)) {
			Item item = new Item(itemName);
			itemDependencyMap.put(itemName, item);
		}

		printConsole("DEPEND " + itemName + " ");
		return 0;
	}

	
	/**
	 * Dependency Line Processor
	 * @param line
	 * @return
	 */

	private int processDependencyLine(String line) {

		StringTokenizer st = new StringTokenizer(line);
		st.nextElement(); // EAT DEPEND :)
		String itemName = (String) st.nextElement();

		if (itemName == null || itemName.length() == 0) return -1;
		if (processDependencyItem(itemName) < 0) return -1;
		
		while (st.hasMoreElements()) {
			String dep = (String) st.nextElement();
			int ret;
			ret = processDependency(itemName, dep);

			if (ret == -1)	return -1;
		}

		printConsole("\n");
		return 0;
	}
	
	private int processDependency(String itemName, String dependentItemName) {

		if (!itemDependencyMap.containsKey(itemName)) return -1;

		Item itemRoot = itemDependencyMap.get(itemName);

		if (itemRoot == null) return -1;

		Item dependentItemEntry = itemDependencyMap.get(dependentItemName);

		if (findDescendent(dependentItemName, itemName)) {
			printConsole(" depends on " + dependentItemName + " ignoring command.\n");
			return -1;
		}
		if (dependentItemEntry != null) {
			itemRoot.dependencyList.add(dependentItemEntry);
		} else {
			dependentItemEntry = new Item(dependentItemName);
			itemDependencyMap.put(dependentItemName, dependentItemEntry);
			itemRoot.dependencyList.add(dependentItemEntry);
		}
		printConsole(dependentItemName);
		printConsole(" ");
		return 0;
	}
	
	private boolean findDescendent(String rootItem, String target) {
		if (!itemDependencyMap.containsKey((rootItem))) {
			return false;
		}

		Stack<Item> stack = new Stack<Item>();
		Item item = itemDependencyMap.get(rootItem);
		stack.push(item);
		while (stack.size() > 0) {
			Item it = (Item) stack.pop();
			if (it.itemName.equals(target)) 
				return true;
			
			for (Item d : it.dependencyList) 
				stack.push(d);
		}
		return false;
	}

	

	/**
	 * Deletion/removal process
	 * @param line
	 * @return
	 */
	private boolean processRemoveLine(String line) {

		StringTokenizer st = new StringTokenizer(line);

		st.nextElement(); // EAT REMOVE :)
		String itemName = (String) st.nextElement();

		if (itemName == null || itemName.length() == 0) {
			return false;
		}

		printConsole("REMOVE " + itemName + "\n");
		if (!installDependencyManifest.containsKey(itemName)) {

			printConsole("\t" + itemName + " is not installed\n");
			return false;
		}

		boolean retval;
		if (installDependencyManifest.containsKey(itemName)
			&& installDependencyManifest.get(itemName).totalNonReferenceCount("commandline") > 0) {
			printConsole("\t" + itemName + " is still needed  \n");
			retval = false;
		} else {
			retval = removeItem(itemName, "commandline");
		}
		return retval;
	}
	
	
	private boolean removeItem(String itemName, String agent) {
		if (!installDependencyManifest.containsKey(itemName)) {
			return false;
		}
		DependencyGraphManager dependencyInstance = installDependencyManifest.get(itemName);

		boolean removed = false;
		for (Item item : dependencyInstance.item.dependencyList) {
			DependencyGraphManager dependencyInstance1 = installDependencyManifest.get(item.itemName);
			if (dependencyInstance1 == null) {
				continue;
			}
			removeItem(dependencyInstance1.item.itemName, itemName);
		}

		dependencyInstance.removeReferenceFromSource(agent);

		if (dependencyInstance.isAnyReference() == false) {
			printConsole("\tremoving " + dependencyInstance.item.itemName + "\n");
			installDependencyManifest.remove(dependencyInstance.item.itemName);
			removed = true;

		}
		return removed;
	}

/**
 * Installation Process details
 * @param line
 * @param agent
 * @return
 */

	protected int processInstallationLine(String line, String agent) {
		StringTokenizer st = new StringTokenizer(line);

		st.nextElement(); // EAT DEPEND :)
		String itemName = (String) st.nextElement();

		if (itemName == null || itemName.length() == 0) return -1;

		return processInstallation(itemName, agent);
	}
	protected int processInstallation(String item, String agent) {
		printConsole("INSTALL " + item + "\n");
		if (installDependencyManifest.containsKey(item)) {
			printConsole("\t" + item + " is already installed \n");
			return 0;
		}
		installItem(item, agent);
		return 0;
	}
	
	protected boolean installItem(String itemName, String agent) {
		Item item = itemDependencyMap.get(itemName);
		boolean reallyDidIt = false;

		DependencyGraphManager dependencyInstance = installDependencyManifest.get(itemName);

		if (item == null) 
			item = new Item(itemName);

		if (dependencyInstance == null) {
			dependencyInstance = new DependencyGraphManager(item);
			reallyDidIt = true;
		}
		dependencyInstance.addReferenceFromSource(agent);
		dependencyInstance.itemName = agent;
		
		installDependencyManifest.put(itemName, dependencyInstance);

		for (Item it : item.dependencyList) 
			installItem(it.itemName, itemName);

		if (reallyDidIt) 
			printConsole("\t installing " + itemName + "\n");
		
		return reallyDidIt;
	}

	protected void processListOfItemLine(String line) {
		printConsole("LIST\n");
		for (DependencyGraphManager dependencyInstance : installDependencyManifest.values()) {
			printConsole("\t" + dependencyInstance.item.itemName + " "
					+ /* DependencyGraphManager.itemDependencyCountMap + */ "\n");
		}
	}

	
	public boolean parseAndRun(InputStream is) {
		boolean bStop = false;
		boolean bErrorState = false;
		if (is == null) {
			printConsole(" ERROR: There is no input !");
			return Boolean.FALSE;
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		while (!bStop) {
			String line;
			try {
				line = br.readLine();

				if (!isValidLine(line)) {
					bStop = true;
					bErrorState = true;
					break;
				}
				if (isStopLine(line)) {
					bStop = true;
					printConsole("END\n");
					break;
				}
				processLine(line);

			} catch (Exception e) {
				bStop = true;
				e.printStackTrace();
				bErrorState = true;
			}

		}

		if (bErrorState) {
			System.err.println("ABNORMAL EXIT !!");
			return Boolean.FALSE;
		}

		return Boolean.TRUE;
	}

	// This could have been better structured. But due to time limit, the program is intendent to get actual output only
	
	public static void main(String[] args) {
		SystemDesign sd = new SystemDesign();
	    StringBuffer sbf = new StringBuffer(InputString.input1);
	    byte[] bytes = sbf.toString().getBytes();
	    InputStream inputStream = new ByteArrayInputStream(bytes);

	    if(sd.parseAndRun(inputStream)){
	    	sd.printConsole("SUCESS !!");
	    }else {
	    	sd.printConsole("FAILURE !!");
	    }
	    
	    
	    

	}

	
}