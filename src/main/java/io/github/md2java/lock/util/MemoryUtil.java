package io.github.md2java.lock.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.md2java.lock.model.LockInfo;

public class MemoryUtil {
	private static Map<String, LockInfo> data = new ConcurrentHashMap<>();

	public static LockInfo getLockInfo(String name) {
		return data.get(name);
	}

	public static void updateLockInfo(String name, LockInfo info) {
		data.put(name, info);
	}

}
