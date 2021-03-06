package com.example.james.applepay;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Class to write to a file
 */
public class AndroidFile {

    private static File file;
    private Boolean fileCreated;

    /**
     * Creates the new file
     * @param fileLocation The location and name of the file
     */
    public AndroidFile(String fileLocation) {
        file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_NOTIFICATIONS), fileLocation);
        try {
            file.createNewFile();
        } catch (IOException e) {
            System.out.println("File not created");
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public Boolean write(String toWrite) {
        try {
            FileOutputStream outputStream = new FileOutputStream(file, true);
            toWrite += "\n\n";
            outputStream.write(toWrite.getBytes());
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
