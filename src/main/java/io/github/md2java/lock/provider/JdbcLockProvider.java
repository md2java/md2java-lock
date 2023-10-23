package io.github.md2java.lock.provider;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import io.github.md2java.lock.annotation.ClusterLock;
import io.github.md2java.lock.model.LockInfo;
import io.github.md2java.lock.util.BeanScannerUtil;
import io.github.md2java.lock.util.Constants;
import io.github.md2java.lock.util.DBUtil;
import io.github.md2java.lock.util.MemoryUtil;
import io.github.md2java.lock.util.NodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@DependsOn("beanScannerUtil")
@Slf4j
public class JdbcLockProvider implements LockProvider {

	private final DataSource dataSource;
	private String node;
	private JdbcTemplate jdbcTemplate;
	private Map<String, ClusterLock> configuredLocks;

	private String driverClassName;

	@Autowired
	private Environment env;

	@PostConstruct
	public void init() throws SQLException {
		node = NodeUtil.hostId();
		jdbcTemplate = new JdbcTemplate(dataSource);
		this.driverClassName = DBUtil.getDriverClassName(dataSource);
		configuredLocks = BeanScannerUtil.configuredLocks();
		createTableIfNotExist(Constants.lockTableName);
		Set<String> names = configuredLocks.keySet();
		names.forEach(s -> {
			monitorLock(s);
		});
		
		
	}

	private void createTableIfNotExist(String locktablename) {
		if (BooleanUtils.isFalse(doesTableExist(locktablename))) {
			String createTableQuery = env.getProperty(driverClassName);
			if (StringUtils.isBlank(createTableQuery)) {
				log.error("something went wrong to find create table query.");
				System.exit(1);
			}
			try {
				jdbcTemplate.execute(createTableQuery);
			} catch (Exception e) {
				log.error("something went wrong to to execute query: {} => {} ", createTableQuery, e.toString());
				System.exit(1);
			}
		}

	}

	@Override
	public Map<String, Object> updateLock(String lockName) {
		LockInfo lockInfo = MemoryUtil.getLockInfo(lockName);
		Map<String, Object> updateLastRun = updateLastRun(lockName, lockInfo);
		return updateLastRun;
	}


	@Override
	public Map<String, Object> monitorLock(String lockName) {
		Map<String, Object> lockInfo = findLockInfo(lockName);
		MemoryUtil.updateLockInfo(lockName, lockInfo);
		return lockInfo;
	}

	private boolean doesTableExist(String tableName) {
		try {
			jdbcTemplate.queryForObject("SELECT 1 FROM " + tableName + " WHERE 1 = 0", Integer.class);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private Map<String, Object> findLockInfo(String lockname) {
		Map<String, Object> mapData = null;
		try {
			mapData = jdbcTemplate
					.queryForMap("SELECT * FROM " + Constants.lockTableName + " WHERE name = " + lockname);
			return mapData;
		} catch (EmptyResultDataAccessException e) {
			jdbcTemplate.update(String.format("INSERT INTO %s VALUES(?,?,?)", Constants.lockTableName), lockname,
					Timestamp.valueOf(LocalDateTime.now()), node);
			mapData = jdbcTemplate
					.queryForMap("SELECT * FROM " + Constants.lockTableName + " WHERE name = " + lockname);
			return mapData;
		} catch (Exception e) {
			return null;
		}
	}

	private Map<String, Object> updateLastRun(String lockname,LockInfo lockInfo){
		Map<String, Object> mapData = null;
		try {
			Timestamp timestamp = Timestamp.valueOf(lockInfo.getLastrun());
			int update = jdbcTemplate.update(
					String.format("UPDATE %s set lastrun=? where name=? and activenode=?", Constants.lockTableName),
					timestamp, lockname, lockInfo.getActiveNode());
			if (update > 0) {
				mapData = buildResponse(lockname, lockInfo.getActiveNode(), timestamp);
			}
			return mapData;
		} catch (Exception e) {
			return null;
		}
	}

	private Map<String, Object> buildResponse(String lockname, String node, Timestamp timestamp) {
		Map<String, Object> mapData;
		mapData = new HashMap<String, Object>();
		mapData.put("name", lockname);
		mapData.put("lastrun", timestamp);
		mapData.put("activenode", node);
		return mapData;
	}

	@Override
	public void monitorAll() {
		Set<String> names = configuredLocks.keySet();
		names.forEach(s -> {
			monitorLock(s);
		});
	}

}