package com.basic.core.model;

import java.io.Serializable;

/**
 * locate com.basic.core.model
 * Created by 79875 on 2017/11/6.
 */
public class InputSplit implements Serializable{
    private long start;
    private long end;
    private long length;

    public InputSplit(long start, long end, long length) {
        this.start = start;
        this.end = end;
        this.length = length;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return "InputSplit{" +
                "start=" + start +
                ", end=" + end +
                ", length=" + length +
                '}';
    }
}
