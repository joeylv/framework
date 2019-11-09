package com.ehsure.framework.generate;

/**
 * Created by jolv on 2017/5/11.
 */

public class FieldInfo {
    private String fieldType;
    private String fieldName;
    private String columnName;
    private String methodGetName;
    private String methodSetName;
    private boolean primaryKey;
    private boolean identityKey;

    public FieldInfo() {
    }

    public String getFieldType() {
        return this.fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getColumnName() {
        return this.columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getMethodGetName() {
        return this.methodGetName;
    }

    public void setMethodGetName(String methodGetName) {
        this.methodGetName = methodGetName;
    }

    public String getMethodSetName() {
        return this.methodSetName;
    }

    public void setMethodSetName(String methodSetName) {
        this.methodSetName = methodSetName;
    }

    public boolean isPrimaryKey() {
        return this.primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public boolean isIdentityKey() {
        return this.identityKey;
    }

    public void setIdentityKey(boolean identityKey) {
        this.identityKey = identityKey;
    }
}

