package com.seal.ttapp_base.dto;


import com.seal.ttapp_base.entity.User;

public final class LoginResult {
    static final String SUCCESS = "SUCCESS";
    static final String FAIL = "登录失败：用户不存在或密码不正确。";
    static final String LOCKED = "用户已被锁定";
    static final String ADMIN_LOCKED = LOCKED + "，请联系系统管理员解锁。";
    static final String AUTO_UNLOCK_FAILED = LOCKED + "，请于 %s 分钟后再试。";
    static final String NON_ADMIN_LOCKED = LOCKED + "，请联系管理员解锁。";
    static final String IP_DENIED = "您的IP地址被禁止登录。";
    static final String VERIFY_CODE = "确认码不正确或已经过期。";
    private boolean success;
    private String message;
    private User loginedUser;
    private String accessToken;

    private LoginResult() {
    }

    public static LoginResult success(User user) {
        LoginResult result = new LoginResult();
        result.loginedUser = user;
        result.success = true;
        result.message = SUCCESS;
        return result;
    }

    public static LoginResult fail() {
        LoginResult result = new LoginResult();
        result.loginedUser = null;
        result.success = false;
        result.message = FAIL;
        return result;
    }

    public static LoginResult ipDenied() {
        LoginResult result = new LoginResult();
        result.loginedUser = null;
        result.success = false;
        result.message = IP_DENIED;
        return result;
    }

    public static LoginResult failedAndLockedIsAdmin(boolean isAdmin) {
        LoginResult result = new LoginResult();
        result.loginedUser = null;
        result.success = false;
        if (isAdmin) {
            result.message = FAIL + "。" + ADMIN_LOCKED;
        } else {
            result.message = FAIL + "。" + NON_ADMIN_LOCKED;
        }
        return result;
    }

    public static LoginResult adminLocked() {
        LoginResult result = new LoginResult();
        result.loginedUser = null;
        result.success = false;
        result.message = ADMIN_LOCKED;
        return result;
    }

    public static LoginResult autoUnLockFailed(int lockTime) {
        LoginResult result = new LoginResult();
        result.loginedUser = null;
        result.success = false;
        result.message = String.format(AUTO_UNLOCK_FAILED, lockTime);
        return result;
    }

    public static LoginResult nonAdminLocked() {
        LoginResult result = new LoginResult();
        result.loginedUser = null;
        result.success = false;
        result.message = NON_ADMIN_LOCKED;
        return result;
    }

    public static LoginResult verifycodefail() {
        LoginResult result = new LoginResult();
        result.loginedUser = null;
        result.success = false;
        result.message = VERIFY_CODE;
        return result;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public User getLoginedUser() {
        return this.loginedUser;
    }

    public String getMessage() {
        return message;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
