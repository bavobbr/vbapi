import forum.utils.HitMap

def file = new File("/home/bavobbr/svhjvotes.txt")
def modsfile = new File("/home/bavobbr/mods.txt")
def alpfile = new File("/home/bavobbr/alpkru.txt")
def content = file.text
boolean firstLine = true
def name = null
def voters
def votemap = [:]
content.eachLine {
  if (firstLine) {
    name = it
    firstLine = false
  }
  else {
    voters = it
    firstLine = true
    def votelist = voters.split(",")
    votelist = votelist.collect { it.trim() }
    votemap.put(name, votelist)
  }
}
println votemap
votemap.each { def k, v ->
  println "$k ${v.size()}"
}

HitMap<String> hitmap = new HitMap<>()
votemap.each { def k, v ->
  v.each { String voter -> hitmap.addHit(voter) }
}
def illegalvotes = []
hitmap.each { def k, v ->
  if (v > 3) {
    println "illegal voter: $k $v"
    illegalvotes << k
  }
}
def mods = modsfile.text.readLines()
mods = mods.collect { it.trim() }
println mods
def alpkru = alpfile.text.readLines()
alpkru = alpkru.collect { it.trim() }
println alpkru
votemap.each { def k, List v ->
  v.removeAll(mods)
  //v.removeAll(alpkru)
  //v.removeAll(illegalvotes)
}
votemap.each { def k, v ->
  println "${v.size()}"
}