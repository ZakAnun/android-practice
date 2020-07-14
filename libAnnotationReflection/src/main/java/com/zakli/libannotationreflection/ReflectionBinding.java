package com.zakli.libannotationreflection;

import android.app.Activity;

import java.lang.reflect.Field;

public class ReflectionBinding {

    public static void bind(Activity activity) {
        // getFields() 是获取所有可以访问的 field
        // getDeclaredFields() 是获取所有的 field
        for (Field field : activity.getClass().getDeclaredFields()) {
            BindView bindView = field.getAnnotation(BindView.class);
            if (bindView != null) {
                try {
                    field.setAccessible(true);
                    field.set(activity, activity.findViewById(bindView.value()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
