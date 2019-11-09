package com.ehsure.framework.generate;

/**
 * Created by jolv on 2017/5/11.
 */

import java.util.ArrayList;
import java.util.List;

public class TableInfo {
    private String tableName;
    private String entityName;
    private String className;
    private List<FieldInfo> fieldInfoList = new ArrayList();
    private boolean hasDate = false;
    private boolean hasDecimal = false;
    private String primaryKeyField;

    public TableInfo() {
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getEntityName() {
        return this.entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<FieldInfo> getFieldInfoList() {
        return this.fieldInfoList;
    }

    public void setFieldInfoList(List<FieldInfo> fieldInfoList) {
        this.fieldInfoList = fieldInfoList;
    }

    public boolean getHasDate() {
        return this.hasDate;
    }

    public void setHasDate(boolean hasDate) {
        this.hasDate = hasDate;
    }

    public boolean getHasDecimal() {
        return this.hasDecimal;
    }

    public void setHasDecimal(boolean hasDecimal) {
        this.hasDecimal = hasDecimal;
    }

    public String getPrimaryKeyField() {
        return this.primaryKeyField;
    }

    public void setPrimaryKeyField(String primaryKeyField) {
        this.primaryKeyField = primaryKeyField;
    }
}
