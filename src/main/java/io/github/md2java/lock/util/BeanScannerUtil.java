package io.github.md2java.lock.util;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import io.github.md2java.lock.annotation.ClusterLock;
import io.github.md2java.lock.annotation.EnableClusterLock;
import io.github.md2java.lock.model.LockInfo;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BeanScannerUtil {

	@Autowired
	private ApplicationContext applicationContext;

	private static Map<String, ClusterLock> configuredLocks;
	 private static EnableClusterLock enableClusterLock;

	@PostConstruct
	public void init() {
		enableClusterLock = findEnableClusterLock();
		configuredLocks = findAllClusterLock();
		log.info("configuredLocks: {} ", configuredLocks());
	}

	private EnableClusterLock findEnableClusterLock() {
		Collection<Object> values = applicationContext.getBeansWithAnnotation(EnableClusterLock.class).values();
		Optional<Object> findFirst = values.stream().findFirst();
		if (findFirst.isPresent()) {
			EnableClusterLock annotation = findFirst.get().getClass().getAnnotation(EnableClusterLock.class);
			return annotation;
		}
		return null;

	}	

	public Map<String, ClusterLock> findAllClusterLock() {
		Map<String, ClusterLock> ret = new ConcurrentHashMap<String, ClusterLock>();
		String[] beanNames = applicationContext.getBeanDefinitionNames();

		for (String beanName : beanNames) {
			Object bean = applicationContext.getBean(beanName);
			Class<?> beanClass = bean.getClass();
			for (Method method : beanClass.getDeclaredMethods()) {
				if (method.isAnnotationPresent(ClusterLock.class)) {
					ClusterLock clusterLock = method.getAnnotation(ClusterLock.class);
					if (ret.containsKey(clusterLock.name()) || isMonitorSpanLess(clusterLock)) {
						String messageTemplate = "duplicate Lockname: {} found or isMonitorSpanLess please correct and retry => {}";
						log.warn(messageTemplate, clusterLock.name(), clusterLock);
						System.exit(1);
					}
					ret.put(clusterLock.name(), clusterLock);
					LockInfo info = LockInfo.builder().clusterLock(clusterLock).lockname(clusterLock.name()).build();
					MemoryUtil.updateLockInfo(clusterLock.name(), info);
				}
			}
		}
		return ret;
	}

	private boolean isMonitorSpanLess(ClusterLock clusterLock) {
		return enableClusterLock.monitorAt() < clusterLock.updateAt()+1*60*100;
	}

	public static Map<String, ClusterLock> configuredLocks() {
		return configuredLocks;
	}

	public static EnableClusterLock enableClusterLock() {
		return enableClusterLock;
	}
}
