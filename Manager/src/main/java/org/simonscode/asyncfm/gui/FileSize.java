package org.simonscode.asyncfm.gui;

import java.util.Objects;

public class FileSize implements Comparable<FileSize> {

    private final long size;

    public FileSize(final long size) {
        this.size = size;
    }

    public long getRaw() {
        return size;
    }

    @Override
    public int compareTo(FileSize o) {
        return Long.compare(o.size, size);
    }

    public static String humanReadableByteCount(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), "kMGTPE".charAt(exp - 1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileSize fileSize = (FileSize) o;
        return size == fileSize.size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(size);
    }

    @Override
    public String toString() {
        return humanReadableByteCount(size);
    }
}
