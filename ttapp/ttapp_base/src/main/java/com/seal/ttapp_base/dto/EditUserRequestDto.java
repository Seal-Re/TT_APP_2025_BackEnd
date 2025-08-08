package com.seal.ttapp_base.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class EditUserRequestDto {
	
	private String userId;
	
	private String passwordHash;
	
	private String oldPasswordHash;

	private Integer management;

}
