package gauge;

import java.io.*;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.util.*;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import common.Configuration;
import common.ConfigurationReader;

import sensorData.*;

/**
 * Class GaugeController class is responsible for:
 * 1. Computing the cycles;
 * 2. Generating the TFGReport.xls 
 */

public class GaugeController {
	
	private static final String FILE_EXTENSION_WITH_DOT = ".cs";
	private static final String FILE_EXTENSION = "cs";
	private static final int THRESHOLD = 000;
	private Vector<CycleElement> allElements = new Vector<CycleElement>();
	private Vector<Cycle> allCycles = new Vector<Cycle>();
	private HashSet<String> allMethodNames = new HashSet<String>();
	//private boolean removeNoisyLogs = false;
	private HashSet<String> allTestNames;
	private final static String vsComConf = "com.xml";
	ConfigurationReader comReader = new ConfigurationReader();
	Configuration com;
	
	public GaugeController(String find, HashSet<String> allTestNames) {
		//if(find.contentEquals("Yes"))
		//	removeNoisyLogs = true;
		this.allTestNames = allTestNames;
		Vector<String> entry = new Vector<String>();
		entry.add("Property");
		try {
			com = comReader.readConf(vsComConf, entry);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *	Computes the cycles. Each cycle must end with a successful TestBlock 
	 */

	public void computeCycle(){
		int elementIndx = allElements.size();
		CycleElement element = null;
		Cycle cycle = new Cycle();
		
		for(int i = 0; i < elementIndx; i++){
			element = (CycleElement)allElements.get(i);
			cycle.elementList.add(element);
			cycle.updateCycle(element); // set tpRatio and cycle elapsed time
			cycle.checkRefac(); // check refactoring cycle
			if (allTestSuccess(element)){
				allCycles.add(cycle);
				Vector<ActiveFile> v = new Vector<ActiveFile>();
				v.addAll(cycle.cycleFiles);		
				cycle = new Cycle();
				cycle.cycleFiles = v;
				cycle.lastTestMethods = element.testBlock;
			}
		}
		return;
	}

	
	/**
	 * Checks if the cycleElement's tests are all successful.
	 */
	private boolean allTestSuccess(CycleElement element){
	
		Vector<UnitTest> testBlock = element.testBlock;
		UnitTest test;
		int size = testBlock.size();
		
		for (int testIndx = 0; testIndx < size; testIndx++){
			test = (UnitTest)testBlock.get(testIndx);
			if (!( test.error.equals(" ") && test.failure.equals(" ")))
				return false;
		}
		return true;
	}
	
	/**
	 * Builds cycle element from the Hackystat JUnitTest and Activity records
	 */
	public void buildCycleElement (DataController dataControl, long idleTime)throws Exception{
		
		findElementTestBlock(dataControl);
		findElementActivity(idleTime, dataControl);
	}

	/**
	 * Retrieves the JUnit Test block for each cycle element
	 * @throws ParseException 
	 */
	@SuppressWarnings("unchecked")
	private void findElementTestBlock(DataController dataControl) throws ParseException{
		
		int size = dataControl.utr.unitTestRecord.size();
		long beginTime, finishTime;
		UnitTest ut1, ut2;
		Vector<UnitTest> currentBlock = new Vector<UnitTest>();
		
		for(int testIndx = 0; testIndx < size; testIndx++){
			CycleElement ce = new CycleElement();
			if(testIndx != size - 1){
				currentBlock.add(dataControl.utr.unitTestRecord.get(testIndx));			
				ut1 = (UnitTest)dataControl.utr.unitTestRecord.get(testIndx);
				ut2 = (UnitTest)dataControl.utr.unitTestRecord.get(testIndx+1);		
				beginTime = ut1.getTimeStamp();
				finishTime = ut2.getTimeStamp();			
				//TODO: After last test run, check if there are more activities!!!
				if (activityExist(beginTime, finishTime, dataControl)) {				
					ce.testBlock = (Vector<UnitTest>)currentBlock.clone();
					allElements.add(ce);
					currentBlock.clear();
				}	
			}else{
				currentBlock.add(dataControl.utr.unitTestRecord.get(testIndx));
				ce.testBlock = (Vector<UnitTest>)currentBlock.clone();
				allElements.add(ce);
			}
		}
		return;
	}

	/**
	 *  Retrieves the activities that take place in each cycle element;
	 *  Updates the file information for cycles 
	 */
	private void findElementActivity (long idleTime, DataController dataControl) throws Exception{
		
		long currentTime, preTime = 0, actTime;
		CycleElement currentElement;
		Activity act;
		Object[] testCaseArray = dataControl.utr.testCaseSet.toArray();
		Object[] projectArray = dataControl.adr.projectSet.toArray();
		int elementLoopSize = allElements.size();
		int activityLoopSize = dataControl.allActivities.size();
		int projectLoopSize = projectArray.length;
		String testCaseName;
		
		for (int i = 0; i < elementLoopSize; i++){
			currentElement = (CycleElement)allElements.get(i);
			currentTime = ((UnitTest)currentElement.testBlock.firstElement()).getTimeStamp();
			if(i!=0)
				preTime = allElements.get(i-1).testBlock.lastElement().getTimeStamp();
			for (int j = 0; j < activityLoopSize; j++){
				act = (Activity)dataControl.allActivities.get(j);
				actTime = act.getTimeStamp();
				if ((actTime < currentTime) && (actTime > preTime) && (act.data.endsWith(FILE_EXTENSION))){
					for (int projIndx = 0; projIndx < projectLoopSize; projIndx ++){
						// Check if the activity data is relevant to the project under development
						if (act.data.indexOf((String)projectArray[projIndx]) > 0) { // project matched
							currentElement.elementAct.add(act);
							for (int tcIndx = 0; tcIndx < testCaseArray.length; tcIndx++){	
								// Retrives the testCase and transfers it into TestCode file name 
								testCaseName = ((String)testCaseArray[tcIndx]).replace('.', '/');
								testCaseName = testCaseName.concat(FILE_EXTENSION_WITH_DOT);
								if (act.data.endsWith(testCaseName)){
									currentElement.testAct.add(act);
									//act.fileName = testCaseName;
									break;
								}else
									currentElement.prodAct.add(act);
							}	
							break;
						}
					}
				}
				if (actTime >= currentTime)
					break; 
			} 
			findActiveFile(currentElement, idleTime, dataControl); // Retrieve the active files in the current element.
		} 
	}		
	
	
	/**
	 * Devises the active files from Activity data for each CycleElement.
	 * @throws ParseException 
	 */
  	private void findActiveFile(CycleElement element, long idleTimeThreshold, DataController dataControl){
  		int size = element.elementAct.size(), indx;
		long time = 0, idleTimeTotal = 0;
  		Activity currentAct, nextAct;
  		ActiveFile currentFile;
		UnitTest firstTest= (UnitTest)element.testBlock.firstElement();
				
		for (indx = 0;  indx < size; indx++){
			currentAct = (Activity)element.elementAct.get(indx);
			if (indx != size-1)  {
				nextAct = (Activity)element.elementAct.get(indx+1);
				time = (nextAct.getTimeStamp()-currentAct.getTimeStamp());
				if (time%idleTimeThreshold > 0 ){
					idleTimeTotal = time/idleTimeThreshold * idleTimeThreshold;
					time = time % idleTimeThreshold;
				}
			}else{
				time = firstTest.getTimeStamp() - currentAct.getTimeStamp();
				if (time/idleTimeThreshold > 1 ){
					idleTimeTotal = time/idleTimeThreshold * idleTimeThreshold;
					time = time % idleTimeThreshold;		
				}
			}	
			
			ActiveFile existFile = findExistFile(currentAct.getFileName(), element.activeFile);					
			if (existFile == null){
				currentFile = constructActiveFile(element, currentAct, time, idleTimeTotal);
				element.activeFile.add(currentFile);
			}else {
				int i = element.activeFile.indexOf(existFile);
				currentFile = element.activeFile.get(i);
				//if(currentAct.type.equals("LineEdit") || currentAct.type.equals("TextDelete") || currentAct.type.equals("TextPaste")){
				//	currentFile.isEditing = true;//TODO
				//}
				if(com.getValue(currentAct.type).equalsIgnoreCase("E")){
					currentFile.isEditing = true;
					if(currentFile.isTestCode)
						element.isTestEditing = true;
					else
						element.isProdEditing = true;
				}
				currentFile.activeTime = currentFile.activeTime + time;
				currentFile.idleTime = currentFile.idleTime + idleTimeTotal;
				if(!currentAct.fName.equalsIgnoreCase("UNKNOWN")){
					if(!allMethodNames.contains(currentAct.fName)){
						currentFile.fNames.add(currentAct.fName + "(*)");
						allMethodNames.add(currentAct.fName);
					}else
						currentFile.fNames.add(currentAct.fName);
				}else{
					currentFile.fNames.add("UNKNOWN");
				}
				element.activeFile.setElementAt(currentFile, i);
			}			
		} 
	}

	private ActiveFile constructActiveFile(CycleElement element, Activity currentAct, long time, long idleTimeTotal) {
		ActiveFile currentFile = new ActiveFile();
	
		currentFile.fileName = currentAct.fileName;
		currentFile.activeTime = time;
		currentFile.idleTime = idleTimeTotal;
		if(!currentAct.fName.equalsIgnoreCase("UNKNOWN")){
			if(!allMethodNames.contains(currentAct.fName)){
				currentFile.fNames.add(currentAct.fName + "(*)");
				allMethodNames.add(currentAct.fName);
			}else
				currentFile.fNames.add(currentAct.fName);
		}else{
			currentFile.fNames.add("UNKNOWN");
		}
		if (findTestAct(currentAct, element.testAct)){ 
			currentFile.isTestCode = true;
		}
		//if(currentAct.type.equals("LineEdit") || currentAct.type.equals("TextDelete") || currentAct.type.equals("TextPaste")){
		//	currentFile.isEditing = true;//TODO
		//}
		if(com.getValue(currentAct.type).equalsIgnoreCase("E")){
			currentFile.isEditing = true;
			if(currentFile.isTestCode)
				element.isTestEditing = true;
			else
				element.isProdEditing = true;
		}
		return currentFile;
	}
	
	/**
	  * Checks if an activity is a TestCode activity
	  */
	private boolean findTestAct(Activity currentAct, Vector<Activity> actList) {
		Activity testAct;
		int size = actList.size();
		
		if (actList.size() > 0){
			for (int i = 0; i < size; i++){
				testAct = (Activity)actList.get(i);
				if (testAct.fileName.equals(currentAct.fileName))
					return true;
			}
		}
		return false;
	}

	
	/**
	 * Checks if there is a file already exists in the active file list, 
	 * Returns the existing file, otherwise returns null
	 */
	private ActiveFile findExistFile(String fileName, Vector<ActiveFile> fileList){
		ActiveFile existFile = null;
		int size = fileList.size();
		
		if (size > 0){
			for (int i = 0; i < size; i++){
				existFile = (ActiveFile)fileList.get(i);
				if (existFile.fileName.equals(fileName))
					return existFile;
			}
		}
		return null;
	}
	
	/**
	 * Checks if there is any activity take place between a time interval.
	 */
	protected boolean activityExist(long beginTime, long endTime, DataController dataControl){
		long activityTime;
		int size = dataControl.adr.activityRecord.size();
		if(endTime - beginTime <= THRESHOLD)
			return false;
		for (int i = 0; i < size; i++){
			if(!dataControl.adr.activityRecord.get(i).type.equalsIgnoreCase("Test Run")){
				activityTime = ((Activity)dataControl.adr.activityRecord.get(i)).getTimeStamp();
				if ((activityTime > beginTime) && (activityTime < endTime))
					return true;
			}
		}
		return false;	 
	}
		
	
	public void updateExcel(String template, String report) throws Exception {
		// Initializes a new report file based on the template		
		copyFile(new File(template),new File(report));
		
		// Initializes the workbook and worksheets
		FileInputStream instream = new FileInputStream(report);
		POIFSFileSystem fsin = new POIFSFileSystem(instream);
		HSSFWorkbook wb = new HSSFWorkbook(fsin);
		
		HSSFSheet sheet0 = wb.getSheetAt(0); // TestFirstGauge_Report Sheet
		//HSSFSheet sheet1 = wb.getSheetAt(1); // Cycle_TPRatio Sheet
		HSSFSheet sheet2 = wb.getSheetAt(1); // Cycle_ElapsedTime Sheet
		HSSFSheet sheet3 = wb.getSheetAt(2); // Cummulated CycleTime Sheet
		HSSFSheet sheet4 = wb.getSheetAt(3); // ActiveTime_Pattern Sheet
		HSSFSheet sheet5 = wb.getSheetAt(4); // CycleTimeDistribution Sheet
		//HSSFSheet sheet6 = wb.getSheetAt(6); // TPRatio_Pattern Sheet
		HSSFSheet sheet7 = wb.getSheetAt(5); // Summary statistics
		HSSFSheet sheet8 = wb.getSheetAt(6); // Test_ProdTime Sheet
		HSSFSheet sheet9 = wb.getSheetAt(7); // Refactoring Sheet
		HSSFSheet sheet10 = wb.getSheetAt(8); // Conformance Analysis Sheet

		// Defines the cell style in the first column in the cycle row, aligns at the center
		HSSFCellStyle firstCycColCS = wb.createCellStyle();
		firstCycColCS.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		firstCycColCS.setBorderRight(HSSFCellStyle.BORDER_THIN);
		firstCycColCS.setBorderTop(HSSFCellStyle.BORDER_THIN);
		firstCycColCS.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		firstCycColCS.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		firstCycColCS.setWrapText(true);
		
		// Defines the cell style in the other columns on the cycle row , aligns at the left(default)
		HSSFCellStyle cycFirstCS = wb.createCellStyle();
		cycFirstCS.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		cycFirstCS.setBorderRight(HSSFCellStyle.BORDER_THIN);
		cycFirstCS.setBorderTop(HSSFCellStyle.BORDER_THIN);
		cycFirstCS.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		//cycFirstCS.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		cycFirstCS.setWrapText(true);
		
		// Defines the cell style in the other columns on the cycle row , aligns at the left(default)
		HSSFCellStyle cycDecCS = wb.createCellStyle();
		cycDecCS.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		cycDecCS.setBorderRight(HSSFCellStyle.BORDER_THIN);
		cycDecCS.setBorderTop(HSSFCellStyle.BORDER_THIN);
		cycDecCS.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		//cycDecCS.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		cycDecCS.setWrapText(true);
		cycDecCS.setDataFormat((short)2);
	
		// Defines the style for common cell, which is not on the top row
		HSSFCellStyle commonCS = wb.createCellStyle();
		commonCS.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		commonCS.setBorderRight(HSSFCellStyle.BORDER_THIN);
		commonCS.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		//commonCS.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		commonCS.setWrapText(true);
		
		// Defines the style for common cell, which is not on the top row
		HSSFCellStyle commonDecCS = wb.createCellStyle();
		commonDecCS.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		commonDecCS.setBorderRight(HSSFCellStyle.BORDER_THIN);
		commonDecCS.setDataFormat((short)2);
		commonDecCS.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		//commonDecCS.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		commonDecCS.setWrapText(true);
		
		// Defines the style for cycle_total row, which is the last row for each cycle 
		HSSFCellStyle cycLastCS = wb.createCellStyle();
		cycLastCS.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		cycLastCS.setBorderRight(HSSFCellStyle.BORDER_THIN);
		cycLastCS.setBorderTop(HSSFCellStyle.BORDER_THIN);
		cycLastCS.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		cycLastCS.setFillPattern(HSSFCellStyle.FINE_DOTS);
		cycLastCS.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		//cycLastCS.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		cycLastCS.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		cycLastCS.setWrapText(true);
		
		// Defines the style for cycle_total row, which is the last row for each cycle 
		HSSFCellStyle cycLastDecCS = wb.createCellStyle();
		cycLastDecCS.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		cycLastDecCS.setBorderRight(HSSFCellStyle.BORDER_THIN);
		cycLastDecCS.setBorderTop(HSSFCellStyle.BORDER_THIN);
		cycLastDecCS.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		cycLastDecCS.setFillPattern(HSSFCellStyle.FINE_DOTS);
		cycLastDecCS.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		//cycLastDecCS.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		cycLastDecCS.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		cycLastDecCS.setWrapText(true);
		cycLastDecCS.setDataFormat((short)2);
		
		// Defines the style for refactoring cycle cell
		HSSFCellStyle refCycleCell = wb.createCellStyle();
		refCycleCell.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		refCycleCell.setBorderRight(HSSFCellStyle.BORDER_THIN);
		refCycleCell.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		//refCycleCell.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		refCycleCell.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		refCycleCell.setFillForegroundColor(HSSFColor.YELLOW.index);
		
		// Defines the style for the refactoring cycle cell in the first row of the cycle
		HSSFCellStyle refFirstCell = wb.createCellStyle();
		refFirstCell.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		refFirstCell.setBorderRight(HSSFCellStyle.BORDER_THIN);
		refFirstCell.setBorderTop(HSSFCellStyle.BORDER_THIN);
		refFirstCell.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		//refFirstCell.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		refFirstCell.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		refFirstCell.setFillForegroundColor(HSSFColor.YELLOW.index);
		
		
		// Defines the 2-decimal points cell style
		HSSFCellStyle decCS = wb.createCellStyle();
		decCS.setDataFormat((short)2);
		
		HSSFCellStyle intCS = wb.createCellStyle();
		intCS.setDataFormat((short)0);
		intCS.setAlignment(HSSFCellStyle.ALIGN_CENTER);
				
		HSSFCellStyle redCS = wb.createCellStyle();
		redCS.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		redCS.setFillForegroundColor(HSSFColor.RED.index);
		redCS.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		
		HSSFCellStyle greenCS = wb.createCellStyle();
		greenCS.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		greenCS.setFillForegroundColor(HSSFColor.BRIGHT_GREEN.index);
		greenCS.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		
		HSSFCellStyle yellowCS = wb.createCellStyle();
		yellowCS.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		yellowCS.setFillForegroundColor(HSSFColor.YELLOW.index);
		yellowCS.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		
		// Set the column width
		sheet0.setColumnWidth((short) 0, (short) 1800);
		sheet0.setColumnWidth((short) 1, (short) 4000);
		sheet0.setColumnWidth((short) 2, (short) 15000);
		sheet0.setColumnWidth((short) 3, (short) 3000);
		sheet0.setColumnWidth((short) 4, (short) 3000);
		sheet0.setColumnWidth((short) 5, (short) 2500);
		sheet0.setColumnWidth((short) 6, (short) 2500);
		sheet0.setColumnWidth((short) 7, (short) 3800);
		//sheet0.setColumnWidth((short) 8, (short) 3000);
		//		
		
		int rowNumCounter = 1;
		HSSFRow nextRow;
		//HSSFRow nextRow_sh1;
		HSSFRow nextRow_sh2;
		HSSFRow nextRow_sh3;
		HSSFRow nextRow_sh4;
		HSSFRow nextRow_sh5;
		//HSSFRow nextRow_sh6;
		HSSFRow nextRow_sh7;
		HSSFRow nextRow_sh8;
		HSSFRow nextRow_sh9;
		HSSFRow nextRow_sh10;
		
		// the cells in the sheets
		//HSSFCell cell_sh1;
		HSSFCell cell_sh2;
		HSSFCell cell_sh4;
		HSSFCell cell_sh5;
		//HSSFCell cell_sh6;
		HSSFCell cell_sh7;
		//HSSFCell cell_sh8;
		HSSFCell cell_sh9;
		HSSFCell cell_sh10;
		
		
		
		long cumCycTime = 0;
		
		Cycle cycle;
		// Write each cycle's statistics
		for (int i = 1; i < allCycles.size()+1; i++){ // cycle index starts with 1 in the report
			Vector<ActiveFile> files;
			Vector<UnitTest> tests;
			
			Activity firstTestAct = new Activity();
			Activity firstProdAct = new Activity(); 
			
			rowNumCounter++; // starts with rowNumCounter = 2
			cycle = (Cycle)allCycles.get(i-1);
		
			// Report current cycle statistics
			// Creates and set cell style in the row
			nextRow = sheet0.createRow((short)rowNumCounter);
			for (short cellIndx = 0; cellIndx < 8; cellIndx++){
				nextRow.createCell((short)cellIndx);
				switch (cellIndx){
					case 5:
					case 6:
					//case 8:
						nextRow.getCell(cellIndx).setCellStyle(cycDecCS);
						break;
					default: 
						if (cellIndx > 0)
							nextRow.getCell(cellIndx).setCellStyle(cycFirstCS); 
						break;
				}
			}
			
			// Set and Fill in the first cylce row cell 
			nextRow.getCell((short)0).setCellStyle(firstCycColCS);
			if (cycle.isRefactoring == true)
				nextRow.getCell((short)0).setCellStyle(refFirstCell);
				
			nextRow.getCell((short)0).setCellValue(i);
			
			// Fills the TPRatio into Cycle_TPRatio sheet
			//nextRow_sh1 = sheet1.createRow((short)(i-1));
			//cell_sh1 = nextRow_sh1.createCell((short)0);
			//cell_sh1.setCellStyle(decCS);
			//cell_sh1.setCellValue(cycle.getTPRatio());
				
			// Fills the elapsed time into Cycle_ElapsedTime sheet
			nextRow_sh2 = sheet2.createRow((short)(i-1));
			cell_sh2 = nextRow_sh2.createCell((short)0);
			cell_sh2.setCellStyle(decCS);
			cell_sh2.setCellValue((cycle.getTime())/1000.0);
			
			// Fills the cumulative cycle elapsed time sheet
			cumCycTime = (long) (cumCycTime + (cycle.getTime())/1000.0);
			nextRow_sh3 = sheet3.createRow((short)(i-1));
			nextRow_sh3.createCell((short)0).setCellValue(cumCycTime/60);
			nextRow_sh3.createCell((short)1).setCellValue(0);
						
			// Fills the cumulative cycle activetime vs Pattern sheet
			nextRow_sh4 = sheet4.createRow((short)(i-1));
			cell_sh4 = nextRow_sh4.createCell((short)0);
			cell_sh4.setCellStyle(decCS);
			cell_sh4.setCellValue((cycle.getTime())/60000.0);
			
			nextRow_sh4.createCell((short)1).setCellValue(cumCycTime/60);
			nextRow_sh4.createCell((short)2).setCellValue(0);
			
			// Fills the CycleTimeDistribution sheet
			nextRow_sh5 = sheet5.createRow((short)(i-1));
			cell_sh5 = nextRow_sh5.createCell((short)0);
			cell_sh5.setCellStyle(decCS);
			cell_sh5.setCellValue((cycle.getTime())/60000.0);
			
			// Fills the TPRatio vs Pattern sheet
			//nextRow_sh6 = sheet6.createRow((short)(i-1));
			//cell_sh6 = nextRow_sh6.createCell((short)0);
			//cell_sh6.setCellStyle(decCS);
			//cell_sh6.setCellValue(cycle.getTPRatio());
			
			//nextRow_sh6.createCell((short)1).setCellValue(cumCycTime/60);
			//nextRow_sh6.createCell((short)2).setCellValue(0);
			
			// Fills the Summary Statistics sheet 
			nextRow_sh7 = sheet7.createRow((short)(i-1));
			cell_sh7 = nextRow_sh7.createCell((short)0);
			cell_sh7.setCellStyle(decCS);
			cell_sh7.setCellValue(-cycle.getTestCodeTime()/1000.0);
			
			cell_sh7 = nextRow_sh7.createCell((short)1);
			cell_sh7.setCellStyle(decCS);
			cell_sh7.setCellValue(cycle.getProdCodeTime()/1000.0);
			
			// Fills the TestCode/ProdCode Time/Size Comparison sheet
			nextRow_sh8 = sheet8.createRow((short)1);
			//nextRow_sh8.createCell((short)0).setCellValue(cycle.getTestCodeSize());
			//nextRow_sh8.createCell((short)1).setCellValue(cycle.getProdCodeSize());
		
			// Fills the Refactoring Sheet
			nextRow_sh9 = sheet9.createRow((short)(i-1));
			cell_sh9 = nextRow_sh9.createCell((short)0);
			cell_sh9.setCellStyle(decCS);
			if (!cycle.isRefactoring)
				cell_sh9.setCellValue(cycle.getTime()/60000.0);
			
			cell_sh9 = nextRow_sh9.createCell((short)1);
			cell_sh9.setCellStyle(decCS);
			if (cycle.isRefactoring)
				cell_sh9.setCellValue(-cycle.getTime()/60000.0);
			
			
			// Fills in the other cycle row cells
			//nextRow.getCell((short)8).setCellValue(cycle.getTPRatio());
			
			// Write each cycle element's statistics			
			for (int j = 0; j < cycle.elementList.size(); j++){
				
				files = ((CycleElement)cycle.elementList.get(j)).activeFile;
				
				// Check what the first activity is: writing test code or production code?
				// Only do this check when both prodAct and testAct exist
				if ((((CycleElement)cycle.elementList.get(j)).testAct.size() != 0 ) 
				 && (((CycleElement)cycle.elementList.get(j)).prodAct.size() != 0 )){
					firstTestAct = (Activity)(((CycleElement)cycle.elementList.get(j)).testAct).get(0);
					firstProdAct = (Activity)(((CycleElement)cycle.elementList.get(j)).prodAct).get(0);;
					
					if (firstTestAct.getTimeStamp() < firstProdAct.getTimeStamp()){ // A TDD cycle
						
						if (cycle.testCodeTimeFilled == false){
							nextRow.getCell((short)6).setCellValue(cycle.getTestCodeTime()/1000.0);
							cycle.testCodeTimeFilled = true;
						}
						
						rowNumCounter = writeNextRow(cycle.isRefactoring, "Test Code", files, nextRow, rowNumCounter, sheet0, commonCS, commonDecCS, refCycleCell );
						nextRow = createNextRow(cycle.isRefactoring, (short)rowNumCounter, sheet0, commonCS, commonDecCS, refCycleCell);
						
						if (cycle.prodCodeTimeFilled == false){
							nextRow.getCell((short)6).setCellValue(cycle.getProdCodeTime()/1000.0);
							cycle.prodCodeTimeFilled = true;
						}
						rowNumCounter = writeNextRow(cycle.isRefactoring, "Prod Code", files, nextRow, rowNumCounter, sheet0, commonCS, commonDecCS, refCycleCell);
						nextRow = createNextRow(cycle.isRefactoring, (short)rowNumCounter, sheet0, commonCS, commonDecCS, refCycleCell);
				 	}
					else { // A non-TDD cycle
						
						if (cycle.prodCodeTimeFilled == false){
							nextRow.getCell((short)6).setCellValue(cycle.getProdCodeTime()/1000.0);
							cycle.prodCodeTimeFilled = true;
						}
						rowNumCounter = writeNextRow(cycle.isRefactoring, "Prod Code", files, nextRow, rowNumCounter, sheet0, commonCS, commonDecCS, refCycleCell);
						nextRow = createNextRow(cycle.isRefactoring, (short)rowNumCounter, sheet0, commonCS, commonDecCS, refCycleCell);
						
						if (cycle.testCodeTimeFilled == false){
							nextRow.getCell((short)6).setCellValue(cycle.getTestCodeTime()/1000.0);
							cycle.testCodeTimeFilled = true;
						}
						rowNumCounter = writeNextRow(cycle.isRefactoring, "Test Code", files,  nextRow, rowNumCounter, sheet0, commonCS, commonDecCS, refCycleCell);
						nextRow = createNextRow(cycle.isRefactoring, (short)rowNumCounter, sheet0, commonCS, commonDecCS, refCycleCell);
					}
				}
				// No testAct, write Prod Code only
				else if (((CycleElement)cycle.elementList.get(j)).testAct.size() == 0 && ((CycleElement)cycle.elementList.get(j)).prodAct.size() != 0 ){
					
					if (cycle.prodCodeTimeFilled == false){
						nextRow.getCell((short)6).setCellValue(cycle.getProdCodeTime()/1000.0);
						cycle.prodCodeTimeFilled = true;
					}
					rowNumCounter = writeNextRow(cycle.isRefactoring, "Prod Code", files, nextRow, rowNumCounter, sheet0, commonCS, commonDecCS, refCycleCell);
					nextRow = createNextRow(cycle.isRefactoring, (short)rowNumCounter, sheet0, commonCS, commonDecCS, refCycleCell);
				}
				
				// No prodAct, write Test Code only
				else if (((CycleElement)cycle.elementList.get(j)).prodAct.size() == 0 && ((CycleElement)cycle.elementList.get(j)).testAct.size() != 0 ){
					
					if (cycle.testCodeTimeFilled == false){
						nextRow.getCell((short)6).setCellValue(cycle.getTestCodeTime()/1000.0);
						cycle.testCodeTimeFilled = true;
					}
					rowNumCounter = writeNextRow(cycle.isRefactoring, "Test Code", files, nextRow, rowNumCounter, sheet0, commonCS, commonDecCS, refCycleCell);
					nextRow = createNextRow(cycle.isRefactoring, (short)rowNumCounter, sheet0, commonCS, commonDecCS, refCycleCell);
				}
				
				// Write to JUnit Run rows
				
				HSSFCell testCell = nextRow.getCell((short)1);
				testCell.setCellValue("Test Case Run");
		
				tests = ((CycleElement)cycle.elementList.get(j)).testBlock;
				int passTestCounter = 0;
				String passedTests = "", failedTests = "";
				for (int k = 0; k < tests.size(); k++){
					long testTime = 0;
					HashSet<String> testSet = new HashSet<String>(); // testSet records the testMethods
					
					UnitTest test = (UnitTest)tests.get(k);
					testSet.add(test.testName);
					// Calcuates the test time, attempted and passed times for the first test
					if (test.counted == false){
						testTime = testTime + test.getTestTime();
						
						int attemptNumber = 1;
						int passNumber = 0;
						int failNumber = 0;
						
						if (test.failure.equals(" ") && test.error.equals(" ")){
							passNumber++;
							passTestCounter++;
							passedTests = test.testName + " (P)_";						
						}else{
							failedTests = test.testName + " (F)_";
							failNumber++;
						}
						
						// Checks the rest of the tests which has the same testCaseName
						for (int l = k+1; l < tests.size(); l++){
							UnitTest nextTest = (UnitTest)tests.get(l);
							if ((nextTest.testCaseName.equals(test.testCaseName)) && (nextTest.counted == false)){ 
								testTime = testTime + nextTest.getTestTime();
								nextTest.counted = true;
								
								if (nextTest.failure.equals(" ") && nextTest.error.equals(" ")){
									passedTests = passedTests +  nextTest.testName + " (P)_";
									passTestCounter++;
								}else{
									failedTests = failedTests  + nextTest.testName + " (F)_";
									failNumber++;
								}
							
								if (!testSet.contains(nextTest.testName)){
									testSet.add(nextTest.testName);
									attemptNumber++;
									if (nextTest.failure.equals(" ") && nextTest.error.equals(" "))
										passNumber++;									
								}								
							}
						}
						
						HSSFCell nameCell 	= nextRow.getCell((short) 2);
						HSSFCell attempCell = nextRow.getCell((short) 3);
						HSSFCell passCell 	= nextRow.getCell((short) 4);
						HSSFCell timeCell 	= nextRow.getCell((short) 5);
						
						String str = failedTests + passedTests;
						str = str.replace('_', '\n');
						str = str.substring(0, str.length()-1);
					
						nameCell.setCellValue(str);//test.testCaseName);
						attempCell.setCellValue(attemptNumber);
						passCell.setCellValue(passNumber);
						timeCell.setCellValue(testTime/1000.0);
						
						nextRow = createNextRow(cycle.isRefactoring, (short)++rowNumCounter, sheet0, commonCS, commonDecCS, refCycleCell);
							
						// The last cycle element 
						HSSFCellStyle lastTestBlockStyle = wb.createCellStyle();
						if ( j == cycle.elementList.size()-1){
							// Adds colour 
							lastTestBlockStyle.setFillForegroundColor(HSSFColor.BRIGHT_GREEN.index);
						}else{
							lastTestBlockStyle.setFillForegroundColor(HSSFColor.RED.index);
						}	
						
						lastTestBlockStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
						
						lastTestBlockStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
						//lastTestBlockStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
						lastTestBlockStyle.setWrapText(true);
						
						testCell.setCellStyle(lastTestBlockStyle);
						nameCell.setCellStyle(lastTestBlockStyle);
						attempCell.setCellStyle(lastTestBlockStyle);
						passCell.setCellStyle(lastTestBlockStyle);
						
						if ( passTestCounter == tests.size() ){ // tests are all successful  
							// Re-sets the cell style for the cycle last row  
							for (short cellIndx = 1; cellIndx < 8; cellIndx++)
								switch (cellIndx){
									case 5: 
										nextRow.getCell(cellIndx).setCellStyle(cycLastDecCS);
										break;
									default: 
										nextRow.getCell(cellIndx).setCellStyle(cycLastCS);
										break;
								}
							sheet0.addMergedRegion(new Region(rowNumCounter,(short)1,rowNumCounter,(short)4));

							// Fills in the cycle total active time and idle time						
							nextRow.getCell((short)1).setCellValue("Cycle Total");
							if (cycle.isRefactoring)
								nextRow.getCell((short)0).setCellValue("R");
							nextRow.getCell((short)5).setCellValue(cycle.getTime()/1000.0);
							nextRow.getCell((short)7).setCellValue(cycle.getIdleTime()/1000.0);
						}
						
					}
				} // end of element's testBlock 
					
			} // end of cycle element
		
			// Draw the last border of the spread sheet
			if ( i == allCycles.size()){
				int rowNumber = sheet0.getLastRowNum();
				HSSFRow lastRow = sheet0.getRow(rowNumber);
				lastRow.getCell((short)0).setCellValue("-END-");
					
				for (short cellIndx = 0; cellIndx < 8; cellIndx++)
					switch (cellIndx){
						case 5: 
							nextRow.getCell(cellIndx).setCellStyle(cycLastDecCS);
							break;
						default: 
							nextRow.getCell(cellIndx).setCellStyle(cycLastCS);
							break;
					}
				sheet0.addMergedRegion(new Region(rowNumber,(short)1,rowNumber,(short)4));

			}
			
			cycle.SetCycleTypes();
			nextRow_sh10 = sheet10.createRow((short)(i));
			cell_sh10 = nextRow_sh10.createCell((short)0);
			if(!cycle.isRefactoring)
				cell_sh10.setCellStyle(intCS);
			else
				cell_sh10.setCellStyle(yellowCS);
			cell_sh10.setCellValue(i);
			
			cell_sh10 = nextRow_sh10.createCell((short)1);			
			//cell_sh10.setCellStyle(level1Analysis(cycle, redCS, greenCS, intCS));
			if(cycle.type.equalsIgnoreCase("TF")){
				cell_sh10.setCellStyle(greenCS);
				//cycle.isTddCompliant = "Y";
			}
			else if(cycle.type.equalsIgnoreCase("TL")){
				cell_sh10.setCellStyle(redCS);
				//cycle.isTddCompliant = "N";
			}
			else
				cell_sh10.setCellStyle(intCS);
			cell_sh10.setCellValue(cycle.type);
			
			cell_sh10 = nextRow_sh10.createCell((short)2);			
			cell_sh10.setCellStyle(level2Analysis(allCycles, i-1, redCS, greenCS, intCS));
			cell_sh10.setCellValue(cycle.type);
			//System.out.println(cycle.isTddCompliant);
		} // end of cycle
		
			FileOutputStream fileOut = new FileOutputStream(report);
			wb.write(fileOut);
			fileOut.close();
			instream.close();
	}

	
	private HSSFCellStyle level2Analysis(Vector<Cycle> cycles, int idx, HSSFCellStyle redCS,
			HSSFCellStyle greenCS, HSSFCellStyle intCS) {
		
		Cycle cycle = cycles.elementAt(idx);
		boolean isTddBefore = false, isTddAfter = false;
		
		
		//level1Analysis(cycle, redCS, greenCS);
		if((cycle.type.equalsIgnoreCase("R") || cycle.type.equalsIgnoreCase("TA") || cycle.type.equalsIgnoreCase("PA") || cycle.type.equalsIgnoreCase("U")) ){
			for(int i = idx; i >= 0; i--){
				if (cycles.elementAt(i).isTddCompliant.equalsIgnoreCase("Y")){
					isTddBefore = true;
					break;
				}
				if (cycles.elementAt(i).isTddCompliant.equalsIgnoreCase("N")){
					//isTddBefore = false;
					break;
				}
			}
			for(int i = idx; i < cycles.size(); i++){
				if (cycles.elementAt(i).isTddCompliant.equalsIgnoreCase("Y")){
					isTddAfter = true;
					break;
				}if (cycles.elementAt(i).isTddCompliant.equalsIgnoreCase("N")){
					//isTddAfter = false;
					break;
				}				
			}
				
			if(isTddBefore && isTddAfter){
				cycle.isTddCompliant = "Y";
				return greenCS;
			}
			else{
				cycle.isTddCompliant = "N";
				return redCS;
			}
			
		}
		return level1Analysis(cycle, redCS, greenCS, intCS);
				
		// TODO Auto-generated method stub
		
	}

	private HSSFCellStyle level1Analysis(Cycle cycle, HSSFCellStyle redCS,
			HSSFCellStyle greenCS, HSSFCellStyle intCS) {
		if(cycle.type.equalsIgnoreCase("TF")){
			//cycle.isTddCompliant = "Y";
			return greenCS;
		}
		if(cycle.type.equalsIgnoreCase("TL")){
			//cycle.isTddCompliant = "N";
			return redCS;
		}
		
		return intCS;
	}

	private int writeNextRow(boolean ref, String codeActivity, Vector<ActiveFile> files, HSSFRow nextRow, int rowNum, HSSFSheet sheet, HSSFCellStyle cs1, HSSFCellStyle cs2, HSSFCellStyle refcs) {
		int rowIndex = rowNum;
		ActiveFile file;
		
		for (int k = 0; k < files.size(); k++){
			file = (ActiveFile)files.get(k);
			if((codeActivity.startsWith("Test") && file.isTestCode) || (!codeActivity.startsWith("Test") && !file.isTestCode)){
				if (file.included == false){
					String str = extractUniqueFiles(file);
					if(file.isEditing)
						nextRow.getCell((short)1).setCellValue(codeActivity + "\nEdit");
					else
						nextRow.getCell((short)1).setCellValue(codeActivity + " \nNon-Edit");
					nextRow.getCell((short)2).setCellValue(str);//file.fileName);
					nextRow.getCell((short)5).setCellValue(file.activeTime/1000.0);
					nextRow.getCell((short)7).setCellValue(file.idleTime/1000.0);
					file.included = true;
					rowIndex++;
					// Creates the next row
					nextRow = createNextRow(ref, (short)rowIndex, sheet, cs1, cs2, refcs);
				}
			}
		}
		
		return rowIndex; 
	}

	private String extractUniqueFiles(ActiveFile file) {
		int size = file.fNames.size();
		Vector<String> uniqueFiles = new Vector<String>();
		String str;
		String testName = "";
		for(int i = 0; i < size; i++ ){
			if(!file.fNames.elementAt(i).contains("UNKNOWN")){
				if(file.isTestCode && file.fNames.elementAt(i).contains(".")){
					testName = file.fNames.elementAt(i).substring(file.fNames.elementAt(i).lastIndexOf('.')+1, file.fNames.elementAt(i).length()).replace("(*)", "");
				}
				if(uniqueFiles.contains(file.fNames.elementAt(i).replace("(*)", "")) || uniqueFiles.contains(file.fNames.elementAt(i).replace("(*)", "") + "(*)") || (file.isTestCode && !this.allTestNames.contains(testName)))
					;
				else if(file.fNames.contains(file.fNames.elementAt(i).replace("(*)", "") + "(*)")){
					uniqueFiles.add(file.fNames.elementAt(i).replace("(*)", "") + "(*)");
				}else 
					uniqueFiles.add(file.fNames.elementAt(i));
			}
		}
		
		/*if(removeNoisyLogs){
			for(int i = 0; i < uniqueFiles.size(); i++ ){
				for(int j = 0; j < uniqueFiles.size(); j++ ){
					if(i != j && uniqueFiles.get(j).toLowerCase().contains(uniqueFiles.get(i).toLowerCase()))
						uniqueFiles.setElementAt("_", i);
				}
			}
		}*/
		str = "(" + file.fileName + ")\n" + uniqueFiles.toString();
		str = str.replace("_, ", "");
		str = str.replace(", _", "");
		str = str.replace(",", "\n");
		str = str.replace(" ", "");
		str = str.replace("[", "");
		str = str.replace("]", "");
		str.trim();
		
		return str;
	}

	private HSSFRow createNextRow(boolean ref, short rowNumber, HSSFSheet sheet, HSSFCellStyle cs1, HSSFCellStyle cs2, HSSFCellStyle refcs) {
		HSSFRow nextRow = sheet.createRow(rowNumber);
		for (short cellIndx = 0; cellIndx < 8; cellIndx++){
			nextRow.createCell((short)cellIndx);
			switch (cellIndx){
				case 5:
				case 6:
				//case 8:
					nextRow.getCell(cellIndx).setCellStyle(cs2);
					break;
				default: 
					nextRow.getCell(cellIndx).setCellStyle(cs1);
					break;
			}
		}
		if (ref == true)
			nextRow.getCell((short)0).setCellStyle(refcs);
			
		return nextRow;
	}

	private void copyFile(File in, File out)  {
		
		if (out.exists())
			out.delete();

		FileChannel sourceChannel = null;
		FileChannel destinationChannel = null;
		try {
			sourceChannel = new FileInputStream(in).getChannel();
			destinationChannel = new FileOutputStream(out).getChannel();
		} catch (FileNotFoundException e) {
			System.out.println("\n(!)Please quit the report file and try again");
			return;
		}
		try {
			sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
			sourceChannel.close();
			destinationChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}	
		
}
