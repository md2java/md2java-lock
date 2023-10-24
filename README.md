# MD2JAVA-LOCK

    Why:
    To manage single execution of any spring bean 
    method[scheduler,jmslistener..etc] across cluster[multiple instance]
   
	prerequisite:
	------------------
	1) Spring boot application
	2) LockProvider bean
	
	how to use:
	------------
	1) add mvn dependency
     	<dependency>
			<groupId>io.github.md2java</groupId>
			<artifactId>md2java-lock</artifactId>
			<version>1.0.0-SNAPSHOT</version>
		</dependency>
			
	2) apply EnableXXX
	 @EnableClusterLock(monitorAt = 45*1000,updateAt = 30*1000)
	 here monitorAt --background job keep checking activenode is working fine
	  and updateAt --background job keep updating on lockprovider about lastrun.
	  
	3)create bean of lockprovider- supported lockprovider is JdbcLockProvider [h2,mysql,postgresql,sqlserver],
	
	@Bean
	public LockProvider lockProvider(DataSource dataSource) {
		JdbcLockProvider jdbcLockProvider = new JdbcLockProvider(dataSource);
		return jdbcLockProvider;
	}
	
	4) apply ClusterLock on method[wherever you needed it]
	  @ClusterLock(name = "logTest11") 
	  here name must be unique
	  
	
	
	