package com.firza.i42movfinder.model.util;

import android.content.Context;

import com.firza.i42movfinder.R;

import java.io.File;

public class Constant {

    public static final String BASE_URL = "https://api.themoviedb.org/";

    public static String getAppPath(Context context) {
        String path = context.getExternalFilesDir(null).getAbsolutePath()
                + File.separator
                + context.getResources().getString(R.string.app_name)
                + File.separator;

        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return path;
    }
}
