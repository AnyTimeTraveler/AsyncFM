package org.simonscode.asyncfm.gui.filebrowser;

import org.simonscode.asyncfm.data.Node;

public interface FileTreeUpdateListener {
    void onNewRootNode(Node rootNode);

    void onFileTreeUpdated();
}
