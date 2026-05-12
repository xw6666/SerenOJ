package top.wjr.serenoj.utils;

import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class ServiceContextUtils {
    public static <T extends Annotation> T getAnnotation(Method method, Class<?> clazz, Class<T> annotationClass) {
        T annotation = AnnotationUtils.getAnnotation(method, annotationClass);
        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(clazz, annotationClass);
        }
        return annotation;
    }
}