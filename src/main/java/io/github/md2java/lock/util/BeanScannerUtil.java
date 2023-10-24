package io.github.md2java.lock.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import io.github.md2java.lock.annotation.ClusterLock;
import io.github.md2java.lock.annotation.EnableClusterLock;
import io.github.md2java.lock.model.LockInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeanScannerUtil {

	private final ApplicationContext applicationContext;
	private static Map<String, ClusterLock> configuredLocks;
	private static EnableClusterLock enableClusterLock;

	public void init() {
		enableClusterLock = findEnableClusterLock();
		configuredLocks = findAllClusterLock();
		log.debug("configuredLocks: {} ", configuredLocks());
	}

	private EnableClusterLock findEnableClusterLock() {
		Collection<String> values = applicationContext.getBeansWithAnnotation(EnableClusterLock.class).keySet();
		Optional<String> findFirst = values.stream().findFirst();
		if (findFirst.isPresent()) {
			Class<? extends Object> class1 = applicationContext.getBean(findFirst.get()).getClass();
			EnableClusterLock extractCustomAnnotation = extractCustomAnnotation(class1, EnableClusterLock.class);
			MemoryUtil.setEnableClusterLock(extractCustomAnnotation);
			return extractCustomAnnotation;
		}
		return null;

	}

	public Map<String, ClusterLock> findAllClusterLock() {
		Map<String, ClusterLock> ret = new ConcurrentHashMap<String, ClusterLock>();
		String[] beanNames = applicationContext.getBeanDefinitionNames();

		for (String beanName : beanNames) {
			Object bean = applicationContext.getBean(beanName);
			Class<?> beanClass = bean.getClass();
			if(StringUtils.contains(String.valueOf(beanClass), "$")) {
				beanClass = beanClass.getSuperclass();
			}
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
		return enableClusterLock.monitorAt() < enableClusterLock.updateAt() + 1 * 60 * 100;
	}

	public static Map<String, ClusterLock> configuredLocks() {
		return configuredLocks;
	}

	public static EnableClusterLock enableClusterLock() {
		return enableClusterLock;
	}

	public static <T extends Annotation> T extractCustomAnnotation(Class<?> clazz, Class<T> annotationType) {
		T annotation = clazz.getAnnotation(annotationType);

		if (annotation == null) {
			// If the class itself doesn't have the annotation, try to find it in its
			// superclasses
			Class<?> superclass = clazz.getSuperclass();
			while (superclass != null && annotation == null) {
				annotation = superclass.getAnnotation(annotationType);
				superclass = superclass.getSuperclass();
			}
		}

		return annotation;
	}

}
