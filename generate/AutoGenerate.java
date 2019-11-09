package com.ehsure.framework.generate;

/**
 * Created by jolv on 2017/5/11.
 */

import com.ehsure.ttp.eplatform.core.util.StringUtil;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.ClasspathResourceLoader;

import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class AutoGenerate {
    private static String projectAbsolutePath;
    private static final String defaultTemplatePath = "/WEB-INF/template";
    private static final String defaultWebResourcePath = "/src/main/java/com/ehsure/generate/resources";
    private static final String defaultTargetRootPath = "/src/main/java";
    private static final String defaultTargetWebAppPath = "/src/main/webapp/";
    private static final String modelTemplatePath = "/model/model.btl";
    private static final String daoTemplatePath = "/dao/dao.btl";
    private static final String daoImplTemplatePath = "/dao/daoImpl.btl";
    private static final String serviceTemplatePath = "/service/service.btl";
    private static final String serviceImplTemplatePath = "/service/serviceImpl.btl";
    private static final String controllerTemplatePath = "/controller/controller.btl";
    private static final String angularAppTemplatePath = "/webapp/app/app.btl";
    private static final String angularConstantTemplatePath = "/webapp/constant/constant.btl";
    private static final String angularControllerTemplatePath = "/webapp/controller/controller.btl";
    private static final String angularViewListTemplatePath = "/webapp/view/list.btl";
    private static final String angularViewAddTemplatePath = "/webapp/view/add.btl";
    private static final String angularViewUpdateTemplatePath = "/webapp/view/update.btl";
    private static final String targetAngularAppPath = "/scripts/app";
    private static final String targetAngularConstantPath = "/scripts/constant";
    private static final String targetAngularControllerPath = "/scripts/controllers";
    private static final String targetAngularViewPath = "/views/";
    private static GroupTemplate groupTemplate = null;
    private String templatePath;
    private String webResourcePath;
    private String targetRootPath;
    private String targetWebAppPath;
    private String basePackage;
    private Connection connection;
    private boolean copyWebResource;

    public AutoGenerate(String jdbcIp, String jdbcPort, String dbName, String jdbcUserName, String jdbcPwd) {
        this(jdbcIp, jdbcPort, dbName, jdbcUserName, jdbcPwd, (String)null);
    }

    public AutoGenerate(String jdbcIp, String jdbcPort, String dbName, String jdbcUserName, String jdbcPwd, String templateModuleName) {
        this.templatePath = null;
        this.webResourcePath = null;
        this.targetRootPath = null;
        this.targetWebAppPath = null;
        this.copyWebResource = false;
        this.targetRootPath = "/src/main/java";
        this.targetWebAppPath = "/src/main/webapp/";
        if(StringUtil.isBlank(templateModuleName)) {
            this.templatePath = "/WEB-INF/template";
        } else {
            this.templatePath = "/" + templateModuleName + "/WEB-INF/template";
        }
        System.out.println(this.templatePath);

        if(StringUtil.isBlank(templateModuleName)) {
            this.webResourcePath = "/src/main/java/com/ehsure/generate/resources";
        } else {
            this.webResourcePath = "/" + templateModuleName + "/src/main/java/com/ehsure/generate/resources";
        }

        String url = "jdbc:mysql://" + jdbcIp + ":" + jdbcPort + "/" + dbName + "?user=" + jdbcUserName + "&password=" + jdbcPwd + "&useUnicode=true&characterEncoding=UTF-8";

        try {
            this.connection = DriverManager.getConnection(url);
        } catch (Exception var12) {
            var12.printStackTrace();
        }

        try {
            File directory = new File("");
            projectAbsolutePath = directory.getCanonicalPath();
            ClasspathResourceLoader classpathResourceLoader = new ClasspathResourceLoader("/WEB-INF/template", "utf-8");
            Configuration cfg = Configuration.defaultConfiguration();
            groupTemplate = new GroupTemplate(classpathResourceLoader, cfg);
        } catch (Exception var11) {
            var11.printStackTrace();
        }

    }

    public void generateCode(String... tableNames) {
        List<String> tableList = new ArrayList();
        List<TableInfo> tableInfoList = new ArrayList();
        if(tableNames != null && tableNames.length != 0) {
            for(int i = 0; i < tableNames.length; ++i) {
                try {
                    ResultSet resultSet = this.connection.getMetaData().getTables((String)null, (String)null, tableNames[i], new String[]{"TABLE"});

                    while(resultSet.next()) {
                        tableList.add(resultSet.getString("TABLE_NAME"));
                    }
                } catch (Exception var7) {
                    var7.printStackTrace();
                }
            }
        } else {
            try {
                ResultSet resultSet = this.connection.getMetaData().getTables((String)null, (String)null, (String)null, new String[]{"TABLE"});

                while(resultSet.next()) {
                    tableList.add(resultSet.getString("TABLE_NAME"));
                }
            } catch (Exception var8) {
                var8.printStackTrace();
            }
        }

        Iterator i$ = tableList.iterator();

        while(i$.hasNext()) {
            String name = (String)i$.next();
            TableInfo tableInfo = this.resolveTable(name);
            tableInfoList.add(tableInfo);
        }

        i$ = tableInfoList.iterator();

        while(i$.hasNext()) {
            TableInfo tableInfo = (TableInfo)i$.next();
            this.generateModel(tableInfo);
            this.generateDao(tableInfo);
            this.generateDaoImpl(tableInfo);
            this.generateService(tableInfo);
            this.generateServiceImpl(tableInfo);
            this.generateController(tableInfo);
        }

        if(this.copyWebResource) {
            this.copyWebResources();
        }

        this.generateAngularConstant(tableInfoList);
        this.generateAngularController(tableInfoList);
        this.generateAngularView(tableInfoList);
    }

    private void generateModel(TableInfo tableInfo) {
        Template t = groupTemplate.getTemplate("/model/model.btl");
        String modelPackage = this.getBasePackage() + ".model";
        t.binding("modelPkg", modelPackage);
        t.binding("tableInfo", tableInfo);
        String str = t.render();
        this.generateFile(modelPackage, tableInfo.getClassName() + ".java", str);
    }

    private void generateDao(TableInfo tableInfo) {
        Template t = groupTemplate.getTemplate("/dao/dao.btl");
        String daoPkg = this.getBasePackage() + ".dao";
        t.binding("modelPkg", this.getBasePackage() + ".model");
        t.binding("daoPkg", daoPkg);
        t.binding("daoImplPkg", this.getBasePackage() + ".dao.impl");
        t.binding("tableInfo", tableInfo);
        String str = t.render();
        this.generateFile(daoPkg, tableInfo.getClassName() + "Dao.java", str);
    }

    private void generateDaoImpl(TableInfo tableInfo) {
        Template t = groupTemplate.getTemplate("/dao/daoImpl.btl");
        String daoImplPkg = this.getBasePackage() + ".dao.impl";
        t.binding("modelPkg", this.getBasePackage() + ".model");
        t.binding("daoPkg", this.getBasePackage() + ".dao");
        t.binding("daoImplPkg", daoImplPkg);
        t.binding("tableInfo", tableInfo);
        String str = t.render();
        this.generateFile(daoImplPkg, tableInfo.getClassName() + "DaoImpl.java", str);
    }

    private void generateService(TableInfo tableInfo) {
        Template t = groupTemplate.getTemplate("/service/service.btl");
        String servicePkg = this.getBasePackage() + ".service";
        t.binding("modelPkg", this.getBasePackage() + ".model");
        t.binding("daoPkg", this.getBasePackage() + ".dao");
        t.binding("servicePkg", servicePkg);
        t.binding("tableInfo", tableInfo);
        String str = t.render();
        this.generateFile(servicePkg, tableInfo.getClassName() + "Service.java", str);
    }

    private void generateServiceImpl(TableInfo tableInfo) {
        Template t = groupTemplate.getTemplate("/service/serviceImpl.btl");
        String serviceImplPkg = this.getBasePackage() + ".service.impl";
        t.binding("modelPkg", this.getBasePackage() + ".model");
        t.binding("daoPkg", this.getBasePackage() + ".dao");
        t.binding("servicePkg", this.getBasePackage() + ".service");
        t.binding("serviceImplPkg", serviceImplPkg);
        t.binding("tableInfo", tableInfo);
        String str = t.render();
        this.generateFile(serviceImplPkg, tableInfo.getClassName() + "ServiceImpl.java", str);
    }

    private void generateController(TableInfo tableInfo) {
        Template t = groupTemplate.getTemplate("/controller/controller.btl");
        String controllerPkg = this.getBasePackage() + ".controller";
        t.binding("modelPkg", this.getBasePackage() + ".model");
        t.binding("daoPkg", this.getBasePackage() + ".dao");
        t.binding("servicePkg", this.getBasePackage() + ".service");
        t.binding("controllerPkg", controllerPkg);
        t.binding("tableInfo", tableInfo);
        String str = t.render();
        this.generateFile(controllerPkg, tableInfo.getClassName() + "Controller.java", str);
    }

    private void generateAngularApp(List<TableInfo> tableInfoList) {
        Iterator i$ = tableInfoList.iterator();

        while(i$.hasNext()) {
            TableInfo tableInfo = (TableInfo)i$.next();
            Template t = groupTemplate.getTemplate("/webapp/app/app.btl");
            t.binding("tableInfo", tableInfo);
            String str = t.render();
            this.generateResourceFile(projectAbsolutePath + this.targetWebAppPath + "/scripts/app", tableInfo.getEntityName() + ".js", str);
        }

    }

    private void generateAngularConstant(List<TableInfo> tableInfoList) {
        Template t = groupTemplate.getTemplate("/webapp/constant/constant.btl");
        t.binding("tableInfoList", tableInfoList);
        String str = t.render();
        this.generateResourceFile(projectAbsolutePath + this.targetWebAppPath + "/scripts/constant", "urlConstant.js", str);
    }

    private void generateAngularController(List<TableInfo> tableInfoList) {
        Iterator i$ = tableInfoList.iterator();

        while(i$.hasNext()) {
            TableInfo tableInfo = (TableInfo)i$.next();
            Template t = groupTemplate.getTemplate("/webapp/controller/controller.btl");
            t.binding("tableInfo", tableInfo);
            String str = t.render();
            this.generateResourceFile(projectAbsolutePath + this.targetWebAppPath + "/scripts/controllers", tableInfo.getEntityName() + "Controller.js", str);
        }

    }

    private void generateAngularView(List<TableInfo> tableInfoList) {
        Iterator i$ = tableInfoList.iterator();

        while(i$.hasNext()) {
            TableInfo tableInfo = (TableInfo)i$.next();
            String modulePath = projectAbsolutePath + this.targetWebAppPath + "/views/" + tableInfo.getEntityName() + File.separator;
            File moduleDirectory = new File(modulePath);
            if(!moduleDirectory.exists()) {
                moduleDirectory.mkdirs();
            }

            Template t = groupTemplate.getTemplate("/webapp/view/list.btl");
            t.binding("tableInfo", tableInfo);
            String str = t.render();
            this.generateResourceFile(modulePath, tableInfo.getEntityName() + "List.html", str);
            t = groupTemplate.getTemplate("/webapp/view/add.btl");
            t.binding("tableInfo", tableInfo);
            str = t.render();
            this.generateResourceFile(modulePath, tableInfo.getEntityName() + "Add.html", str);
            t = groupTemplate.getTemplate("/webapp/view/update.btl");
            t.binding("tableInfo", tableInfo);
            str = t.render();
            this.generateResourceFile(modulePath, tableInfo.getEntityName() + "Update.html", str);
        }

    }

    private String convertFilePath(String pkgName) {
        String filePath = pkgName.replace(".", "/");
        return projectAbsolutePath + this.targetRootPath + "/" + filePath;
    }

    private void generateFile(String pkgName, String fileName, String content) {
        String filePath = this.convertFilePath(pkgName);
        File file = new File(filePath);
        if(!file.exists()) {
            file.mkdirs();
        }

        file = new File(filePath + File.separator + fileName);

        try {
            if(file.exists()) {
                return;
            }

            file.createNewFile();
        } catch (Exception var8) {
            var8.printStackTrace();
            return;
        }

        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(content, 0, content.length());
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception var7) {
            var7.printStackTrace();
        }

    }

    private void generateResourceFile(String filePath, String fileName, String content) {
        File file = new File(filePath);
        if(!file.exists()) {
            file.mkdirs();
        }

        file = new File(filePath + File.separator + fileName);

        try {
            if(file.exists()) {
                return;
            }

            file.createNewFile();
        } catch (Exception var7) {
            var7.printStackTrace();
            return;
        }

        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(content, 0, content.length());
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }

    public TableInfo resolveTable(String tableName) {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setTableName(tableName);
        int _i = tableInfo.getTableName().indexOf("_");
        String s = null;
        if(_i != -1 && _i != 0 && _i != tableInfo.getTableName().length() - 1) {
            s = this.convertUnderLine(tableInfo.getTableName());
        } else {
            s = tableInfo.getTableName().toLowerCase();
        }

        tableInfo.setClassName(s.substring(0, 1).toUpperCase() + s.substring(1));
        tableInfo.setEntityName(tableInfo.getClassName().substring(0, 1).toLowerCase() + tableInfo.getClassName().substring(1));
        ArrayList list = new ArrayList();

        try {
            DatabaseMetaData databaseMetaData = this.connection.getMetaData();
            ResultSet resultSet = databaseMetaData.getPrimaryKeys((String)null, (String)null, tableInfo.getTableName());

            String primaryKey;
            for(primaryKey = null; resultSet.next(); primaryKey = resultSet.getString("COLUMN_NAME")) {
                ;
            }

            resultSet = databaseMetaData.getColumns((String)null, (String)null, tableInfo.getTableName(), (String)null);

            while(true) {
                FieldInfo fieldInfo;
                String columnName;
                do {
                    do {
                        do {
                            do {
                                do {
                                    if(!resultSet.next()) {
                                        tableInfo.setFieldInfoList(list);
                                        return tableInfo;
                                    }

                                    fieldInfo = new FieldInfo();
                                    columnName = resultSet.getString("COLUMN_NAME");
                                } while("deleted".equalsIgnoreCase(columnName));
                            } while("create_time".equalsIgnoreCase(columnName));
                        } while("create_user".equalsIgnoreCase(columnName));
                    } while("update_time".equalsIgnoreCase(columnName));
                } while("update_user".equalsIgnoreCase(columnName));

                String fieldName = null;
                int _index = columnName.indexOf("_");
                if(_index != -1 && _index != 0 && _index != columnName.length() - 1) {
                    fieldName = this.convertUnderLine(columnName);
                } else {
                    fieldName = columnName.toLowerCase();
                }

                String methodGetName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                String methodSetName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                fieldInfo.setColumnName(columnName);
                fieldInfo.setFieldName(fieldName);
                fieldInfo.setMethodGetName(methodGetName);
                fieldInfo.setMethodSetName(methodSetName);
                fieldInfo.setFieldType(this.getColumnType(Integer.valueOf(resultSet.getString("DATA_TYPE")).intValue()));
                fieldInfo.setIdentityKey(false);
                if(columnName.equals(primaryKey)) {
                    fieldInfo.setPrimaryKey(true);
                    tableInfo.setPrimaryKeyField(fieldInfo.getFieldName());
                }

                if(Date.class.getSimpleName().equals(fieldInfo.getFieldType())) {
                    tableInfo.setHasDate(true);
                }

                if(BigDecimal.class.getSimpleName().equals(fieldInfo.getFieldType())) {
                    tableInfo.setHasDecimal(true);
                }

                list.add(fieldInfo);
            }
        } catch (Exception var15) {
            var15.printStackTrace();
            return null;
        }
    }

    public String convertUnderLine(String s) {
        int _index = s.indexOf("_");
        if(_index == -1) {
            return s;
        } else {
            String[] ss = s.split("_");
            StringBuilder sb = new StringBuilder();

            for(int i = 0; i < ss.length; ++i) {
                if(i == 0) {
                    sb.append(ss[i].toLowerCase());
                } else {
                    sb.append(ss[i].substring(0, 1).toUpperCase() + ss[i].substring(1).toLowerCase());
                }
            }

            return sb.toString();
        }
    }

    public String getBasePackage() {
        return this.basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public void setTargetModuleName(String moduleName) {
        if(StringUtil.isNotBlank(moduleName)) {
            this.targetRootPath = "/" + moduleName + "/src/main/java";
            this.targetWebAppPath = "/" + moduleName + "/src/main/webapp/";
        }

    }

    public boolean isCopyWebResource() {
        return this.copyWebResource;
    }

    public void setCopyWebResource(boolean copyWebResource) {
        this.copyWebResource = copyWebResource;
    }

    private String getColumnType(int type) {
        switch(type) {
            case -16:
                return String.class.getSimpleName();
            case -15:
                return String.class.getSimpleName();
            case -9:
                return String.class.getSimpleName();
            case -7:
                return "boolean";
            case -6:
                return Byte.class.getSimpleName();
            case -5:
                return Long.class.getSimpleName();
            case -4:
                return "Byte[]";
            case -3:
                return "Byte[]";
            case -2:
                return "Byte[]";
            case -1:
                return String.class.getSimpleName();
            case 1:
                return String.class.getSimpleName();
            case 2:
                return Long.class.getSimpleName();
            case 3:
                return BigDecimal.class.getSimpleName();
            case 4:
                return Integer.class.getSimpleName();
            case 5:
                return Short.class.getSimpleName();
            case 6:
                return Float.class.getSimpleName();
            case 8:
                return Double.class.getSimpleName();
            case 12:
                return String.class.getSimpleName();
            case 16:
                return "boolean";
            case 91:
                return Date.class.getSimpleName();
            case 92:
                return Date.class.getSimpleName();
            case 93:
                return Date.class.getSimpleName();
            case 2004:
                return String.class.getSimpleName();
            case 2005:
                return String.class.getSimpleName();
            case 2011:
                return String.class.getSimpleName();
            default:
                return String.class.getSimpleName();
        }
    }

    private void copyWebResources() {
        String targetPath = projectAbsolutePath + this.targetWebAppPath;
        String sourcePath = projectAbsolutePath + this.webResourcePath;
        File[] files = (new File(sourcePath)).listFiles();
        System.out.println(files.length);

        for(int i = 0; i < files.length; ++i) {
            if(files[i].isFile()) {
                this.fileChannelCopy(files[i], new File(targetPath + files[i].getName()));
            }

            if(files[i].isDirectory()) {
                String sourceDir = sourcePath + File.separator + files[i].getName();
                String targetDir = targetPath + File.separator + files[i].getName();
                this.copyDirectory(sourceDir, targetDir);
            }
        }

    }

    private void fileChannelCopy(File s, File t) {
        FileInputStream fi = null;
        FileOutputStream fo = null;
        FileChannel in = null;
        FileChannel out = null;

        try {
            fi = new FileInputStream(s);
            fo = new FileOutputStream(t);
            in = fi.getChannel();
            out = fo.getChannel();
            in.transferTo(0L, in.size(), out);
        } catch (IOException var16) {
            var16.printStackTrace();
        } finally {
            try {
                fi.close();
                in.close();
                fo.close();
                out.close();
            } catch (IOException var15) {
                var15.printStackTrace();
            }

        }

    }

    public void copyDirectory(String sourceDir, String targetDir) {
        try {
            File targetDirect = new File(targetDir);
            if(!targetDirect.exists()) {
                targetDirect.mkdirs();
            }

            File[] file = (new File(sourceDir)).listFiles();

            for(int i = 0; i < file.length; ++i) {
                if(file[i].isFile()) {
                    File sourceFile = file[i];
                    File targetFile = new File((new File(targetDir)).getAbsolutePath() + File.separator + file[i].getName());
                    if(!targetFile.exists()) {
                        this.fileChannelCopy(sourceFile, targetFile);
                    }
                }

                if(file[i].isDirectory()) {
                    String dir1 = sourceDir + File.separator + file[i].getName();
                    String dir2 = targetDir + File.separator + file[i].getName();
                    this.copyDirectory(dir1, dir2);
                }
            }
        } catch (Exception var8) {
            var8.printStackTrace();
        }

    }
}
