package io.github.md2java.lock.util;

public enum QueryList {

	H2("CREATE TABLE CLUSTERLOCKINFO (Id INT AUTO_INCREMENT PRIMARY KEY,name VARCHAR(255),lastrun TIMESTAMP,activenode VARCHAR(255));"),
	MYSQL("CREATE TABLE CLUSTERLOCKINFO (Id INT AUTO_INCREMENT PRIMARY KEY,name VARCHAR(255),lastrun TIMESTAMP,activenode VARCHAR(255));")
	;
   
	private String query;
	
	private QueryList(String query) {
		this.query = query;
		
	}

	public String query() {
		return query;
	}
	
    
}
