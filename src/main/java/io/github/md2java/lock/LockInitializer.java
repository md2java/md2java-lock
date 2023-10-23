package io.github.md2java.lock;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import io.github.md2java.lock.annotation.ClusterLock;
import io.github.md2java.lock.annotation.EnableClusterLock;
import io.github.md2java.lock.provider.LockProvider;
import io.github.md2java.lock.provider.UpdateLockScheduler;
import io.github.md2java.lock.util.BeanScannerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class LockInitializer implements ApplicationListener<ContextRefreshedEvent> {

	private final LockProvider lockProvider;
	private final ApplicationContext applicationContext;
	private final BeanScannerUtil beanScannerUtil;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		init();
	}

	public void init() {
		beanScannerUtil.init();
		lockProvider.init();
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
			log.info("scheduler intialized: {} ",scheduler);
		});
		log.info("Initialized: {} ", this);
	}

}
