package io.github.md2java.lock.provider;

import java.util.Map;

import io.github.md2java.lock.model.LockInfo;

public interface LockProvider {
	void initilize();
	Map<String ,Object> updateLockInfo(LockInfo lockInfo);
	Map<String ,Object> updateNodeInfo(LockInfo lockInfo);
	Map<String, Object> findLockInfo(String lockname);
	
}
