package com.datasync.util;

import com.datasync.bean.TableInfo;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.*;

/**
 * 数据库操作类
 * @author zhangqingzhou
 */
public class DbSys {
    private Logger logger = Logger.getLogger(DbSys.class);
    private static final Map<String, String> map = ConfigUtil.getPropertiesForMap;
    private String maxPriColumn;
    private Connection mysqlSourceConn;
    private Connection mysqlTargetConn;
    private TableInfo tableInfo;

    public DbSys(TableInfo tableInfo) {
        try {
            Class.forName(map.get("dbdriver"));
            this.mysqlSourceConn = this.getSourceConn();
            this.mysqlTargetConn = this.getTargetConn();
            this.tableInfo = tableInfo;
        } catch (Exception e) {
            this.logger.error("创建数据库链接异常：", e);
        }
    }

    public synchronized Connection getSourceConn() throws SQLException {
        return DriverManager.getConnection(map.get("sourceDburl"), map.get("sourceUsername"), map.get("sourcePassword"));
    }

    public synchronized Connection getTargetConn() throws SQLException {
        return DriverManager.getConnection(map.get("targetDburl"), map.get("targetUsername"), map.get("targetPassword"));
    }

    private boolean testMysql(Connection mysqlConn) {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = mysqlConn.createStatement();
            rs = stmt.executeQuery("select curdate()");
        } catch (Exception e) {
            return false;
        } finally {
            try {
                rs.close();
            } catch (Exception e) {
            }
            try {
                stmt.close();
            } catch (Exception e) {
            }
        }
        return true;
    }


    /**
     * 处理单表数据
     *
     * @param columns
     * @param tableName
     * @param auto_id
     * @param one_table_num
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> getSourceData(String columns, String tableName, String sourcePriColum, String auto_id, String one_table_num) throws Exception {
        List<Map<String, Object>> dbList = new ArrayList<Map<String, Object>>();
        PreparedStatement pst = null;
        ResultSet res = null;
        String where = map.get("one_table_where_sql");
        if (columns.equals("allCols") || columns.equals("*")) {
            columns = this.getAllColumnNameStrs(this.getSourceConn(), tableName, true);
        }
        String sql = "select " + columns + "," + sourcePriColum + " from " + tableName + " " + where;
        sql += " and " + sourcePriColum + ">" + auto_id + " order by " + sourcePriColum + " asc limit " + one_table_num;
        //logger.info("query ref data! sql：【auto_id："+auto_id+"】");
        this.maxPriColumn = auto_id;
        try {
            String[] columnList = columns.split(",");
            if (this.testMysql(this.mysqlSourceConn) == false) {
                this.mysqlSourceConn = this.getSourceConn();
            }
            pst = this.mysqlSourceConn.prepareStatement(sql);
            res = pst.executeQuery();
            Map<String, Object> rowMap = null;
            while (res.next()) {
                try {
                    rowMap = new HashMap<String, Object>(columnList.length);
                    for (String key : columnList) {
                        rowMap.put(tableInfo.getCoverField(key), res.getString(key));
                    }

                    String curId = res.getString(sourcePriColum);
                    long nowId = Long.parseLong(curId);
                    long oldId = Long.parseLong(this.maxPriColumn);
                    if (this.maxPriColumn == null || (nowId - oldId > 0)) {
                        this.maxPriColumn = curId;
                    }
                    dbList.add(rowMap);
                } catch (Exception e) {
                    // TODO: handle exception
                    logger.error("table: " + tableName + "\n" + e.getMessage());
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            logger.error("query data! sql：" + sql + "\n" + e.getMessage());
        } finally {
            try {
                if (pst != null) {
                    pst.close();
                }
                if (res != null) {
                    res.close();
                }
                this.mysqlSourceConn.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return dbList;
    }

    /**
     * 插入数据
     *
     * @param objList
     * @param columns
     * @param tableName
     * @throws Exception
     */
    public boolean insertTargetData(List<Map<String, Object>> objList, String columns, String tableName) throws Exception {
        if (objList == null || objList.size() == 0) {
            this.logger.info("insert data! table: 【" + tableName + "】; data size: NULL or 0! ");
            return true;
        }
        if (columns.equals("allCols") || columns.equals("*")) {
            columns = getAllColumnNameStrs(this.getTargetConn(), tableName, false);
        }

        PreparedStatement pst = null;
        this.logger.info("insert data! table: 【" + tableName + "】; data size: " + objList.size());
        try {
            StringBuffer buffer = new StringBuffer();
            String[] colAry = columns.split(",");
            int columnNum = colAry.length;
            buffer.append("insert into " + tableName + " (" + columns + ") values(");
            for (int i = 0; i < columnNum; i++) {
                buffer.append("?,");
            }
            buffer.deleteCharAt(buffer.length() - 1);
            buffer.append(")");
            if (this.testMysql(this.mysqlTargetConn) == false) {
                this.mysqlTargetConn = this.getTargetConn();
            }
            pst = this.mysqlTargetConn.prepareStatement(buffer.toString());
            try {
                for (Map<String, Object> rowMap : objList) {
                    try {
                        for (int i = 0; i < colAry.length; i++) {
                            pst.setObject(i + 1, rowMap.get(colAry[i]));
                        }
                        pst.addBatch();
                    } catch (Exception e) {
                        this.logger.warn("insert data! table: " + tableName + " ! 批量保存数据组装异常： " + e.getMessage());
                    }
                }
                pst.executeBatch();
                pst.close();
            } catch (Exception e) {
                logger.error("insert data! table: " + tableName + " ! 批量保存异常：" + e.getMessage());
                pst = this.mysqlTargetConn.prepareStatement(buffer.toString());
                for (Map<String, Object> rowMap : objList) {
                    try {
                        for (int i = 0; i < colAry.length; i++) {
                            pst.setObject(i + 1, rowMap.get(colAry[i]));
                        }
                        pst.addBatch();
                    } catch (Exception e1) {
                        this.logger.warn("insert data! table: " + tableName + " ! 批量保存数据组装异常： " + e1.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            this.logger.error("insert data! table: " + tableName, e);
            return false;
        } finally {
            try {
                if (pst != null) {
                    pst.close();
                }
            } catch (Exception e2) {
                // TODO: handle exception
                e2.printStackTrace();
            }
            try {
                this.mysqlTargetConn.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 最大游标
     *
     * @return 2019年3月30日 上午12:50:53
     */
    public String getMaxPriColumn() {
        return maxPriColumn;
    }

    /**
     * 处理单表数据
     *
     * @param columns       源字段
     * @param tableName     源表
     * @param auto_id
     * @param one_table_num
     * @return
     * @throws Exception
     */
    public List<Object[]> readOneDate(String columns, String tableName, String sourcePriColum, String auto_id, String one_table_num) throws Exception {
        // TODO Auto-generated method stub
        List<Object[]> dbList = new ArrayList<Object[]>();
        PreparedStatement pst = null;
        ResultSet res = null;
        String where = map.get("one_table_where_sql");
        if (columns.equals("allCols") || columns.equals("*")) {
            columns = getAllColumnNameStrs(this.getSourceConn(), tableName, true);
        }
        String sql = "select " + columns + "," + sourcePriColum + " from " + tableName + " " + where;
        sql += " and " + sourcePriColum + ">" + auto_id + " order by " + sourcePriColum + " asc limit " + one_table_num;
        //logger.info("query ref data! sql：【auto_id："+auto_id+"】");
        this.maxPriColumn = auto_id;
        try {
            String[] columnList = columns.split(",");
            if (this.testMysql(this.mysqlSourceConn) == false) {
                this.mysqlSourceConn = this.getSourceConn();
            }
            pst = this.mysqlSourceConn.prepareStatement(sql);
            res = pst.executeQuery();
            while (res.next()) {
                try {
                    Object[] rowAry = new Object[columnList.length];
                    for (int i = 0; i < rowAry.length; i++) {
                        rowAry[i] = res.getString(columnList[i]);
                    }
                    String curId = res.getString(sourcePriColum);
                    long nowId = Long.parseLong(curId);
                    long oldId = Long.parseLong(this.maxPriColumn);
                    if (this.maxPriColumn == null || (nowId - oldId > 0)) {
                        this.maxPriColumn = curId;
                    }
                    dbList.add(rowAry);
                } catch (Exception e) {
                    // TODO: handle exception
                    logger.error("table: " + tableName + "\n" + e.getMessage());
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            logger.error("query data! sql：" + sql + "\n" + e.getMessage());
        } finally {
            try {
                if (pst != null) {
                    pst.close();
                }
                if (res != null) {
                    res.close();
                }
                this.mysqlSourceConn.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return dbList;
    }

    /**
     * 插入数据
     *
     * @param objList
     * @param columns
     * @param tableName
     * @throws Exception
     */
    public boolean insertDate(List<Object[]> objList, String columns, String tableName) throws Exception {
        if (objList == null) {
            this.logger.info("insert data! table: " + tableName + "; data size: NULL ");
            return false;
        }
        if (columns.equals("allCols") || columns.equals("*")) {
            columns = getAllColumnNameStrs(this.getTargetConn(), tableName, true);
        }

        PreparedStatement pst = null;
        this.logger.info("insert data! table: 【" + tableName + "】; data size: " + objList.size());
        try {
            StringBuffer buffer = new StringBuffer();
            String[] colAry = columns.split(",");
            int columnNum = colAry.length;
            buffer.append("insert into " + tableName + " (" + columns + ") values(");
            for (int i = 0; i < columnNum; i++) {
                buffer.append("?,");
            }
            buffer.deleteCharAt(buffer.length() - 1);
            buffer.append(")");
            if (this.testMysql(this.mysqlTargetConn) == false) {
                this.mysqlTargetConn = this.getTargetConn();
            }
            pst = this.mysqlTargetConn.prepareStatement(buffer.toString());
            try {
                for (Object[] objAry : objList) {
                    try {
                        for (int i = 0; i < objAry.length; i++) {
                            pst.setObject(i + 1, objAry[i]);
                        }
                        pst.addBatch();
                    } catch (Exception e) {
                        this.logger.warn("insert data! table: " + tableName + " ! 批量保存数据组装异常： " + e.getMessage());
                    }
                }
                pst.executeBatch();
                pst.close();
            } catch (Exception e) {
                // TODO: handle exception
                logger.error("insert data! table: " + tableName + " ! 批量保存异常：" + e.getMessage());
                pst = this.mysqlTargetConn.prepareStatement(buffer.toString());
                for (Object[] objAry : objList) {
                    try {
                        for (int i = 0; i < objAry.length; i++) {
                            pst.setObject(i + 1, objAry[i]);
                        }
                        pst.executeUpdate();
                    } catch (Exception e1) {
                        this.logger.warn("insert data! table: " + tableName + " ! 单条保存 " + e1.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            this.logger.error("insert data! table: " + tableName, e);
            return false;
        } finally {
            try {
                if (pst != null) {
                    pst.close();
                }
            } catch (Exception e2) {
                // TODO: handle exception
                e2.printStackTrace();
            }
            try {
                this.mysqlTargetConn.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 获取列名
     *
     * @param con
     * @param tableName
     * @return
     */
    public String[] getAllColumnNames(Connection con, String tableName) {
        String[] colNames = null;
        try {
            String sql = "select * from " + tableName + " limit 0";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet tables = ps.executeQuery(sql);
            ResultSetMetaData metaData2 = tables.getMetaData();
            int columnCount = metaData2.getColumnCount();
            colNames = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                colNames[i] = metaData2.getColumnName(i + 1);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return colNames;
    }

    /**
     * 获取列名
     *
     * @param con
     * @param tableName
     * @return
     */
    public String getAllColumnNameStrs(Connection con, String tableName, boolean isSource) {
        String colNames = "";
        try {
            Set<String> filterMap = new HashSet<String>();

            String filterColum = null;
            if (isSource) {
                filterColum = this.tableInfo.getSourceFilterColum();
            } else {
                filterColum = this.tableInfo.getTargetFilterColum();
            }
            if (filterColum != null && filterColum.trim().length() > 0) {
                String[] filter = filterColum.split(",");
                for (String key : filter) {
                    filterMap.add(key);
                }
            }
            String sql = "select * from " + tableName + " limit 0";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet tables = ps.executeQuery(sql);
            ResultSetMetaData metaData2 = tables.getMetaData();
            int columnCount = metaData2.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                String columnName = metaData2.getColumnName(i + 1);
                if (filterMap.contains(columnName)) {
                    continue;
                }
                colNames += "," + columnName;
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (colNames.length() > 1) {
            colNames = colNames.substring(1);
        }
        return colNames;
    }


}
