# MD2JAVA-LOCK

	EnableXXX annotation
	ClusterLock(monitorAt=100s,updateAt=20s,name=unique)
	LockProvider interface=>{monitorLock(),uploadLock()} with one jdbcLockProvider impl
	List{lockname,lastrun,activeNode}
	scheduler for monitorLock()[if activenode is different],uploadLock()[if activenode is same]
	check duplicate lockname at booting time of apps
	jdbc table{lockname,lastrun,activenode}
	DLL query[application-sql.properties]{driver-class=query}
