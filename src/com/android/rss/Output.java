package com.android.rss;

import android.annotation.SuppressLint;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Output {
    @SuppressLint({ "SdCardPath", "SdCardPath" })
    public static final void writeLog(String str){
        
        File f = new File("/sdcard/lockscreen");
        if(!f.exists()){
            f.mkdirs();
        }
        File outFile = new File("/sdcard/lockscreen/log.txt");
        if(!outFile.exists()){
            try {
                outFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileWriter fw = null;        
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(outFile, true);
            bw = new BufferedWriter(fw);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formatTime = sdf.format(new Date());
            bw.write(formatTime + " : " + str + "\n");
            bw.close();
            fw.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
