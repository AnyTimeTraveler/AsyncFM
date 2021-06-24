package org.simonscode.asyncfm.operations;

public abstract class Transaction {
    public abstract void execute();
    public abstract void undo();
    public abstract String toString();

    public abstract String getKind();
}
