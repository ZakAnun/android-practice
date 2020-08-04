package com.zakli.libannotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liminglin
 *
 * RetentionPolicy.SOURCE 只需要在编译之前用，注解处理的时候
 * RetentionPolicy.RUNTIME 是在运行时进行处理
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface BindView {
    int value();
}
