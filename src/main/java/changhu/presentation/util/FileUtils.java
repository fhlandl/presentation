package changhu.presentation.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Slf4j
public class FileUtils {

    private FileUtils() {
    }

    public static void createDirectory(String path) {
        File directory = new File(path);
        if (directory.mkdirs()) {
            log.info("Directory created successfully");
        } else {
            log.error("Directory creation failed");
        }
    }

    public static void deleteDirectory(String path) {
        File dir = new File(path);

        if (dir.exists()) {
            File[] subDirs = dir.listFiles();
            for (File subDir : subDirs) {
                if (!subDir.isFile()) {
                    deleteDirectory(subDir.getPath());
                }
                subDir.delete();
            }
        }
        dir.delete();
    }

    public static byte[] getFileAsByteArray(String path) throws IOException {
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);
        return IOUtils.toByteArray(fileInputStream);
    }
}
