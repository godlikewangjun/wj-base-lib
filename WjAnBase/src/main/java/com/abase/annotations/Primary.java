package com.abase.annotations;

/**
 * 注解sql创建表
 * @author Administrator
 * @version 1.0
 * @date 2018/10/26/026
 */

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Primary {

}
