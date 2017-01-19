package littlebigdata

import groovy.sql.Sql

def nextYear = { Date date ->
	Calendar cal = new GregorianCalendar()
	cal.setTime(date)
	cal.set(Calendar.YEAR, cal.get(Calendar.YEAR)+1)
	return cal.getTime()
}

def sql = Sql.newInstance("jdbc:mysql://localhost:3306/shrimp", "root", "", "com.mysql.jdbc.Driver")

Date dateStart = new Date()
dateStart.set(year: 2007, month: 0, date: 1)
Date dateEnd = new Date()
dateEnd.set(year: 2008, month: 0, date: 1)

List results = []
while (dateStart.before(new Date()))
{
	def sqlStart = dateStart.format("yyyy-MM-dd 00:00:00")
	def sqlEnd = dateEnd.format("yyyy-MM-dd 00:00:00")

	println "Querying between $sqlStart and $sqlEnd"

	Map users = [:]
	sql.eachRow("select username, count(*) from Post where date > ${sqlStart} and date < ${sqlEnd} and valid = 1 group by username") {
		users[it[0]] = it[1]
	}
	results << [dateStart, users]
	dateStart = nextYear(dateStart)
	dateEnd = nextYear(dateEnd)
}
println "start date, active users, hardcore users"
results.each { List data ->
	def start = data[0].format("yyyy")
	print start+", "
}
println "\n"
1.upto(50) { idx ->
	results.each { List data ->
		Map map = data[1]
		LinkedHashMap sorted = map.sort { a, b -> b.value <=> a.value }
		def aslist = sorted.collect { [it.key, it.value] }
		def person = aslist[idx-1]
		print person[0]+", "
	}
	print "\n"
}