package com.seal.ttapp_base.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.List;


public class User {

    @ApiModelProperty("用户id")
    private String userId;

    @ApiModelProperty("用户名称")
    private String name;

    private String stuffName;

    private String title;

    @ApiModelProperty("盐")
    private String salt;

    @ApiModelProperty("密码哈希")
    private String passwordHash;

    @ApiModelProperty("用户状态")
    private Boolean userStatus;

    private Byte failedLoginAttemptCount;

    @ApiModelProperty("是否锁定")
    private Boolean locked;

    @ApiModelProperty("是否为自动锁定")
    private Boolean autoLocked;

    @ApiModelProperty("锁定时间")
    private Date lockedAt;

    @ApiModelProperty("最后登录时间")
    private Date lastLogin;

    private String securityKey;

    private String profile;

    @ApiModelProperty("创建时间")
    private Date createdAt;

    @ApiModelProperty("更新时间")
    private Date updatedAt;

    @ApiModelProperty("创建人")
    private String createdBy;

    @ApiModelProperty("更新人")
    private String updatedBy;

    @ApiModelProperty("删除标记")
    private Boolean deleted;

    @ApiModelProperty("角色集合")
    private List<Role> roles;

    @ApiModelProperty("管理的专业")
    private Integer management;

    @TableField(exist = false)
    private String subjectName; // 军代表专业名称

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStuffName() {
        return stuffName;
    }

    public void setStuffName(String stuffName) {
        this.stuffName = stuffName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Boolean getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(Boolean userStatus) {
        this.userStatus = userStatus;
    }

    public Byte getFailedLoginAttemptCount() {
        return failedLoginAttemptCount;
    }

    public void setFailedLoginAttemptCount(Byte failedLoginAttemptCount) {
        this.failedLoginAttemptCount = failedLoginAttemptCount;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public Boolean getAutoLocked() {
        return autoLocked;
    }

    public void setAutoLocked(Boolean autoLocked) {
        this.autoLocked = autoLocked;
    }

    public Date getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(Date lockedAt) {
        this.lockedAt = lockedAt;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getSecurityKey() {
        return securityKey;
    }

    public void setSecurityKey(String securityKey) {
        this.securityKey = securityKey;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Integer getManagement() {
        return management;
    }

    public void setManagement(Integer management) {
        this.management = management;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
}