package littlebigdata

import groovy.sql.Sql


def sql = Sql.newInstance("jdbc:mysql://localhost:3306/shrimp", "root", "", "com.mysql.jdbc.Driver")

sql.eachRow("select id, postId from Post where date is NULL") {
	def dbId = it[0] as Integer
	def prev = dbId -1
	println "Looking to fix $dbId with previous $prev"
	def prevDate
	sql.eachRow("select date from Post where id = $prev") {
		prevDate = it[0]
	}
	println "Found date $prevDate"
	if (prevDate) {
		sql.execute("update Post set date = $prevDate where id = $dbId")
	}
}