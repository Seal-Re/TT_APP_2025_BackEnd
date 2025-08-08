package com.seal.ttapp_base.dto;

import com.seal.ttapp_base.annotation.FieldDesc;
import lombok.Data;

@Data
public class UserExcelDto {
	
	@FieldDesc(value = "登陆名")
	private String name;
	
	@FieldDesc(value = "别名")
	private String stuffName;
	
	@FieldDesc(value = "头衔")
	private String title;
	
	@FieldDesc(value = "角色")
	private String roleName;

}
