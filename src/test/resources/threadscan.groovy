import forum.model.Post
import forum.model.Thread
import forum.utils.PostUtils
import forum.model.Credentials
import forum.ForumService

class Poster {
    String username
    Integer occurences = 0
    Map<String, Target> targets = [:]

    void hit(String username, Integer strength) {
        if (username != this.username) {
            Target target = findOrCreate(username)
            target.add(strength)
        }
    }

    private Target findOrCreate(String username) {
        if (targets[username]) {
            return targets[username]
        }
        else {
            Target target = new Target(username: username)
            targets.put(username, target)
            return target
        }
    }

    public List<Target> getSortedTargets() {
        targets.values().sort { Target target1, target2 ->
            target2.strength <=> target1.strength
        }
    }

    public List<Target> bestTargets(Integer count) {
        List<Target> sortedTargets = getSortedTargets()
        return sortedTargets.take(count)
    }
}

class Target {
    String username
    Integer strength = 0

    public void add(int hit) {
        strength += hit
    }

    public String toString() {
        return "${username}:${strength}"
    }


}

Map posters = [:]
Credentials credentials = new Credentials(username: "user", password: "pass")
ForumService postService = new ForumService(credentials, "http://forum.shrimprefuge.be")

def findOrCreate = { String username ->
    if (posters[username]) {
        return posters[username]
    }
    else {
        Poster poster = new Poster(username: username)
        posters.put(username, poster)
        return poster
    }
}

def getPreceding(List<Post> posts, Integer currentIndex, int numberToRevert) {
    int lookForId = currentIndex - numberToRevert
    if (lookForId < 0) return null
    else {
        return posts.get(lookForId)
    }
}

def analyzePost = { Post post ->
    Poster poster = findOrCreate(post.username)

    // FACTOR QUOTES
    Set<String> targets = PostUtils.getQuotedTargets(post)
    if (targets) {
        println "targets found in post ${post.postId}: ${targets}"
        targets.each {
            poster.hit(it, 10)
        }
    }
}

def analyzePostContext = { Post post, Integer index, List<Post> context ->
    Poster poster = findOrCreate(post.username)
    Post previous = getPreceding(context, index, 1)
    if (previous) {
        poster.hit(previous.username, 3)
    }
    Post previous2 = getPreceding(context, index, 2)
    if (previous2) {
        poster.hit(previous2.username, 2)
    }
    Post previous3 = getPreceding(context, index, 3)
    if (previous3) {
        poster.hit(previous3.username, 1)
    }
}

1.upto(3) { def pageId ->

    def ids = postService.getThreadIds(9, pageId)
    println ids

// OVERRIDE FOR TESTING
//ids = [17060]
    ids.each {
        Thread thread = postService.getThread(it, 500)
        println "title [${thread.title}] posts [${thread.posts.size()}]"
        thread.posts.eachWithIndex { Post post, Integer index ->
            Poster poster = findOrCreate(post.username)
            poster.occurences++
            try { analyzePost(post) } catch (e) { e.printStackTrace() }
            try { analyzePostContext(post, index, thread.posts) } catch (e) { e.printStackTrace() }
        }
    }
}


def sortedByVolume = posters.values().sort { Poster poster1, poster2 -> poster2.occurences <=> poster1.occurences}
sortedByVolume.each { Poster poster ->
    println "${poster.username} [${poster.occurences}] ${poster.bestTargets(10)}"
}

