package com.datasync.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
/**
 * 常用一般文件操作
 * @author zhangqingzhou
 */
public class FileUtil {
	private static Logger logger = Logger.getLogger(FileUtil.class);
	/**
	 * 追加文件：使用FileOutputStream，在构造FileOutputStream时，把第二个参数设为true
	 * 
	 * @param fileName 文件名
	 * @param content  文件内容
	 */
    public static void writeFileByAddNew(String fileName, String content) {   
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, true), "UTF-8"));
            out.write(content + "\n");
            System.out.println("重新更新游标 "+fileName+"-->"+content);
        } catch (Exception e) {
            logger.error("file name: " + fileName, e);
        } finally {
            try {
                out.close();
            } catch (Exception e) {     }
        } 
    }
    
    /**
	 * 写一个新文件
	 * 
	 * @param fileName 文件名
	 * @param content  文件内容
	 */
    public static void writeFileByNewFile(String dirName, String fileName, String content) {  
        if(content == null || fileName == null ){
			return ;
		}
		
		if(dirName == null){
			dirName = "./";
		}
		
		File dir = new File(dirName);
		if(dir.exists() == false){
			dir.mkdirs();
		} 
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dirName + File.separator + fileName, false), "UTF-8"));
            out.write(content);
            System.out.println("重新更新游标 "+fileName+"-->"+content);
        } catch (Exception e) {
            logger.error("file name: " + fileName, e);
        } finally {
            try {
                out.close();
            } catch (Exception e) {        }
        } 
    }
    
    /**
     * 按行读取文件
     * 
     * @param fileName 文件名
     * @return  List<String> 行列表
     * @throws IOException 
     */
    public static List<String> readFileByLine(String fileName) {
    	List<String> lineList = new ArrayList<String>();
    	
    	try {
    		FileInputStream stream = new FileInputStream(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				lineList.add(line.trim());
			}
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lineList;
    }
    
    /**
     * 读取文件，作为一个字符串返回
     * 
     * @param fileName 文件名
     * @return  String 文件内容
     * @throws IOException 
     */
    public static String readFile(String fileName){
    	StringBuilder builder = new StringBuilder();
    	BufferedReader reader = null;
    	FileInputStream fileInput = null;
    	try {
    		File file = new File(fileName);
    		if(!file.exists()){
    			file.createNewFile();
    		}
    		fileInput = new FileInputStream(file);
			reader = new BufferedReader(new InputStreamReader(fileInput, "UTF-8"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				builder.append(line.trim() + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				fileInput.close();
			} catch (Exception e) {}
			try {
				reader.close();
			} catch (Exception e) {}
		}
		return builder.toString().trim();
    }
    
    public static void main(String[] args){
    	FileUtil.writeFileByNewFile(".", "test.txt", "aaaa");
    	System.out.println("http://weibo.com/123465".replace("/", "_"));
    }
}
