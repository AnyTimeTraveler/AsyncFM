package org.simonscode.asyncfm.common;

import java.io.File;

public class RootNode extends Node {
    private final long freeSpace;

    public RootNode(File file) {
        super(null, file);
        freeSpace = file.getFreeSpace();
    }
}
