package littlebigdata

import groovy.sql.Sql

def nextMonth = { Date date ->
	Calendar cal = new GregorianCalendar()
	cal.setTime(date)
	int month = cal.get(Calendar.MONTH)
	if (month < 11) cal.set(Calendar.MONTH, month+1)
	else {
		cal.set(Calendar.MONTH, 0)
		cal.set(Calendar.YEAR, cal.get(Calendar.YEAR)+1)
	}
	return cal.getTime()
}

def sql = Sql.newInstance("jdbc:mysql://localhost:3306/shrimp", "root", "", "com.mysql.jdbc.Driver")

Date dateStart = new Date()
dateStart.set(year: 2007, month: 0, date: 1)
Date dateEnd = new Date()
dateEnd.set(year: 2007, month: 1, date: 1)

List results = []
while (dateStart.before(new Date()))
{
	def sqlStart = dateStart.format("yyyy-MM-dd 00:00:00")
	def sqlEnd = dateEnd.format("yyyy-MM-dd 00:00:00")

	println "Querying between $sqlStart and $sqlEnd"

	def validPosts = 0
	def deleted = 0
	def active

	sql.eachRow("select count(*) from Post where date > ${sqlStart} and date < ${sqlEnd} and valid = 1") {
		validPosts = it[0]
		println validPosts
	}
	sql.eachRow("select count(*) from Post where date > ${sqlStart} and date < ${sqlEnd} and valid = 0") {
		deleted = it[0]
		println deleted
	}
	Map users = [:]
	sql.eachRow("select username, count(*) from Post where date > ${sqlStart} and date < ${sqlEnd} group by username") {
		//if ((it[1] as Integer) >= 10) {
			users[it[0]] = it[1]
		//}
	}
	active = users.size()
	println active
	results << [dateStart, dateEnd, validPosts+deleted, validPosts, deleted, active]
	dateStart = nextMonth(dateStart)
	dateEnd = nextMonth(dateEnd)
}
println "year, total, valid, deleted/admin, users"

Integer total = 0
Integer valid = 0
Integer deleted = 0
Integer users = 0
Integer year = 2007
results.each { List data ->
	total+= data[2] as Integer
	valid+= data[3] as Integer
	deleted+= data[4] as Integer
	users+= data[5] as Integer

	Calendar cal = new GregorianCalendar()
	cal.setTime(data[0])

	int month = cal.get(Calendar.MONTH)
	if (month == 11) {
		println "$year, $total, $valid, $deleted, ${users/12 as Long}"
		total = valid = deleted = users = 0
		year = cal.get(Calendar.YEAR)+1
	}
}
println "$year, $total, $valid, $deleted, ${users/12 as Long}"
println "$year, ${total/12 as Long}, ${valid/12 as Long}, ${deleted/12 as Long}, ${users/12 as Long}"