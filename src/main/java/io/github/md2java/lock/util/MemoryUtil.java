package io.github.md2java.lock.util;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Objects;
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

	public static void updateLockInfo(String name, Map<String, Object> info) {
		LockInfo lockInfo = data.get(name);
		if (Objects.isNull(lockInfo)) {
			lockInfo = LockInfo.builder().lockname(name).build();
		}
		lockInfo.setActiveNode(String.valueOf(info.get("activenode")));
		Timestamp lastRun = (Timestamp) info.get("lastrun");
		lockInfo.setLastrun(lastRun.toLocalDateTime());
		data.put(name, lockInfo);
	}

}
