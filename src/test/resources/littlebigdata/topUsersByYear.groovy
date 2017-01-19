package littlebigdata

import forum.utils.HitMap
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

List<HitMap> results = []
while (dateStart.before(new Date()))
{
	def sqlStart = dateStart.format("yyyy-MM-dd 00:00:00")
	def sqlEnd = dateEnd.format("yyyy-MM-dd 00:00:00")

	println "Querying between $sqlStart and $sqlEnd"

	HitMap<String> hits = new HitMap()
	sql.eachRow("select username, count(*) from Post where date > ${sqlStart} and date < ${sqlEnd} and valid = 1 group by username") {
		hits.addHits(it[0], it[1] as Integer)
	}
	results << hits
	dateStart = nextYear(dateStart)
	dateEnd = nextYear(dateEnd)
}

def topresults = results.collect { it.toSortedMap().take(300) }

def compareAndPrint = { LinkedHashMap a, LinkedHashMap b, int year ->
	File out = new File("/Users/bbr/Desktop/hitlist_${year}.csv")
	out.delete()
	out << "year $year\n"
	print "year $year\n"
	out << "rank, user, posts, previous rank, difference\n"
	print "rank, user, posts, previous rank, difference\n"
	List rankedUsers = []
	List derankedUsers = []
	List newUsers = []
	List byeUsers = []
	b.eachWithIndex {  k, v, idx ->
		def currentRank = idx +1
		def previousRank = "-"
		def diff = "-"
		if (a[k]) {
			previousRank = a.findIndexOf { it.key == k } + 1
			diff = previousRank - currentRank
		}
		out << "$currentRank, $k, $v, $previousRank, $diff\n"
		print "$currentRank, $k, $v, $previousRank, $diff\n"
		if (diff instanceof Number && currentRank <= 50) rankedUsers << [name: k, posts: v, diff: diff]
		if (diff instanceof String && currentRank <= 200) newUsers << [name: k, posts: v, rank: currentRank]
		if (diff instanceof Number && previousRank instanceof Number && previousRank <= 50) derankedUsers << [name: k, posts: v, diff: diff]
	}
	a.eachWithIndex{ k, v, idx ->
		if (!b[k]) {
			byeUsers << [name: k, rank: idx+1]
		}
	}

	out << "\n"
	out << "Going up\n"
	rankedUsers.sort { ua, ub -> ub.diff <=> ua.diff }.take(10).each {
		out << "${it.name}, ${it.diff}\n"
	}
	out << "\n"
	out << "Going down\n"
	derankedUsers.sort { ua, ub -> ua.diff <=> ub.diff }.take(10).each {
		out << "${it.name}, ${it.diff}\n"
	}
	out << "\n"
	out << "Coming in\n"
	newUsers.sort { ua, ub -> ua.rank <=> ub.rank }.take(20).each {
		out << "${it.name}, ${it.rank}\n"
	}
	out << "\n"
	out << "Going out\n"
	byeUsers.sort { ua, ub -> ua.rank <=> ub.rank }.take(20).each {
		out << "${it.name}, ${it.rank}\n"
	}
}

File out = new File("/Users/bbr/Desktop/hitlist_2007.csv")
out.delete()
out << "year 2007\n"
print "year 2007\n"
out << "rank, user, posts, previous rank, difference\n"
print "rank, user, posts, previous rank, difference\n"
topresults.first().eachWithIndex {  k, v, idx ->
	def currentRank = idx +1
	out << "$currentRank, $k, $v, -, -\n"
	print "$currentRank, $k, $v, -, -\n"
}

def year = 2008
2.upto(topresults.size()) {
	compareAndPrint(topresults[it-2], topresults[it-1], year)
	year++
}

