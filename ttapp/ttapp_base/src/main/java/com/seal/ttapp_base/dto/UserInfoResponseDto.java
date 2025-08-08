package com.seal.ttapp_base.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class UserInfoResponseDto {

    @ApiModelProperty("用户id")
    private String userId;

    @ApiModelProperty("用户名称")
    private String name;

    @ApiModelProperty("别名")
    private String stuffName;

    @ApiModelProperty("用户状态")
    private Boolean userStatus;

}
