package io.github.md2java.lock.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import io.github.md2java.lock.LockAutoConfiguration;

@Retention(RUNTIME)
@Target(TYPE)
@Import(LockAutoConfiguration.class)
@Documented
public @interface EnableClusterLock {

}
