package littlebigdata

import groovy.sql.Sql

import java.text.SimpleDateFormat

def sql = Sql.newInstance("jdbc:mysql://localhost:3306/shrimp", "root", "", "com.mysql.jdbc.Driver")

def df = new SimpleDateFormat("yyyy-MM-dd mm:HH:ss")

Map<String, Integer> users = [:]
sql.eachRow("select username, count(*) as cnt from Post group by username order by cnt desc limit 300") {
	users[it.username] = it.cnt
}

Map<String, List<Map>> userdata = [:]

users.take(300).each { String name, Long cnt ->
	def years = []
	sql.eachRow("select date from Post where username=${name} order by id asc") {
	//sql.eachRow("select date from Post where username=${name} order by id asc") {
		if (it.date) {
			def mydate = df.parse(it.date.toString())
			def year = mydate.toCalendar().get(Calendar.YEAR)
			years << year
		}
	}
	def byear = years.countBy { it }
	def sorted = byear.sort { a,b -> a.key <=> b.key }
	println sorted
	userdata[name] = sorted
}

def lost = [:]
def returned = [:]

userdata.each {
	println it
	def missingYears = []
	Map cnts = it.value
	Integer start = cnts.min { it.key }.key
	Integer end = 2022
	def range = start..end
	println range
	def diff = range - cnts.keySet()
	println diff
	if (diff.contains(2022)) {
		lost[it.key] = diff[0]
	}
	else if (diff) {
		returned[it.key] = diff
	}
}

println "\nLOST"
def lostbyyear = lost.groupBy { it.value }.sort { it.key }
lostbyyear.each {
	def names = it.value.keySet()
	def sortednames = names.sort { a, b -> users[b] <=> users[a] }
	println "$it.key ${sortednames}"
}


println "\nMISSING"
def sortedreturned = returned.sort { a, b -> users[b.key] <=> users[a.key] }
sortedreturned.each {
	println "$it.key in ${it.value.join(", ") }"
}
