package com.datasync.main;

import java.io.File;
import java.io.FileInputStream;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.datasync.bean.FieldConversion;
import com.datasync.bean.TableConfig;
import com.datasync.bean.TableInfo;
import com.datasync.server.SynThreads;
import com.datasync.util.Initialization;
import com.thoughtworks.xstream.XStream;

/**
 * 入口类
 * 说明：
 *
 * @author zqz
 * @createTime 2019年3月30日 下午1:46:06
 */
public class MoverMain {
    private XStream xStream;
    private Logger logger = Logger.getLogger(MoverMain.class);

    public MoverMain() {
        xStream = new XStream();
        xStream.alias("tableConfig", TableConfig.class);
        xStream.alias("tableInfo", TableInfo.class);
        xStream.alias("fieldConversion", FieldConversion.class);
    }

    public void start() {
        try {
            TableConfig list = (TableConfig) xStream.fromXML(new FileInputStream(new File("config/dataMove.xml")));
            this.logger.info("number of tables: " + list.getTableList().size());
            for (int j = 0; j < list.getTableList().size(); j++) {
                TableInfo tableInfo = list.getTableList().get(j);
                this.logger.info("table is -->【" + tableInfo.getTargetTable() + "】");
                SynThreads syn = new SynThreads(tableInfo);
                new Thread(syn).start();
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            this.logger.error("", e);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        PropertyConfigurator.configure("config/log4j.properties");//加载log4j日志文件
        Initialization.init();
        MoverMain main = new MoverMain();
        main.start();
    }

}
