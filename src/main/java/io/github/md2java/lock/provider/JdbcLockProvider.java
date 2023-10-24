package io.github.md2java.lock.provider;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import io.github.md2java.lock.annotation.ClusterLock;
import io.github.md2java.lock.model.LockInfo;
import io.github.md2java.lock.util.BeanScannerUtil;
import io.github.md2java.lock.util.Constants;
import io.github.md2java.lock.util.DBUtil;
import io.github.md2java.lock.util.MemoryUtil;
import io.github.md2java.lock.util.NodeUtil;
import io.github.md2java.lock.util.QueryList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class JdbcLockProvider implements LockProvider {

	private final DataSource dataSource;
	private String node;
	private JdbcTemplate jdbcTemplate;
	private Map<String, ClusterLock> configuredLocks;

	private String driverClassName;

	@Override
	public void init() {
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
			QueryList[] values = QueryList.values();
			String createTableQuery = null;
			for (QueryList queryList : values) {
				boolean contains = StringUtils.contains(driverClassName.toUpperCase(), queryList.name());
				if (contains) {
					createTableQuery = queryList.query();
					break;
				}
			}
			if (StringUtils.isBlank(createTableQuery)) {
				throw new RuntimeException("something went wrong to find create table query.");
			}
			try {
				jdbcTemplate.execute(createTableQuery);
			} catch (Exception e) {
				log.error("something went wrong to to execute query: {} => {} ", createTableQuery, e.toString());
				throw e;
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
		log.debug("monitor scheduler started...");
		Map<String, Object> lockInfo = findLockInfo(lockName);
		MemoryUtil.updateLockInfo(lockName, lockInfo);
		if (isNeedToSwitchNode(lockInfo)) {
			switchNode(lockName);
			lockInfo = findLockInfo(lockName);
			MemoryUtil.updateLockInfo(lockName, lockInfo);
		}
		return lockInfo;
	}

	private void switchNode(String lockName) {
		LockInfo updateLock = LockInfo.builder().activeNode(node).lockname(lockName).lastrun(new Date()).build();
		Map<String, Object> lockDetails = updateSwitchNode(lockName, updateLock);
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

	private boolean doesTableExist(String tableName) {
		try {
			jdbcTemplate.queryForObject("SELECT 1 FROM " + tableName + " WHERE 1 = 0", Integer.class);
			return true;
		} catch (EmptyResultDataAccessException e) {
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private Map<String, Object> findLockInfo(String lockname) {
		Map<String, Object> mapData = null;
		try {
			mapData = jdbcTemplate.queryForMap("SELECT * FROM " + Constants.lockTableName + " WHERE name =? ",
					lockname);
			return mapData;
		} catch (EmptyResultDataAccessException e) {
			jdbcTemplate.update(
					String.format("INSERT INTO %s  (name,lastrun,activenode) VALUES(?,?,?)", Constants.lockTableName),
					lockname, Timestamp.valueOf(LocalDateTime.now()), node);
			mapData = jdbcTemplate.queryForMap("SELECT * FROM " + Constants.lockTableName + " WHERE name = ?",
					lockname);
			return mapData;
		} catch (Exception e) {
			return null;
		}
	}

	private Map<String, Object> updateLastRun(String lockname, LockInfo lockInfo) {
		Map<String, Object> mapData = null;
		try {
			int update = jdbcTemplate.update(
					String.format("UPDATE %s set lastrun=? where name=? and activenode=?", Constants.lockTableName),
					lockInfo.getLastrun(), lockname, lockInfo.getActiveNode());
			if (update > 0) {
				mapData = buildResponse(lockname, lockInfo.getActiveNode(), lockInfo.getLastrun());
			}
			return mapData;
		} catch (Exception e) {
			return null;
		}
	}

	private Map<String, Object> updateSwitchNode(String lockname, LockInfo lockInfo) {
		Map<String, Object> mapData = null;
		try {
			int update = jdbcTemplate.update(
					String.format("UPDATE %s set lastrun=? ,activenode=? where name=? ", Constants.lockTableName),
					lockInfo.getLastrun(), lockInfo.getActiveNode(), lockname);
			if (update > 0) {
				mapData = buildResponse(lockname, lockInfo.getActiveNode(), lockInfo.getLastrun());
			}
			return mapData;
		} catch (Exception e) {
			log.error("something went wrong: ", e);
			return null;
		}
	}

	private Map<String, Object> buildResponse(String lockname, String node, Date timestamp) {
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
			LockInfo lockInfo = MemoryUtil.getLockInfo(s);
			if (Objects.nonNull(lockInfo)) {
				if (StringUtils.equalsIgnoreCase(node, lockInfo.getActiveNode())) {
					log.debug("skipped because activenode is the current node");
					return;
				}
			}
			monitorLock(s);
		});
	}

}
