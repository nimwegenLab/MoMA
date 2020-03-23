package com.jug.export;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class ResultTable {
    List<ResultTableColumn> columnList = new ArrayList<>();
    String separator = ";";

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
            column.writeHeader(writer);
            writeSeparator(writer);
        }
        writer.write("\n");
    }

    private void printRows(Writer writer) throws IOException {
        int numberOfEntries = columnList.get(0).getNumberOfEntries();

        for (int ind = 0; ind < numberOfEntries; ind++) {
            for (ResultTableColumn column : columnList) {
                column.writeValue(ind, writer);
                writeSeparator(writer);
            }
            writer.write("\n");
        }
    }

    private void writeSeparator(Writer writer) throws IOException {
        writer.write(String.format("%s", separator));
    }
}

