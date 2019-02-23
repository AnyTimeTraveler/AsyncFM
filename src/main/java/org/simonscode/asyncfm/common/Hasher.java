package org.simonscode.asyncfm.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.CRC32;

public class Hasher {
    private static final CRC32 crc = new CRC32();
    private static byte[] buf = new byte[1024];

    public static long hash(File file) {
        long hash = 0L;
        crc.reset();
        try {
            FileInputStream fis = new FileInputStream(file);

            int i;
            while ((i = fis.read(buf)) != -1) {
                crc.update(buf, 0, i);
            }
            hash = crc.getValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hash;
    }
}
