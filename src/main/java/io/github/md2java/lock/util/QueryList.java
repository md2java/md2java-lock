package io.github.md2java.lock.util;

public enum QueryList {

	H2("CREATE TABLE CLUSTERLOCKINFO (Id INT AUTO_INCREMENT PRIMARY KEY,name VARCHAR(255),lastrun TIMESTAMP,activenode VARCHAR(255));"),
	MYSQL("CREATE TABLE CLUSTERLOCKINFO (Id INT AUTO_INCREMENT PRIMARY KEY,name VARCHAR(255),lastrun TIMESTAMP,activenode VARCHAR(255));"),
	POSTGRE("CREATE TABLE CLUSTERLOCKINFO (Id SERIAL PRIMARY KEY,name VARCHAR(255),lastrun TIMESTAMP,activenode VARCHAR(255));"),
	SQLSERVER("CREATE TABLE CLUSTERLOCKINFO (Id INT IDENTITY(1,1) PRIMARY KEY,name NVARCHAR(255),lastrun DATETIME,activenode NVARCHAR(255));");

	private String query;

	private QueryList(String query) {
		this.query = query;

	}

	public String query() {
		return query;
	}

}
