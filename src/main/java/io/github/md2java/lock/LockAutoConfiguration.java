package io.github.md2java.lock;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import io.github.md2java.lock.annotation.ClusterLock;
import io.github.md2java.lock.annotation.EnableClusterLock;
import io.github.md2java.lock.provider.LockProvider;
import io.github.md2java.lock.provider.UpdateLockScheduler;
import io.github.md2java.lock.util.BeanScannerUtil;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ConditionalOnBean(LockProvider.class)
@ComponentScan
@Slf4j
public class LockAutoConfiguration {

	@Autowired
	private LockProvider lockProvider;

	@Autowired
	private ApplicationContext applicationContext;

	@PostConstruct
	public void init() {
		EnableClusterLock enableClusterLock = BeanScannerUtil.enableClusterLock();
		ScheduledExecutorService newScheduledThreadPool = Executors.newScheduledThreadPool(2,
				new CustomizableThreadFactory("monitor_"));
		newScheduledThreadPool.scheduleWithFixedDelay(() -> lockProvider.monitorAll(), 100,
				enableClusterLock.monitorAt(), TimeUnit.MILLISECONDS);
		Map<String, ClusterLock> configuredLocks = BeanScannerUtil.configuredLocks();
		ScheduledExecutorService schedulerService = Executors.newScheduledThreadPool(configuredLocks.size(),
				new CustomizableThreadFactory("updateLock_"));

		configuredLocks.entrySet().stream().forEach(s -> {
			UpdateLockScheduler scheduler = applicationContext.getBean(UpdateLockScheduler.class);
			scheduler.setLockname(s.getKey());
			schedulerService.scheduleWithFixedDelay(scheduler, 1000, s.getValue().updateAt(), TimeUnit.MILLISECONDS);
		});
		log.info("Initialized: {} ", this);
	}

}
