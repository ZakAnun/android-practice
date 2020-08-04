package com.zakli.libprocessor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.zakli.libannotation.BindView;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class BindingProcessor extends AbstractProcessor {

    Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
    }

    /**
     * 处理注解
     *
     * JavaPoet 大致用法
     * JavaFile.builder()
     *         .build()
     *         .writeTo(filer);
     *
     * @param set
     * @param roundEnvironment 表示多轮的 annotation processing
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println("execute processing.....");
        for (Element element : roundEnvironment.getRootElements()) {
            String packageStr = element.getEnclosingElement().toString();
            String classStr = element.getSimpleName().toString();
            ClassName className = ClassName.get(packageStr, classStr + "$Binding");
            MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.get(packageStr, classStr), "activity");
            boolean hasBinding = false;

            for (Element enclosedElement : element.getEnclosedElements()) {
                BindView bindView = enclosedElement.getAnnotation(BindView.class);
                if (bindView != null) {
                    hasBinding = true;
                    constructorBuilder.addStatement("activity.$N = activity.findViewById($L)",
                            enclosedElement.getSimpleName(), bindView.value());
                }
            }

            TypeSpec buildClass = TypeSpec.classBuilder(className)
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(constructorBuilder.build())
                    .build();

            if (hasBinding) {
                try {
                    JavaFile.builder(packageStr, buildClass)
                            .build()
                            .writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    /**
     * 指定需要支持的 annotation
     *
     * 只处理 BindView
     *
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(BindView.class.getCanonicalName());
    }
}