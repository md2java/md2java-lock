package io.github.md2java.lock.service;

import org.aspectj.lang.ProceedingJoinPoint;

public interface LockService {

	Object applyLogic(ProceedingJoinPoint pjp);

}
