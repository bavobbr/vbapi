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

	def active

	Map users = [:]
	Map hardcoreUsers = [:]
	sql.eachRow("select username, count(*) from Post where date > ${sqlStart} and date < ${sqlEnd} group by username") {
		if ((it[1] as Integer) >= 10){
			hardcoreUsers[it[0]] = it[1]
		}
		users[it[0]] = it[1]
	}
	active = users.size()
	hardActive = hardcoreUsers.size()
	results << [dateStart, active, hardActive]
	dateStart = nextMonth(dateStart)
	dateEnd = nextMonth(dateEnd)
}
println "start date, active users, hardcore users"
results.each { List data ->
	def start = data[0].format("yyyy-MM")
	println "${start},${data[1]}, ${data[2]}"
}