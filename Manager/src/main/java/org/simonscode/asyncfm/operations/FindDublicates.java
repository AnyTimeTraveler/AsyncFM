package org.simonscode.asyncfm.operations;

import org.simonscode.asyncfm.data.Node;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

public class FindDublicates extends Transaction {
    private final Node node;

    private Map<Integer, LinkedList<Node>> hashes;

    public FindDublicates(final Node node) {
        this.node = node;
        hashes = new HashMap<>();
        fillHashMap(node);
        hashes = hashes.entrySet()
                .stream()
                .filter(e -> e.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void execute() {
        setMarkRecursively(node, true);
    }

    private void fillHashMap(Node node) {
        final int hash = node.getHash();
        if (hashes.containsKey(hash)) {
            hashes.get(hash).add(node);
        } else {
            hashes.put(hash, new LinkedList<>(Collections.singleton(node)));
        }
        for (Node child : node.getChildren()) {
            fillHashMap(child);
        }
    }

    private void setMarkRecursively(Node node, boolean doMark) {
        for (Node child : node.getChildren()) {
            if (hashes.containsKey(child.getHash())) {
                child.setMarked(doMark);
            }
            setMarkRecursively(child, doMark);
        }
    }

    @Override
    public void undo() {
        setMarkRecursively(node, false);
    }

    @Override
    public String toString() {
        return null;
    }
}
