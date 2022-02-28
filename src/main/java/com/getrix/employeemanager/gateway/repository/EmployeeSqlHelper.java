package com.getrix.employeemanager.gateway.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class EmployeeSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("first_name", table, columnPrefix + "_first_name"));
        columns.add(Column.aliased("last_name", table, columnPrefix + "_last_name"));
        columns.add(Column.aliased("date_of_hiring", table, columnPrefix + "_date_of_hiring"));
        columns.add(Column.aliased("salary", table, columnPrefix + "_salary"));

        columns.add(Column.aliased("department_id", table, columnPrefix + "_department_id"));
        return columns;
    }
}
