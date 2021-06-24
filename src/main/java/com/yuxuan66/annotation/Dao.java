package com.yuxuan66.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Sir丶雨轩
 * @since 2021/6/24
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Dao {

}
