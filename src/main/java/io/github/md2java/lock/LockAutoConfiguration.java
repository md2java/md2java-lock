package io.github.md2java.lock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@ConditionalOnProperty(havingValue = "true",name = "lock.enabled",matchIfMissing = true)
public class LockAutoConfiguration {
	
	
	
}
