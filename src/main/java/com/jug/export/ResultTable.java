package com.jug.export;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for outputting data columns to a CSV file.
 * To be used in conjunction with {@link ResultTableColumn}, which holds
 * the values of individual columns.
 */
public class ResultTable {
    String separator;

    public ResultTable(String separator) {
        this.separator = separator;
    }

    List<ResultTableColumn> columnList = new ArrayList<>();

    /**
     * Write table to {@param writer}.
     *
     * @param writer output writer
     * @throws IOException thrown by {@param writer}
     */
    public void writeTable(Writer writer) throws IOException {
        writeHeader(writer);
        writeRows(writer);
        writer.flush();
    }

    /**
     * Add a column to this table.
     *
     * @param column to add
     * @param <T> value type contained in the column
     * @return a reference to the added column, so that we can keep it for adding values.
     */
    public <T> ResultTableColumn<T> addColumn(ResultTableColumn<T> column) {
        columnList.add(column);
        return column;
    }

    /**
     * Write header to the output writer.
     *
     * @param writer output writer
     * @throws IOException thrown by {@param writer}
     */
    private void writeHeader(Writer writer) throws IOException {
        for (ResultTableColumn column : columnList) {
            column.writeHeader(writer);
            if (!isLastColumn(column)) {
                writeSeparator(writer);
            }
        }
        writer.write("\n");
    }

    /**
     * Write table rows to {@param writer}
     *
     * @param writer output writer
     * @throws IOException thrown by writer
     */
    private void writeRows(Writer writer) throws IOException {
        int numberOfEntries = columnList.get(0).getNumberOfEntries();

        for (int ind = 0; ind < numberOfEntries; ind++) {
            for (ResultTableColumn column : columnList) {
                column.writeValue(ind, writer);
                if (!isLastColumn(column)) {
                    writeSeparator(writer);
                }
            }
            writer.write("\n");
        }
    }

    private boolean isLastColumn(ResultTableColumn column) {
        boolean isLastColumn = (columnList.indexOf(column) == (columnList.size() - 1));
        return isLastColumn;
    }

    /**
     * Write the column separator.
     *
     * @param writer output writer
     * @throws IOException thrown by writer
     */
    private void writeSeparator(Writer writer) throws IOException {
        writer.write(String.format("%s", separator));
    }
}

