package com.seal.ttapp_base.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class TUserRoleDto {

    private Long id;

    private Long userId;

    private Long roleId;
}
