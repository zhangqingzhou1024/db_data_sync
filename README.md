
数据库同步组件：
说明：
即把 源数据库（source-DB）中的表，同步到目标数据库（target-DB）的目标表中；

入口类：
com.datasync.main.MoverMain

配置说明：
1. 数据源配置 config/config.properties
	
	#mysql  Driver
	dbdriver=com.mysql.jdbc.Driver
	
	#源数据库的配置信息
	sourceDburl=jdbc:mysql://127.0.0.1:3308/source_data?useUnicode=true&characterEncoding=UTF-8
	sourceUsername=root
	sourcePassword=123456
	
	#导入数据库的配置信息
	targetDburl=jdbc:mysql://127.0.0.1:3308/study?useUnicode=true&characterEncoding=UTF-8
	targetUsername=root
	targetPassword=123456
	
	#true:一次性 false:持续跟踪
	is_run_once=false
	
	#单表导入（没次查询数量）
	one_table_num=3000
	#单表导入（查询条件）
	one_table_where_sql=where 1=1 

------------------------------------------------------
2. 同步表信息配置 config/dataMove.xml  配置源表和目标表信息
	
<tableConfig>
<tableList>
	<tableInfo>
		<sourceTable>event_news_ref</sourceTable>
		<sourceColumn>auto_id,title,content,website_id</sourceColumn>
		<sourcePriColum>auto_id</sourcePriColum>
		<targetTable>student</targetTable>
		<targetColumns>*</targetColumns>
		<!-- 字段映射关系 -->
		<fieldConversionList>
			<fieldConversion>
				<sourceField>auto_id</sourceField>
				<targetField>id</targetField>
			</fieldConversion>
			<fieldConversion>
				<sourceField>content</sourceField>
				<targetField>text</targetField>
			</fieldConversion> 
			<fieldConversion>
				<sourceField>website_id</sourceField>
				<targetField>flag_id</targetField>
			</fieldConversion> 
		</fieldConversionList>
	</tableInfo> 
	
	<tableInfo>
		<sourceTable>event_news_ref</sourceTable>
		<sourceFilterColum>eid</sourceFilterColum>
		<sourceColumn>*</sourceColumn>
		<sourcePriColum>auto_id</sourcePriColum>
		<targetTable>event_news_ref</targetTable>
		<targetColumns>*</targetColumns>
		<!-- <targetFilterColum>*</targetFilterColum> -->
		
		<fieldConversionList>
			<fieldConversion>
				<sourceField>auto_id</sourceField>
				<targetField>eid</targetField>
			</fieldConversion>
		<!-- 	<fieldConversion>
				<sourceField>eid</sourceField>
				<targetField>auto_id</targetField>
			</fieldConversion> -->
		
		</fieldConversionList>
		
	</tableInfo>
	
</tableList>
</tableConfig>
---------------------------------------------------------
	

	
