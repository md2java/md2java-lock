package io.github.md2java.lock.provider;

import java.util.Map;

public interface LockProvider {

	Map<String ,Object> updateLock(String lockName);
	Map<String ,Object> monitorLock(String lockName);
	void monitorAll();
	void init();
}
