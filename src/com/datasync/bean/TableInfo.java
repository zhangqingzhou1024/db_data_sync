package com.datasync.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableInfo {
	private String sourceTable;
	private String sourceColumn;
	private String sourcePriColum;
	private String sourceFilterColum;
	private String targetTable;
	private String targetColumns;
	private String targetFilterColum;
	private String maxSourcePriColum;
	
	private List<FieldConversion> fieldConversionList;
	
	private Map<String, String> fieldMap = new HashMap<String, String>();
	
	public String getMaxSourcePriColum() {
		return maxSourcePriColum;
	}
	public void setMaxSourcePriColum(String maxSourcePriColum) {
		this.maxSourcePriColum = maxSourcePriColum;
	}
	public String getSourceTable() {
		return sourceTable;
	}
	public void setSourceTable(String sourceTable) {
		this.sourceTable = sourceTable;
	}
	public String getSourceColumn() {
		return sourceColumn;
	}
	public void setSourceColumn(String sourceColumn) {
		this.sourceColumn = sourceColumn;
	}
	public String getSourcePriColum() {
		return sourcePriColum;
	}
	public void setSourcePriColum(String sourcePriColum) {
		this.sourcePriColum = sourcePriColum;
	}
	public String getTargetTable() {
		return targetTable;
	}
	public void setTargetTable(String targetTable) {
		this.targetTable = targetTable;
	}
	public String getTargetColumns() {
		return targetColumns;
	}
	public void setTargetColumns(String targetColumns) {
		this.targetColumns = targetColumns;
	}
	public List<FieldConversion> getFieldConversionList() {
		return fieldConversionList;
	}
	public void setFieldConversionList(List<FieldConversion> fieldConversionList) {
		this.fieldConversionList = fieldConversionList;
		
	}
	
	public String getSourceFilterColum() {
		return sourceFilterColum;
	}
	public void setSourceFilterColum(String sourceFilterColum) {
		this.sourceFilterColum = sourceFilterColum;
	}
	public String getTargetFilterColum() {
		return targetFilterColum;
	}
	public void setTargetFilterColum(String targetFilterColum) {
		this.targetFilterColum = targetFilterColum;
	}
	
	/**
	 * 设置
	 * @param fieldConversionList
	 * 2019年3月30日 上午2:13:19
	 */
	public void setFieldCoverMap(List<FieldConversion> fieldConversionList){
		if(fieldConversionList == null || fieldConversionList.size() == 0){
			return;
		}
		for (FieldConversion obj : fieldConversionList) {
			fieldMap.put(obj.getSourceField(), obj.getTargetField());
		}
	}
	
	/**
	 * 获取转化后的 字段名
	 * @param sourceField
	 * @return
	 * 2019年3月30日 上午1:53:55
	 */
	public String getCoverField(String sourceField){
		
		String field = fieldMap.get(sourceField);
		if(field == null){
			return sourceField;
		}
		return field;
	}

}
