package com.jug.export;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

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
    Map<String, ResultTableColumn> columnMap = new HashMap<>();
    Map<ResultTableColumn, Class<?>> columnTypeMap = new HashMap<>();

    /**
     * Write table to {@param file}.
     *
     * @param file file object to which we will writ
     * @throws IOException thrown by {@param writer}
     */
    public void writeToFile(File file) throws IOException {
        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
        this.writeTable(writer);
    }

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
        columnMap.put(column.getColumnHeader(), column);
        return column;
    }

    public <T> ResultTableColumn<T> getColumn(Class<T> type, String columnName) {
        if(columnMap.containsKey(columnName)){
            return columnMap.get(columnName);
        }
        ResultTableColumn<T> column = new ResultTableColumn<T>(columnName);
        columnList.add(column);
        columnMap.put(columnName, column);
        columnTypeMap.put(column, type);
        return column;
    }

    public Class<?> getColumnType(ResultTableColumn column) {
        return columnTypeMap.get(column);
    }

//    public <T> void addValue(T value, String columnHeader) {
//        ResultTableColumn column = columnMap.get(columnHeader);
//        if(!value.getClass().isAssignableFrom(getColumnType(column))){
//            throw new RuntimeException(String.format("Provided value type (%s) does not match value-type of the target column (%s)", value.getClass(), getColumnType(column)));
//        }
//        if (isNull(column)) {
//            throw new RuntimeException("specified column name does not exist");
//        }
//        column.addValue(value);
//    }

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
        writeEndOfLine(writer);
    }

    /**
     * Write end-of-row specifier according to CSV format, which consists of a carriage return (CR) and a linefeed (LF),
     * i.e. CRLF according to:
     * https://www.loc.gov/preservation/digital/formats/fdd/fdd000323.shtml.
     * https://stackoverflow.com/a/13821601/653770
     *
     * @param writer
     * @throws IOException
     */
    private void writeEndOfLine(Writer writer) throws IOException {
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
            writeEndOfLine(writer);
        }
    }

    /**
     * Returns if this is the last column in the table.
     *
     * @param column
     * @return
     */
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

