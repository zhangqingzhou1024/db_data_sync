package com.datasync.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * 读取配置文件信息
 *
 * @author zhangqingzhou
 */
public class Initialization {

    public static void init() {
        try {
            Properties prop = new Properties();
            // 读取属性文件a.properties
            InputStream in = new BufferedInputStream(new FileInputStream("config/config.properties"));
            // 加载属性列表
            prop.load(new InputStreamReader(in, "utf-8"));
            Iterator<String> it = prop.stringPropertyNames().iterator();
            Map<String, String> map = ConfigUtil.getPropertiesForMap;
            while (it.hasNext()) {
                String key = it.next();
                map.put(key, prop.getProperty(key));
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

}
