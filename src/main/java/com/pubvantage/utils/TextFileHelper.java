package com.pubvantage.utils;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class TextFileHelper {
    private static Logger logger = Logger.getLogger(TextFileHelper.class.getName());

    /**
     * write data to text file
     *
     * @param pathName path need to save in
     * @param isAppend true: append, false: override
     * @param data     data need to be saved
     */
    public static void write(String pathName, boolean isAppend, String[] data) {

        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            StringBuilder stringBuilder = new StringBuilder();
            if (data != null && data.length > 0) {
                //objective
                if(data[0].isEmpty()){
                    data[0]= "0.0";
                }
                stringBuilder.append(ConvertUtil.convertObjectToDecimal(data[0])).append(" ");

                for (int i = 1; i < data.length; i++) {
                    if(data[i].isEmpty()){
                        data[i]= "0.0";
                    }
                    stringBuilder.append(i).append(":").append(ConvertUtil.convertObjectToDecimal(data[i])).append(" ");
                }
            }
            StringBuilder stringBuilder1 = new StringBuilder(stringBuilder.toString().trim()).append("\n");


            fw = new FileWriter(pathName, isAppend);

            bw = new BufferedWriter(fw);
            bw.write(stringBuilder1.toString());



            final File file = new File(pathName);
            file.setReadable(true, false);
            file.setExecutable(true, false);
            file.setWritable(true, false);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (bw != null)
                    bw.close();

                if (fw != null)
                    fw.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
