package io.github.md2java.lock.service;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import io.github.md2java.lock.model.LockInfo;
import io.github.md2java.lock.provider.LockProvider;
import io.github.md2java.lock.util.BeanScannerUtil;
import io.github.md2java.lock.util.MemoryUtil;
import io.github.md2java.lock.util.NodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProviderWrapperImpl implements ProviderWrapper {
	private final LockProvider lockProvider;

	@Override
	public void initilize() {
		lockProvider.initilize();
		Set<String> names = BeanScannerUtil.configuredLocks().keySet();
		names.forEach(s -> {
			monitorLock(s);
		});

	}

	@Override
	public void monitorAll() {
		Set<String> names = BeanScannerUtil.configuredLocks().keySet();
		names.forEach(s -> {
			LockInfo lockInfo = MemoryUtil.getLockInfo(s);
			if (Objects.nonNull(lockInfo)) {
				if (StringUtils.equalsIgnoreCase(NodeUtil.hostId(), lockInfo.getActiveNode())) {
					log.debug("skipped because activenode is the current node");
					return;
				}
			}
			monitorLock(s);
		});

	}

	private void monitorLock(String lockName) {
		log.debug("monitor scheduler started...");
		Map<String, Object> lockInfo = lockProvider.findLockInfo(lockName);
		MemoryUtil.updateLockInfo(lockName, lockInfo);
		if (isNeedToSwitchNode(lockInfo)) {
			switchNode(lockName);
			lockInfo = lockProvider.findLockInfo(lockName);
			MemoryUtil.updateLockInfo(lockName, lockInfo);
		}

	}

	private void switchNode(String lockName) {
		LockInfo updateLock = LockInfo.builder().activeNode(NodeUtil.hostId()).lockname(lockName).lastrun(new Date())
				.build();
		Map<String, Object> lockDetails = lockProvider.updateNodeInfo(updateLock);
		if (Objects.nonNull(lockDetails)) {
			log.debug("lock switched node to : {} ", updateLock.getActiveNode());
		}
	}

	private boolean isNeedToSwitchNode(Map<String, Object> lockInfo) {
		LockInfo lockInfoModel = MemoryUtil.getLockInfo(String.valueOf(lockInfo.get("name")));
		Date lastrun = lockInfoModel.getLastrun();
		Date now = new Date();
		long updateAt = MemoryUtil.getEnableClusterLock().updateAt();
		if ((now.getTime() - lastrun.getTime()) > (updateAt + 100)) {
			return true;
		}
		return false;
	}

	@Override
	public Map<String, Object> updateLock(String lockname) {
		LockInfo lockInfo = MemoryUtil.getLockInfo(lockname);
		Map<String, Object> updateLastRun = lockProvider.updateLockInfo(lockInfo);
		return updateLastRun;
	}
}
