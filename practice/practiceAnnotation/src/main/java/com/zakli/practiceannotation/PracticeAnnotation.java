package com.zakli.practiceannotation;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zakli.libannotation.ApBinding;
import com.zakli.libannotation.BindView;


/**
 * 练习
 * 1、通过反射实现 view 的绑定 -- libAnnotationReflection
 * 2、通过 annotation processor 实现 view 的绑定 -- libApBinding、libAnnotation、libProcessor
 */
public class PracticeAnnotation extends AppCompatActivity {

    @BindView(R.id.parent)
    View parent;
    @BindView(R.id.textView)
    TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pannotation_main);
//        ReflectionBinding.bind(this);
//        textView.setText("reflection binding");

        ApBinding.bind(this);
        textView.setText("processor binding");
        parent.setBackgroundColor(Color.GREEN);
    }
}
