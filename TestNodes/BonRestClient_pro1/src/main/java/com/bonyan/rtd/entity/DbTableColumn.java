package com.bonyan.rtd.entity;

public class DbTableColumn {

    private String columnName;
    private String columnType;
    private Integer columnLimitSize;
    private Boolean isUnique;
    private Boolean isNullable;
    private Boolean isPrimaryKey;
    private String primaryKeyConstraint;
    private Boolean isForeignKey;
    private String foreignKeyConstraint;
    private Object defaultValue;
    private Object value;

    public DbTableColumn(String columnName, String columnType) {
        this.columnName = columnName;
        this.columnType = columnType;
    }

    public String getColumnName() {
        return columnName;
    }

    public DbTableColumn setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public String getColumnType() {
        return columnType;
    }

    public DbTableColumn setColumnType(String columnType) {
        this.columnType = columnType;
        return this;
    }

    public Integer getColumnLimitSize() {
        return columnLimitSize;
    }

    public DbTableColumn setColumnLimitSize(Integer columnLimitSize) {
        this.columnLimitSize = columnLimitSize;
        return this;
    }

    public Boolean getUnique() {
        return isUnique;
    }

    public DbTableColumn setUnique(Boolean unique) {
        isUnique = unique;
        return this;
    }

    public Boolean getNullable() {
        return isNullable;
    }

    public DbTableColumn setNullable(Boolean nullable) {
        isNullable = nullable;
        return this;
    }

    public Boolean getPrimaryKey() {
        return isPrimaryKey;
    }

    public DbTableColumn setPrimaryKey(Boolean primaryKey) {
        isPrimaryKey = primaryKey;
        return this;
    }

    public String getPrimaryKeyConstraint() {
        return primaryKeyConstraint;
    }

    public DbTableColumn setPrimaryKeyConstraint(String primaryKeyConstraint) {
        this.primaryKeyConstraint = primaryKeyConstraint;
        return this;
    }

    public Boolean getForeignKey() {
        return isForeignKey;
    }

    public DbTableColumn setForeignKey(Boolean foreignKey) {
        isForeignKey = foreignKey;
        return this;
    }

    public String getForeignKeyConstraint() {
        return foreignKeyConstraint;
    }

    public DbTableColumn setForeignKeyConstraint(String foreignKeyConstraint) {
        this.foreignKeyConstraint = foreignKeyConstraint;
        return this;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public DbTableColumn setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
