package io.github.md2java.lock;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import io.github.md2java.lock.annotation.ClusterLock;
import io.github.md2java.lock.annotation.EnableClusterLock;
import io.github.md2java.lock.provider.UpdateLockScheduler;
import io.github.md2java.lock.service.ProviderWrapper;
import io.github.md2java.lock.util.BeanScannerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class LockInitializer implements ApplicationListener<ContextRefreshedEvent> {

	private final ProviderWrapper providerWrapper;
	private final UpdateLockScheduler updateLockScheduler;
	private final BeanScannerUtil beanScannerUtil;

	@Value("${lock.bg.threads.size:2}")
	private int bgThredsSize;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		init();
	}

	public void init() {
		beanScannerUtil.init();
		providerWrapper.initilize();
		EnableClusterLock enabledLock = BeanScannerUtil.enableClusterLock();
		Map<String, ClusterLock> configuredLocks = BeanScannerUtil.configuredLocks();

		CustomizableThreadFactory enableThreadGroup = new CustomizableThreadFactory("monitor-");
		ScheduledExecutorService enableSE = Executors.newScheduledThreadPool(bgThredsSize, enableThreadGroup);
		TimeUnit milli = TimeUnit.MILLISECONDS;
		enableSE.scheduleWithFixedDelay(providerWrapper::monitorAll, 100, enabledLock.monitorAt(), milli);

		CustomizableThreadFactory updateThreadGroup = new CustomizableThreadFactory("updateLock-");
		ScheduledExecutorService updateSE = Executors.newScheduledThreadPool(bgThredsSize, updateThreadGroup);
		updateLockScheduler.setLocknames(configuredLocks.keySet());
		updateSE.scheduleWithFixedDelay(updateLockScheduler, 1000, enabledLock.updateAt(), milli);
		log.debug("Initialized: {} ", this);
	}

}
