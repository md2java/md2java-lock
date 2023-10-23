package io.github.md2java.lock;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import io.github.md2java.lock.provider.LockProvider;
import io.github.md2java.lock.util.BeanScannerUtil;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ConditionalOnBean(LockProvider.class)
@ComponentScan
@Slf4j
public class LockAutoConfiguration {

	@Autowired
	private BeanScannerUtil beanScannerUtil;

	@PostConstruct
	public void init() {
		log.info("Initialized: {} ", this);
		beanScannerUtil.findAllClusterLock();
	}

}
