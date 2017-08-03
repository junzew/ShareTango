package com.imran.wali.sharetango.Utility;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

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

    public static String encodeBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bytes = baos.toByteArray();
        String encoded = Base64.encodeToString(bytes, Base64.DEFAULT);
        return encoded;
    }

    public static void decodeBitmapIntoImageView(String encoded, ImageView image) {
        if (encoded == null) return;
        byte[] imageAsBytes = Base64.decode(encoded, Base64.DEFAULT);
        image.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
    }
}
