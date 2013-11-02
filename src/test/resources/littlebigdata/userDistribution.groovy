package littlebigdata

import forum.utils.HitMap
import groovy.sql.Sql



def nextYear = {Date date ->
	Calendar cal = new GregorianCalendar()
	cal.setTime(date)
	cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 1)
	return cal.getTime()
}

def sql = Sql.newInstance("jdbc:mysql://localhost:3306/shrimp", "root", "", "com.mysql.jdbc.Driver")

Date dateStart = new Date()
dateStart.set(year: 2007, month: 0, date: 1)
Date dateEnd = new Date()
dateEnd.set(year: 2008, month: 0, date: 1)

List results = []
def blocksize = 50
while (dateStart.before(new Date()))
{
	def sqlStart = dateStart.format("yyyy-MM-dd 00:00:00")
	def sqlEnd = dateEnd.format("yyyy-MM-dd 00:00:00")

	println "Querying between $sqlStart and $sqlEnd"

	HitMap<Integer> percentiles = new HitMap<>()
	1.upto(20)  {
		percentiles.addHit(it)
	}
	sql.eachRow("select username, count(*) from Post where date > ${sqlStart} and date < ${sqlEnd} group by username") {
		Integer postnum = it[1] as Long
		println "$postnum ${it[0]}"


		if (postnum > 19 * blocksize) percentiles.addHit(20)
		else if (postnum > 18 * blocksize) percentiles.addHit(19)
		else if (postnum > 17 * blocksize) percentiles.addHit(18)
		else if (postnum > 16 * blocksize) percentiles.addHit(17)
		else if (postnum > 15 * blocksize) percentiles.addHit(16)
		else if (postnum > 14 * blocksize) percentiles.addHit(15)
		else if (postnum > 13 * blocksize) percentiles.addHit(14)
		else if (postnum > 12 * blocksize) percentiles.addHit(13)
		else if (postnum > 11 * blocksize) percentiles.addHit(12)
		else if (postnum > 10 * blocksize) percentiles.addHit(11)
		else if (postnum > 9 * blocksize) percentiles.addHit(10)
		else if (postnum > 8 * blocksize) percentiles.addHit(9)
		else if (postnum > 7 * blocksize) percentiles.addHit(8)
		else if (postnum > 6 * blocksize) percentiles.addHit(7)
		else if (postnum > 5 * blocksize) percentiles.addHit(6)
		else if (postnum > 4 * blocksize) percentiles.addHit(5)
		else if (postnum > 3 * blocksize) percentiles.addHit(4)
		else if (postnum > 2 * blocksize) percentiles.addHit(3)
		else if (postnum > 1 * blocksize) percentiles.addHit(2)
		else if (postnum <= blocksize) percentiles.addHit(1)
	}
	results << [dateStart, percentiles]
	dateStart = nextYear(dateStart)
	dateEnd = nextYear(dateEnd)
}

println "start date, ${(1..20).join(",")}"
results.each {List data ->
	def start = data[0].format("MM-dd-yyyy")
	HitMap hitMap = data[1]
	LinkedHashMap map = hitMap.asMap().sort { a, b -> a.key <=> b.key} as LinkedHashMap
	println "${start},${map.values().join(",")}"
}