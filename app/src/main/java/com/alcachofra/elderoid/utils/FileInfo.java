package com.alcachofra.elderoid.utils;

import java.util.Objects;

public class FileInfo implements Comparable<FileInfo> {
    private final String path;
    private final long time;

    /**
     * Constructor of FileInfo.
     * @param path Path of file.
     * @param time Time file was created.
     */
    public FileInfo(String path, long time) {
        this.path = path;
        this.time = time;
    }

    /**
     * Get File path.
     * @return String containing path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Get time this File was created.
     * @return Long containing time since epoch (00:00:00 GMT, January 1, 1970).
     */
    public long getTime() {
        return time;
    }

    /**
     * Compares this FileInfo to another FileInfo (sorting environment). Compares time.
     * @param o Another FileInfo.
     * @return > 0 if more recent, < 0 if older, 0 if same.
     */
    @Override
    public int compareTo(FileInfo o) {
        long tmp = getTime() - o.getTime();
        if (tmp > 0) return 1;
        if (tmp < 0) return -1;
        return 0;
    }

    /**
     * Compares this FileInfo to another FileInfo (use to distinguish two FileInfo). Compares path and time.
     * @param o Another FileInfo.
     * @return True if the same.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileInfo fileInfo = (FileInfo) o;
        return time == fileInfo.time &&
                Objects.equals(path, fileInfo.path);
    }

    /**
     * Returns a hash code for this object.
     * @return int a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(path, time);
    }

    /**
     * String value of this FileInfo.
     * @return String containing this FileInfo's information.
     */
    @Override
    public String toString() {
        return "FileInfo{" +
                "path='" + path + '\'' +
                ", time=" + time +
                '}';
    }
}
