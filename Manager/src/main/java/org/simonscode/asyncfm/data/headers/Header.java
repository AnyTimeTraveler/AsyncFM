package org.simonscode.asyncfm.data.headers;

import java.io.DataInputStream;
import java.io.IOException;

public class Header {

    int version;
    byte flags;
    long entries;
    String basePath;

    Header(int version) {
        this.version = version;
    }

    public static Header fromBytes(DataInputStream dis) throws IOException {
        int version = dis.readInt();

        switch (version) {
            case 1:
                return new HeaderV1(dis);
            default:
                throw new IOException("Unknown version!");
        }
    }

    public int getVersion() {
        return version;
    }

    public byte getFlags() {
        return flags;
    }

    public long getEntries() {
        return entries;
    }

    public String getBasePath() {
        return basePath;
    }
}
