package io.github.md2java.lock.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class UpdateLockScheduler  implements Runnable{
	
	@Autowired
	private LockProvider lockProvider;
	
	private String lockname;
	
	
	@Override
	public void run() {
		lockProvider.updateLock(getLockname());
		
	}

	public String getLockname() {
		return lockname;
	}


	public void setLockname(String lockname) {
		this.lockname = lockname;
	}
	
	
	
}
