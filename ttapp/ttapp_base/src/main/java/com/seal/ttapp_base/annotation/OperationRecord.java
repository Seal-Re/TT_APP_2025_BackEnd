package com.seal.ttapp_base.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OperationRecord {
	public static enum ResultSource {
		ALL, RESPONSE_STATUS_ONLY, RESPONSE_DATA_ONLY
	}
	
	String value();
	ResultSource resultSource() default ResultSource.RESPONSE_STATUS_ONLY;
	String expression() default "";
	String compareValue() default "";
}
