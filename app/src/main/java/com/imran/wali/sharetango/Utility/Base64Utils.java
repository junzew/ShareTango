package com.imran.wali.sharetango.Utility;

import android.util.Base64;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;


/**
 * Created by junze on 2017-04-30.
 */

public class Base64Utils {
    public static String toBase64(File file) throws IOException {
        byte bytes[] = FileUtils.readFileToByteArray(file);
        String encoded = Base64.encodeToString(bytes,Base64.DEFAULT);
        return encoded;
    }
    public static void decode(String encodedBase64, String path, String fileName) throws IOException {
        byte[] bytes = Base64.decode(encodedBase64, Base64.DEFAULT);
        FileUtils.writeByteArrayToFile(new File(path, fileName), bytes);
    }
}
