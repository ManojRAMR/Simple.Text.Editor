/*
 * Copyright (c) 2021 Manoj Randeni. All rights reserved.
 * Licensed under the Apache license. See License.txt in the project root for license information
 */

package util;

public class Index {
    private int startIndex;
    private int endIndex;

    public Index() {
    }

    public Index(int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    @Override
    public String toString() {
        return "Index{" +
                "startIndex=" + startIndex +
                ", endIndex=" + endIndex +
                '}';
    }
}
