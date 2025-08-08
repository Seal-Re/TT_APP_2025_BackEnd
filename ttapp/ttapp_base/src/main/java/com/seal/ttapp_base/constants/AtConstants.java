package com.seal.ttapp_base.constants;

import com.seal.ttapp_base.annotation.NotAuthenticated;

public class AtConstants {
    public static final String API_PREFIX = "/api/v1.0";

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss Z";

    public static final int defaultIdleTime = 30;
    public static final int defaultLoginLockingTime = 3;
    public static final int defaultMaxLoginTries = 5;
    public static final String defaultPasswordSecurityLevel = "LOW"; // 密码最低安全级别
    public static final String passWordSecurityHighLevel = "HIGH";	// 密码最高安全级别

    public static final String TTAPP_SECURITY_KEY = "ttappSecurityKey";
    public static final String USER_AS_MDC_KEY = "mdc.user";
    public static final String USERID_AS_MDC_KEY = "mdc.userId";
    public static final String IP_AS_MDC_KEY = "mdc.ip";
    public static final String USER_AS_TOKEN = NotAuthenticated.COOKIE_NAME_FOR_TOKEN;

    public static final String SYSTEM_ROLE_NAME_CN = "系统管理";
    public static final String ADMIN_USER_NAME = "admin";

    /**平台类型*/
    public static final String USAGE_PARK = "park";

    public static final String USAGE_INDUS = "indus";


    /**步骤指令执行类型*/

    public static final Integer COMMANDRESULT_TYPE_SEND = 0;

    public static final Integer COMMANDRESULT_TYPE_RECEIVE = 1;
}
