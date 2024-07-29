package com.bonyan.rtd.service;

import com.bonyan.rtd.entity.DbTableColumn;
import com.comptel.mc.node.database.DataSourceFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

public class TimesTenDbWriterService {

    private static final String CHECK_TABLE_EXIST_QUERY = "SELECT 1 FROM {table-name}";
    private static final String CREATE_TABLE_QUERY = "CREATE TABLE {table-name} ({table-columns})";
    private static final String INSERT_RECORD_QUERY = "INSERT INTO TABLE {table-name} ({table-columns}) VALUES ({table-values})";


    private DataSource dataSource;
    private Connection connection;


    TimesTenDbWriterService(String username, String password, String datasourceName, String dbDriverName) {
        this.dataSource = DataSourceFactory.getInstance().getDataSource(username, password, datasourceName, dbDriverName);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    //    Todo complete method
    public void tableCreate(String tableName, Collection<DbTableColumn> columns) throws SQLException {
        StringBuilder columnsDefinition = new StringBuilder();
        for (DbTableColumn column : columns) {
            columnsDefinition.append(column.getColumnName()).append(" ").append(column.getColumnType().toString());
        }
        connection.commit();
    }

    public String insertRecord(String tableName, Collection<DbTableColumn> columns) throws SQLException {
        StringBuilder columnsDefinition = new StringBuilder();
        StringBuilder valuesDefinition = new StringBuilder();

        for (DbTableColumn column : columns) {
            columnsDefinition.append(column.getColumnName()).append(",");
            if ("VARCHAR".equals(column.getColumnType())) {
                valuesDefinition.append("'").append(column.getValue()).append("',");
            } else {
                valuesDefinition.append(column.getValue()).append(",");
            }
        }
        columnsDefinition.deleteCharAt(columnsDefinition.lastIndexOf(","));
        valuesDefinition.deleteCharAt(valuesDefinition.lastIndexOf(","));
        String insertQuery = INSERT_RECORD_QUERY.replace("{table-name}", tableName)
                .replace("{table-columns}", columnsDefinition)
                .replace("{table-values}", valuesDefinition);
        return connection.nativeSQL(insertQuery);
    }

}
