package com.jug.export;

import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class ResultTableTest {
    @Test
    public void tablePrintsCorrectly() throws IOException {
        ResultTableColumn<Double> columnDouble = new ResultTableColumn<>("DoubleColumn");
        ResultTableColumn<Integer> columnInteger = new ResultTableColumn<>("IntegerColumn");
        ResultTableColumn<String> columnString = new ResultTableColumn<>("StringColumn");

        ResultTable table = new ResultTable();

        columnDouble.addValue(1.2345);
        columnDouble.addValue(2.3456);

        columnInteger.addValue(1);
        columnInteger.addValue(2);

        columnString.addValue("String 1");
        columnString.addValue("String 2");

        table.addColumn(columnDouble);
        table.addColumn(columnInteger);
        table.addColumn(columnString);

        Writer writer = new PrintWriter(System.out);
        table.writeTable(writer);
    }
}