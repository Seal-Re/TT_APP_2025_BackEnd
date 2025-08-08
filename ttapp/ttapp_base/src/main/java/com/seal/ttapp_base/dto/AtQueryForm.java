package com.seal.ttapp_base.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AtQueryForm {
	Integer pageSize;
	Integer  pageNum;
	String queryStr;
	String[] columns;
	Map<String, String> sorts;
	
	//int totalPage;
	//int totalRecords;
}
