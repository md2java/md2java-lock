package io.github.md2java.lock.provider;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.github.md2java.lock.model.LockInfo;
import io.github.md2java.lock.util.MemoryUtil;
import io.github.md2java.lock.util.NodeUtil;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope("prototype")
@Slf4j
public class UpdateLockScheduler  implements Runnable{
	
	@Autowired
	private LockProvider lockProvider;
	
	private String lockname;
	
	@Override
	public void run() {
		log.info("updateLock started..");
		LockInfo lockInfo = MemoryUtil.getLockInfo(lockname);
		if (StringUtils.equalsIgnoreCase(lockInfo.getActiveNode(), NodeUtil.hostId())) {
			lockProvider.updateLock(getLockname());
		}
	}

	public String getLockname() {
		return lockname;
	}


	public void setLockname(String lockname) {
		this.lockname = lockname;
	}
	
	
	
}
