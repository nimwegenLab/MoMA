package com.jug.export;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class ResultTableTest {
//    @Test
//    public void addValue__for_incorrect_value_type__throws_RuntimeException() {
//        String expected = "some_string";
////        ResultTableColumn<Integer> column = new ResultTableColumn<>("column_name");
//
//        ResultTable table = new ResultTable(",");
//        ResultTableColumn<Double> column = table.getColumn(Double.class, "column_name");
//        table.getColumn(Double.class, "column_name").addValue();
//        RuntimeException error = Assertions.assertThrows(RuntimeException.class, () -> table.addValue(expected, "column_name"));
//        Assertions.assertEquals("specified column name does not exist", error.getMessage());
//    }
//
//    @Test
//    public void addValue__for_non_existing_column__throws_RuntimeException() {
//        Integer expected = 1;
//        ResultTableColumn<Integer> column = new ResultTableColumn<>("column_name");
//
//        ResultTable table = new ResultTable(",");
//        table.addColumn(column);
//
//        RuntimeException error = Assertions.assertThrows(RuntimeException.class, () -> table.addValue(expected, "INCORRECT_COLUMN_NAME"));
//        Assertions.assertEquals("specified column name does not exist", error.getMessage());
//    }
//
//    @Test
//    public void addValue__for_column_with_string_type__adds_value_correctly() {
//        String expected = "test string";
//        ResultTableColumn<String> column = new ResultTableColumn<>("column_name");
//
//        ResultTable table = new ResultTable(",");
//        table.addColumn(column);
//
//        table.addValue(expected, "column_name");
//        String actual = column.getValue(0);
//        Assertions.assertEquals(expected, actual);
//    }
//
//    @Test
//    public void addValue__for_column_with_integer_type__adds_value_correctly() {
//        int expected = 111;
//        ResultTableColumn<Integer> column = new ResultTableColumn<>("column_name");
//
//        ResultTable table = new ResultTable(",");
//        table.addColumn(column);
//
//        table.addValue(expected, "column_name");
//        Integer actual = column.getValue(0);
//        Assertions.assertEquals(expected, actual, 1e-6);
//    }
//
//    @Test
//    public void addValue__for_column_with_double_type__adds_value_correctly() {
//        double expected = 1.2345;
//        ResultTableColumn<Double> column = new ResultTableColumn<>("column_name");
//
//        ResultTable table = new ResultTable(",");
//        table.addColumn(column);
//
//        table.addValue(expected, "column_name");
//        Double actual = column.getValue(0);
//        Assertions.assertEquals(expected, actual, 1e-6);
//    }

    @Test
    public void tablePrintsCorrectly() throws IOException {
        ResultTableColumn<Double> columnDouble = new ResultTableColumn<>("DoubleColumn");
        ResultTableColumn<Integer> columnInteger = new ResultTableColumn<>("IntegerColumn");
        ResultTableColumn<String> columnString = new ResultTableColumn<>("StringColumn");

        ResultTable table = new ResultTable(",");

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