package io.github.md2java.lock.service;

import java.util.Map;

public interface ProviderWrapper {

	void initilize();
	void monitorAll();
	 Map<String, Object> updateLock(String lockname);

}
