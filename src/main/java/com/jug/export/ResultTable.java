package com.jug.export;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class ResultTable {
    List<ResultTableColumn> columnList = new ArrayList<>();

    public void print(Writer writer) throws IOException {
        printHeader(writer);
        printRows(writer);
        writer.flush();
    }

    public <T> ResultTableColumn<T> addColumn(ResultTableColumn<T> column) {
        columnList.add(column);
        return column;
    }

    private void printHeader(Writer writer) throws IOException {
        for (ResultTableColumn column : columnList) {
            writer.write(String.format("%s\t", column.getHeader()));
        }
        writer.write("\n");
    }

    private void printRows(Writer writer) throws IOException {
        int numberOfEntries = columnList.get(0).getNumberOfEntries();

        for (int ind = 0; ind < numberOfEntries; ind++) {
            for (ResultTableColumn column : columnList) {
                writer.write(String.format("%s;\t", column.getValue(ind)));
            }
            writer.write("\n");
        }
    }
}

