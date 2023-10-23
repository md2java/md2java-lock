package io.github.md2java.lock.service;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Service;

import io.github.md2java.lock.annotation.ClusterLock;
import io.github.md2java.lock.model.LockInfo;
import io.github.md2java.lock.util.AnnotationUtil;
import io.github.md2java.lock.util.MemoryUtil;
import io.github.md2java.lock.util.NodeUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LockServiceImpl implements LockService {

	@Override
	public Object applyLogic(ProceedingJoinPoint pjp) {
		Object ret = null;
		ClusterLock clusterLock = AnnotationUtil.annotatedObject(pjp, ClusterLock.class);
		LockInfo lockInfo = MemoryUtil.getLockInfo(clusterLock.name());
		if (isActiveNodeSame(lockInfo)) {
			try {
				ret = pjp.proceed(pjp.getArgs());
				lockInfo.setLastrun(LocalDateTime.now());
			} catch (Throwable e) {
				log.error("something went wrong: {} ", e.toString());
			}
		}
		return ret;
	}

	private boolean isActiveNodeSame(LockInfo lockInfo) {
		return StringUtils.equalsIgnoreCase(NodeUtil.hostId(), lockInfo.getActiveNode());
	}

}
