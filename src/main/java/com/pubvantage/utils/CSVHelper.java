package com.pubvantage.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CSVHelper {
   private static Logger logger = Logger.getLogger(CSVHelper.class.getName());

    /**
     * write converted data to file
     * @param pathName path of file
     * @param data converted data
     * @param isAppend true: append, false: override
     */
    public static void writeNoHeader(String pathName, String[] data, boolean isAppend) {
        try {
            FileWriter writer = new FileWriter(pathName, isAppend);

            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);

            csvPrinter.printRecord(data);
            csvPrinter.flush();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * read data from file
     * @param pathName path of file
     * @return data in file
     */
    public static String[] read(String pathName) {
        String[] data = null;
        try {
            Reader reader = Files.newBufferedReader(Paths.get(pathName));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                data = new String[csvRecord.size()];
                for (int i = 0; i < data.length; i++) {
                    data[i] = csvRecord.get(i);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return data;
    }
}
