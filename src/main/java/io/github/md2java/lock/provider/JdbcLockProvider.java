package io.github.md2java.lock.provider;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import io.github.md2java.lock.model.LockInfo;
import io.github.md2java.lock.util.Constants;
import io.github.md2java.lock.util.DBUtil;
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

	private String driverClassName;

	@Override
	public void initilize() {
		node = NodeUtil.hostId();
		jdbcTemplate = new JdbcTemplate(dataSource);
		this.driverClassName = DBUtil.getDriverClassName(dataSource);
		createTableIfNotExist(Constants.lockTableName);
	}

	@Override
	public Map<String, Object> updateLockInfo(LockInfo lockInfo) {
		return updateLastRun(lockInfo.getLockname(), lockInfo);
	}

	@Override
	public Map<String, Object> updateNodeInfo(LockInfo lockInfo) {
		return updateSwitchNode(lockInfo.getLockname(), lockInfo);
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

	@Override
	public Map<String, Object> findLockInfo(String lockname) {
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

}
