package io.github.md2java.lock.provider;

import java.util.Map;

public interface LockProvider {

	Map<String ,Object> updateLock();
	Map<String ,Object> monitorLock();
}
