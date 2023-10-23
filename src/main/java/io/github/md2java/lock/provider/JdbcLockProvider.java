package io.github.md2java.lock.provider;

import java.util.Map;

import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JdbcLockProvider implements LockProvider {
	
	private DataSource dataSource;
	private String node;

	@Override
	public Map<String, Object> updateLock() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> monitorLock() {
		// TODO Auto-generated method stub
		return null;
	}

}
