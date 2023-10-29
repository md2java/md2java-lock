package io.github.md2java.lock.provider;

import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.md2java.lock.model.LockInfo;
import io.github.md2java.lock.service.ProviderWrapper;
import io.github.md2java.lock.util.MemoryUtil;
import io.github.md2java.lock.util.NodeUtil;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UpdateLockScheduler implements Runnable {

	@Autowired
	private ProviderWrapper providerWrapper;

	private Set<String> locknames;

	@Override
	public void run() {

		getLocknames().stream().forEach(lockname -> {
			LockInfo lockInfo = MemoryUtil.getLockInfo(lockname);
			if (BooleanUtils.isFalse(StringUtils.equalsIgnoreCase(lockInfo.getActiveNode(), NodeUtil.hostId()))) {
				String msgTemplate = "skipped {} current node:{} is not activenode:{} ";
				log.debug(msgTemplate, lockInfo.getLockname(), NodeUtil.hostId(), lockInfo.getActiveNode());
				return;
			}
			log.debug("updateLock started..");
			providerWrapper.updateLock(lockname);
			log.debug("updateLock end..");
		});
	}

	public Set<String> getLocknames() {
		return locknames;
	}

	public void setLocknames(Set<String> locknames) {
		this.locknames = locknames;
	}

}
