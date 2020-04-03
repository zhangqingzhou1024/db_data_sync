package com.datasync.server;

import com.datasync.bean.TableInfo;
import com.datasync.util.ConfigUtil;
import com.datasync.util.DbSys;
import com.datasync.util.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 同步线程
 * 一张表一个线程
 * 作用：
 *
 * @author zqz
 * @createTime 2019年3月29日 下午5:32:30
 */
public class SynThreads implements Runnable {
    private TableInfo table;
    private Logger logger = Logger.getLogger(SynThreads.class);
    private static final Map<String, String> map = ConfigUtil.getPropertiesForMap;
    // 同步记录游标
    private static String synRecordDir = "cursor";
    private String cursorFile = "";
    // 是否为 一次性任务
    private boolean runOnce = true;
    DbSys dbsys = null;

    public SynThreads(TableInfo tableInfo) {
        this.table = tableInfo;
        tableInfo.setFieldCoverMap(tableInfo.getFieldConversionList());
        dbsys = new DbSys(tableInfo);

        this.cursorFile = "dest_" + tableInfo.getTargetTable();
        this.runOnce = map.get("is_run_once").equals("false") ? false : true;
        //this.cursorFile = tableInfo.getSourceTable() + "_" + tableInfo.getTargetTable();
    }

    @Override
    public void run() {
        while (true) {

            //int loadSize  = this.dataSyncForOne();
            int loadSize = this.dataSyncForOneMap();
            if (loadSize == 0) {
                if (runOnce) {
                    this.logger.info(table.getSourceTable() + " 结束运行！");
                    return;
                } else {
                    this.logger.info(table.getSourceTable() + " load num is 0, sleep 10s ,now runId -->" + dbsys.getMaxPriColumn());
                    sleep(10L);
                }
            }
        }
    }


    /**
     * 2019年3月29日 下午11:21:31
     */
    private int dataSyncForOne() {

        String idStr = FileUtil.readFile("" + synRecordDir + "/" + cursorFile);
        String auto_id = "0";
        if (StringUtils.isNotBlank(idStr)) {
            auto_id = idStr;
        }
        List<Object[]> saveData = null;
        try {
            saveData = dbsys.readOneDate(table.getSourceColumn(), table.getSourceTable(), table.getSourcePriColum(), auto_id, map.get("one_table_num"));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        boolean isSucced = false;
        try {
            isSucced = dbsys.insertDate(saveData, table.getTargetColumns(), table.getTargetTable());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isSucced && dbsys.getMaxPriColumn() != null) {
            idStr = dbsys.getMaxPriColumn();
            FileUtil.writeFileByNewFile("./" + synRecordDir + "", cursorFile, idStr);
        } else {
            this.logger.warn("Fail to insert data!");
        }
        if (saveData == null) {
            return 0;
        }
        return saveData.size();
    }

    /**
     * 2019年3月29日 下午11:21:31
     */
    private int dataSyncForOneMap() {

        String idStr = FileUtil.readFile("" + synRecordDir + "/" + cursorFile);
        String auto_id = "0";
        if (StringUtils.isNotBlank(idStr)) {
            auto_id = idStr;
        }
        List<Map<String, Object>> saveData = null;
        try {
            saveData = dbsys.getSourceData(table.getSourceColumn(), table.getSourceTable(), table.getSourcePriColum(), auto_id, map.get("one_table_num"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean isSucced = false;
        try {
            isSucced = dbsys.insertTargetData(saveData, table.getTargetColumns(), table.getTargetTable());
        } catch (Exception e) {
            e.printStackTrace();
        }

        int sourceSize = 0;
        if (saveData == null) {
            sourceSize = 0;
        } else {
            sourceSize = saveData.size();
        }
        if (isSucced && dbsys.getMaxPriColumn() != null && sourceSize > 0) {
            idStr = dbsys.getMaxPriColumn();
            FileUtil.writeFileByNewFile("./" + synRecordDir + "", cursorFile, idStr);
        } else {
            if (sourceSize > 0) {
                this.logger.warn("Fail to insert data!");
            }
        }

        return sourceSize;
    }

    /**
     * 睡眠
     *
     * @param timeout 2019年3月30日 上午12:32:59
     */
    private void sleep(long timeout) {
        try {
            TimeUnit.SECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
