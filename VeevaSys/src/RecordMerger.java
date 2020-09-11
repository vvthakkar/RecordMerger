import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.*;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class RecordMerger {
	public static final String FILENAME_COMBINED = "combined.csv";
	public static final String DIRECTORY = "data\\";

	final static Logger logger = Logger.getLogger(RecordMerger.class);
	
	
	/**
     * Initialize treebased(for sorting purpose) guava data table with
     * Comparator.naturalOrder() for ascending order and Id on first column comparator
     *
     */
	static Table<String, String, String> DataTable=TreeBasedTable.create(Comparator.naturalOrder(), new Comparator<String>() {
        @Override
        public int compare(String s1, String s2) {
        	if ("ID".equals(s1) && "ID".equals(s2)){
                return 0;
            } if ("ID".equals(s1)){
                return -1;
            } if ("ID".equals(s2)){
                return 1;
            }else{
                return s1.compareTo(s2);
            }
        }
    }
    );
	 
    /**
     * Entry point: Reads data from diferent data sources from data\ directory and writes merged data into 
     * cmobined.csv
     * 
     *Features
     *can read and extract any number of columns (Assumed each table has ID in common )
     *can read any number of data 
     *can read 2 file types html and csv and txt
	 *can read any numbers of data files
	 *Exports data to combined.csv (order-Ascending by ID)
     * @param String[] args
     * @return void
     * @throws IOException if error reading/writing from/to file
     *
     */
	public static void main(String[] args) throws IOException {
		//Configured log4j programatically so no need to add any additional config file for log4j
    	configureLog4j();
		
    	//Reading files from directory
    	List<String> filesInDirectory = Files.walk(Paths.get(DIRECTORY))
                .filter(Files::isRegularFile)
                .map(Path::toString)
                .collect(Collectors.toList());
   	  
    	logger.debug("List of file in directory"+filesInDirectory);
    	
    	 for(String fileName:filesInDirectory) {
 			if(fileName.contains(".html")) readFromHtml(fileName);
 			else if (fileName.contains("csv") && !fileName.equals(FILENAME_COMBINED)) readFromCsv(fileName);
 			else logger.error(fileName+": the file format is not supported");
 		 }
 	       
		ExportDataToCsv();
 	}
	
	/**
     *Configures log4j programatically.
	 
     * @param null
     * @return void
     * 
     */
	private static void configureLog4j() {
	// TODO Auto-generated method stub
    	Logger rootLogger = Logger.getRootLogger();
    	
		rootLogger.setLevel(Level.DEBUG);
    	PatternLayout layout = new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} [%t] %-5p %c %x %M - %m%n");
    	rootLogger.addAppender(new ConsoleAppender(layout));
    }
    /**
     * Exports data from guava table object into csv file 
     *
     * @param null
     * @return void
     * @throws IOException if error reading/writing from/to file
     *
     */
	public static void ExportDataToCsv() throws IOException {
		// TODO Auto-generated method stub
		 CSVWriter csvwriter = new CSVWriter(new FileWriter(DIRECTORY+FILENAME_COMBINED), ',','"');
	        Set<String> columns = DataTable.columnKeySet();
	     
	        csvwriter.writeNext(columns.toArray(new String[columns.size()]));
	        
	        for(String id : DataTable.rowKeySet()){
	        	List<String> rowEntries = new ArrayList<>();
	        	for(String column : DataTable.columnKeySet()){
	        		if(DataTable.get(id, column)==null) rowEntries.add("N/A");
	        		else rowEntries.add(DataTable.get(id, column));
	        	}
	        	csvwriter.writeNext(rowEntries.toArray(new String[rowEntries.size()]));
	        }
	        csvwriter.close();
	        logger.debug(FILENAME_COMBINED+" exporetd!");
        }


	 /**
     * Reads html file and extracts data from table tag and parse into guava table 
     *
     * @param path to html file
     * @return void
     * @throws IOException if error reading/writing from/to file
     *
     */
	public static void readFromHtml(String fileName) throws IOException {
		 Document doc = Jsoup.parse( new File(fileName), null);
	     Elements columns = doc.select("#directory tr th");
	     Elements td = doc.select("#directory tr td");
	     int sizeCols = columns.size();
	    
	     int idIndexHtml = -1;
	     Map<Integer, String> indexMapHtml = new HashMap<>();
	     //Assumption: ID column must exist and value must not be null in the html data table
	     
          for (int i = 0; i < sizeCols; i++){
	            if ("ID".equals(columns.get(i).text())){
	                idIndexHtml = i;
	            }
	            indexMapHtml.put(i, columns.get(i).text().toUpperCase());
	            
	      }   
	        
	     //Parse data from html table to guava table
	     for(int i = 0; i < td.size(); i += sizeCols){
	    	 int indexId = sizeCols*(i/sizeCols) + idIndexHtml;
	    	//Assuming ID contains only integer characters. NO other chars || spaces
		     String id = td.get(indexId).text();
	    	 
	    	 for (int j = i; j < i + sizeCols; j++) {
	    		 if (DataTable.get(id, indexMapHtml.get(j % sizeCols)) == null) {
	    			 DataTable.put(id, indexMapHtml.get(j % sizeCols), td.get(j).text());
                } 
	    		 else {
	    			 if (!DataTable.get(id, indexMapHtml.get(j % sizeCols)).equals(td.get(j).text())) {
	    				 logger.info(indexMapHtml.get(j % sizeCols)+" of "+id+" conflicts");
	    			 }
	    		 }
	    	}
	    } 
	   
	    logger.debug("Data parsed from html");
	}
	/**
     * Reads csv file and parse data into guava table 
     *
     * @param path to html file
     * @return void
     * @throws IOException if error reading/writing from/to file
     *
     */
	private static void readFromCsv(String fileName) throws IOException {
		CSVReader csvReader = new CSVReader(new FileReader(fileName),',','"',0);
		String[] nextLine;
		int sizeOfColumnsCsv=-1;
		int idIndexcsv = -1;
		Map<Integer, String> indexMapcsv = new HashMap<>();
		if ((nextLine = csvReader.readNext()) != null){
			sizeOfColumnsCsv = nextLine.length;
		}
		for (int i = 0; i < sizeOfColumnsCsv; i++){
			   //Assumption: ID column must exist and values must not be null in the data 
			if ("ID".equals(nextLine[i])) {
			idIndexcsv = i;
			}
		 indexMapcsv.put(i, nextLine[i].toUpperCase());
		}
		
		while ((nextLine = csvReader.readNext()) != null) {
			//Assuming ID contains only integer characters. NO other chars || spaces
			String id = nextLine[idIndexcsv];
			for (int i = 0; i < sizeOfColumnsCsv; i++) {
				 //check if value doesn't exist in table for given ID then add to data table 
				 if (DataTable.get(id, indexMapcsv.get(i)) == null) {
	                        DataTable.put(id, indexMapcsv.get(i), nextLine[i]);
	                    }
	 	        	 else
	        			 if (!DataTable.get(id, indexMapcsv.get(i)).equals(nextLine[i])) {
	        				 logger.info(indexMapcsv.get(i)+" of "+id +" conflicts");
	        			 }
	        }
	        	
	    }
	    csvReader.close();
	    logger.debug("Data parsed from CSV");
	}
}
