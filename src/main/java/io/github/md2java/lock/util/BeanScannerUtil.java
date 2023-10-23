package io.github.md2java.lock.util;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import io.github.md2java.lock.annotation.ClusterLock;
import io.github.md2java.lock.model.LockInfo;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BeanScannerUtil {

	@Autowired
	private ApplicationContext applicationContext;

	public Map<String, ClusterLock> findAllClusterLock() {
		Map<String, ClusterLock> ret = new ConcurrentHashMap<String, ClusterLock>();
		String[] beanNames = applicationContext.getBeanDefinitionNames();

		for (String beanName : beanNames) {
			Object bean = applicationContext.getBean(beanName);
			Class<?> beanClass = bean.getClass();
			for (Method method : beanClass.getDeclaredMethods()) {
				if (method.isAnnotationPresent(ClusterLock.class)) {
					ClusterLock clusterLock = method.getAnnotation(ClusterLock.class);
					if (ret.containsKey(clusterLock.name())) {
						String messageTemplate = "duplicate Lockname: {} found please correct and retry => {}";
						log.warn(messageTemplate, clusterLock.name(), clusterLock);
						System.exit(1);
					}
					LockInfo info = LockInfo.builder().clusterLock(clusterLock).lockname(clusterLock.name()).build();
					MemoryUtil.updateLockInfo(clusterLock.name(), info);
				}
			}
		}
		return ret;
	}
}
