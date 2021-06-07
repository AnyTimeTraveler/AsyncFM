package org.simonscode.asyncfm.data.headers;

import java.io.DataInputStream;
import java.io.IOException;

import static org.simonscode.asyncfm.data.NodeWalker.readString;

public class HeaderV1 extends Header {
    public HeaderV1(DataInputStream dis) throws IOException {
        super(1);
        flags = dis.readByte();
        entries = dis.readLong();
        basePath = readString(dis);
        System.out.println(basePath);
    }
}
