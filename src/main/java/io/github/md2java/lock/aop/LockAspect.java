package io.github.md2java.lock.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import io.github.md2java.lock.service.LockService;
import lombok.RequiredArgsConstructor;


@Aspect
@Component
@RequiredArgsConstructor
public class LockAspect {

	private LockService lockService;

	@Pointcut("@annotation(io.github.md2java.lock.annotation.ClusterLock)")
	public void lockInfo() {

	}

	@Around("lockInfo()")
	public Object handlelLogMethodInfo(ProceedingJoinPoint pjp) throws Throwable {
		Object ret = lockService.applyLogic(pjp);
		return ret;

	}

	
}
