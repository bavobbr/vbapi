import forum.model.Credentials
import forum.ForumService
import forum.model.Member

Credentials credentials = new Credentials(username: "user", password: "pass")
ForumService postService = new ForumService(credentials, "http://forum.shrimprefuge.be")
def membs = postService.collectMembers(500)
println "Found: ${membs.size()}"

def today = new Date()
def lastyear = today.minus(365)
membs.retainAll {
  println "${it.lastActive} ${lastyear}"
  it.lastActive == null || it.lastActive > lastyear
}

membs.each { Member member ->
  println "active: $member.username $member.totalPosts $member.lastActive"
}
println membs.size()

def file = new File("/home/bavobbr/svhjvotes.txt")
def content = file.text
boolean firstLine = true
def voters
def allvoters = [] as Set
content.eachLine {
  if (firstLine) {
    firstLine = false
  }
  else {
    voters = it
    firstLine = true
    def votelist = voters.split(",")
    votelist = votelist.collect { it.trim() }
    allvoters.addAll(votelist)
  }
}
println "Counted voters on Shrimper vhj ${allvoters.size()}"

file = new File("/home/bavobbr/lvhj2012.txt")
content = file.text
firstLine = true
def allvoters2 = [] as Set
content.eachLine {
  if (firstLine) {
    firstLine = false
  }
  else {
    voters = it
    firstLine = true
    def votelist = voters.split(",")
    votelist = votelist.collect { it.trim() }
    allvoters2.addAll(votelist)
  }
}
println "Counted voters with Loser vhj ${allvoters2.size()}"

allvoters.retainAll(allvoters2)
println "Counted voters ${allvoters.size()}"

membs.removeAll {
  allvoters.contains(it.username)
}
println "${membs.collect { it.username}}"
println "Population is ${membs.size()}"

def sent = ["andu","Sentinel","Niels","Teki","Matthias","DeathKnight","TheAccountant","Awoujol","Fieldy","Anton","Jonah","kar","Cybdm","Leeuwke","blumcole","Turtle","Takeshi","Mahershalalhash"]
membs.removeAll {
  sent.contains(it.username)
}

def pmfile = new File("/home/bavobbr/pm.txt")
def text = pmfile.text

membs.each {
  def target = it.username
  def pm = target+"\n"+text
  postService.message(target, "Verkiezingen 2012", pm)
  sleep 15000
}
