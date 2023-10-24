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
	1) add dependency
	2) apply EnableXXX 
	3) apply ClusterLock on method[wherever you needed it]
	
	
	
	
