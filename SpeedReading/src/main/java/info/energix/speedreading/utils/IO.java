package info.energix.speedreading.utils;

import java.io.File;
import java.io.FileFilter;

public class IO {
    public static File[] listFiles(String path, Boolean onlyDirectories){
        if(onlyDirectories) {
            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            };
            return new File(path).listFiles(filter);
        } else {
            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile();
                }
            };
            return new File(path).listFiles(filter);
        }
    }
}
