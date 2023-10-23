package io.github.md2java.lock.service;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Service;

import io.github.md2java.lock.annotation.ClusterLock;
import io.github.md2java.lock.util.AnnotationUtil;

@Service
public class LockServiceImpl implements LockService {

	@Override
	public Object applyLogic(ProceedingJoinPoint pjp) {
		ClusterLock clusterLock = AnnotationUtil.annotatedObject(pjp,ClusterLock.class);
		
		return null;
	}

	

}
