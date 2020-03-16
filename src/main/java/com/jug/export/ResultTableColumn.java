package com.jug.export;

import java.util.ArrayList;
import java.util.List;

public class ResultTableColumn<T> {
    private List<T> values  = new ArrayList<T>();
    private String header;

    public ResultTableColumn(String header) {
        this.header = header;
    }

    public void addValue(T value){
        values.add(value);
    }

    public String getValue(int ind){
        return values.get(ind).toString();
    }

    public Integer getNumberOfEntries(){
        return values.size();
    }

    public String getHeader(){
        return header;
    }
}
