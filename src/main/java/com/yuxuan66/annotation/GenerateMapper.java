package com.yuxuan66.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * GenerateMapper 生成Mapper注解 加到实体类会自动生成Mapper
 * @author Sir丶雨轩
 * @since 2021/6/24
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface GenerateMapper {

    /**
     * 需要自动导入的包
     * @return 包名
     */
    String[] autoImport() default {"com.baomidou.mybatisplus.core.mapper.BaseMapper"};

    /**
     * Mapper需要继承的Mapper，可以不写包名，在autoImport中写包名即可
     * @return Mapper
     */
    String baseMapper() default "BaseMapper";

}
