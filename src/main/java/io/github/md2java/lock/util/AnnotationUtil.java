package io.github.md2java.lock.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

public class AnnotationUtil {
	
	
	public static <T extends Annotation> T annotatedObject(ProceedingJoinPoint pjp,Class<T> annotationClass) {
		Method method = findMethod(pjp);
		 T annotation = method.getAnnotation(annotationClass);
		return annotation;
	}

	public static Method findMethod(ProceedingJoinPoint pjp) {
		MethodSignature signature = (MethodSignature) pjp.getSignature();
		Method method = signature.getMethod();
		return method;
	}

}
