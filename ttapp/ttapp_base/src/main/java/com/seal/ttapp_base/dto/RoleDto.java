package com.seal.ttapp_base.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;



@Getter
@Setter
@ToString
@TableName("role")
@ApiModel("Role")
public class RoleDto {

    private Long roleId;

    private String roleName;

    public RoleDto() {

    }
    public RoleDto(String roleName) {
        this.roleName = roleName;
    }

}
