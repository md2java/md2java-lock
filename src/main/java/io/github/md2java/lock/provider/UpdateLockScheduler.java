package io.github.md2java.lock.provider;

import java.util.Set;

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
	
	private Set<String> locknames;
	
	@Override
	public void run() {
		log.info("updateLock started..");
		getLocknames().stream().forEach(lockname-> {
			LockInfo lockInfo = MemoryUtil.getLockInfo(lockname);
			if (StringUtils.equalsIgnoreCase(lockInfo.getActiveNode(), NodeUtil.hostId())) {
				lockProvider.updateLock(lockname);
			}			
		});
		log.info("updateLock end..");
	}

	public Set<String> getLocknames() {
		return locknames;
	}

	public void setLocknames(Set<String> locknames) {
		this.locknames = locknames;
	}

	
	
	
	
}
