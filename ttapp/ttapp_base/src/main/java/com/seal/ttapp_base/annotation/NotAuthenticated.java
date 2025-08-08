package com.seal.ttapp_base.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface NotAuthenticated {

    String COOKIE_NAME_FOR_TOKEN = "accessToken";

}
