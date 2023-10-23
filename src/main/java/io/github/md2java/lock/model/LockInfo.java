package io.github.md2java.lock.model;

import java.time.LocalDateTime;

import io.github.md2java.lock.annotation.ClusterLock;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LockInfo {
	private String lockname;
	private LocalDateTime lastrun;
	private String activeNode;
	private ClusterLock clusterLock;

}
