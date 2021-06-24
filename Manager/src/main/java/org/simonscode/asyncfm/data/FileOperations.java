package org.simonscode.asyncfm.data;

public class FileOperations {
    public void moveFile(Node node, Node newParent) {
        node.getParent().removeChild(node);
        newParent.addChild(node);
    }
}
