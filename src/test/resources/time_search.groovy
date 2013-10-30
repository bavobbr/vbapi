import forum.model.Post

import forum.model.Credentials
import forum.ForumService

def keywords = ["*geld","lening","kapita*","beleg*","aande*","beurs*","finan*","kost","goedkoop","duur","lenen","bank","hypotheek","equit*","oblig*","loon","verloning","afbetal*","hedge","fonds"]

//def keywords = ["*.jpg*"]
def hits = [:]
def forumId = ""
def searchuser = ""
def maxresults = 99999
def yearStart = new GregorianCalendar(2012, Calendar.JANUARY, 1).getTime()

Credentials credentials = new Credentials(username: "user", password: "pass")
ForumService service = new ForumService(credentials, "http://forum.shrimprefuge.be")

def removeOld = { List list ->
    list.removeAll { it.date.before(yearStart)}
}

def removeMismatches = { List list, String term ->
    def strippedTerm = term.replace("*","").toUpperCase()
    list.removeAll { Post post -> !(post.message.toUpperCase() =~ strippedTerm)}
}

def processHits = { List list ->
    list.each { Post post ->
        def user = post.username
        def userhits = hits[user]
        if (userhits) hits[user] = userhits + 1
        else hits[user] = 1
    }
}

def processHitsPerTerm = { List list, String term ->
    def number = list.size()
    def userhits = hits[term]
    if (userhits) hits[term] = userhits + number
    else hits[term] = number

}

keywords.each { String term ->
    sleep 5000
    println "Scanning for $term"
    def posts = service.searchPosts(term, forumId, searchuser, maxresults)
    removeOld(posts)
    removeMismatches(posts, term)
    println "added ${posts.size()} posts for $term"
    processHits(posts)
    sorted = hits.sort { a, b -> b.value <=> a.value }
    println hits
}
def sorted = hits.sort { a, b -> b.value <=> a.value }
def top = []
sorted.each { def k, v ->
    top << "${k},${v}"
}
println keywords
println "Shrimper, Hits"
top.each {
    println it
}





