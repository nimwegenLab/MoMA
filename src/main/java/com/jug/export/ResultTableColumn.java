package com.jug.export;

import java.io.IOException;
import java.io.Writer;
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

    public void writeValue(int ind, Writer writer) throws IOException{
        writer.write(String.format("%s", getValue(ind)));
    }

    public void writeHeader(Writer writer) throws IOException {
        writer.write(String.format("%s", getHeader()));
    }

    public Integer getNumberOfEntries(){
        return values.size();
    }

    public String getHeader(){
        return header;
    }
}
