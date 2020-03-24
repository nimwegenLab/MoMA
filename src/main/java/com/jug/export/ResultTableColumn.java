package com.jug.export;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * This class is for usage in conjunction with {@link ResultTable}.
 * It hold the header and values of a column and formats them upon output.
 *
 * @param <T> value type stored in the column.
 */
public class ResultTableColumn<T> {
    /**
     * Column values.
     */
    private List<T> values  = new ArrayList<T>();

    /**
     * Column header.
     */
    private String columnHeader;

    /**
     * Lambda holding the formatter for formatting the value upon string output.
     */
    private Function<T, String> outputStringFormatter;

    /**
     * Construct with the provided column header.
     *
     * @param columnHeader column header for output.
     */
    public ResultTableColumn(String columnHeader) {
        this.columnHeader = columnHeader;
        outputStringFormatter = (T input) -> input.toString();
    }

    /**
     * Construct with the provided column header and .
     *
     * @param columnHeader column header for output.
     * @param valueFormat format of the values during output. Must match type {@link T}.
     */
    public ResultTableColumn(String columnHeader, String valueFormat) {
        this.columnHeader = columnHeader;
        outputStringFormatter = (T input) -> String.format(valueFormat, input);
    }

    /**
     * Add a value to the column.
     *
     * @param value to add.
     */
    public void addValue(T value){
        values.add(value);
    }

    /**
     * Get the column value at position {@param ind}.
     *
     * @param ind index of value to return
     * @return value of {@link values} at {@param ind}.
     */
    private T getValue(int ind){
        return values.get(ind);
    }

    /**
     * Write the value at position {@param ind} to {@param writer}.
     *
     * @param ind index of value to write
     * @param writer output writer
     * @throws IOException thrown by {@param writer}
     */
    public void writeValue(int ind, Writer writer) throws IOException{
        writer.write(outputStringFormatter.apply(getValue(ind)));
    }

    /**
     * Write column header to {@param writer}.
     *
     * @param writer output writer
     * @throws IOException thrown by {@param writer}
     */
    public void writeHeader(Writer writer) throws IOException {
        writer.write(String.format("%s", getColumnHeader()));
    }

    /**
     * Get number of values in the column.
     *
     * @return number of values
     */
    public Integer getNumberOfEntries(){
        return values.size();
    }

    /**
     * Get column {@link columnHeader}.
     *
     * @return header
     */
    private String getColumnHeader(){
        return columnHeader;
    }
}
