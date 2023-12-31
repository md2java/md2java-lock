package io.github.md2java.lock.model;

import java.util.Date;

import io.github.md2java.lock.annotation.ClusterLock;
import io.github.md2java.lock.annotation.EnableClusterLock;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LockInfo {
	private String lockname;
	private Date lastrun;
	private Date lastnoderun;
	private String activeNode;
	private ClusterLock clusterLock;
	private EnableClusterLock enableClusterLock;

}
