import forum.*
import forum.model.Post
import forum.model.User
import forum.model.Vote
import forum.model.Day
import forum.utils.VoteUtils
import forum.utils.HitMap

import forum.ForumService
import forum.model.Credentials

def newday = 0
def postThreadId = 16932
def readThreadId = 16914
Day day = new Day()
Credentials credentials = new Credentials(username: "x", password: "y")
ForumService postService = new ForumService(credentials, "http://forum.shrimprefuge.be")

List<Post> posts = postService.scanPosts(readThreadId);
//posts = posts.findAll { it.postNumber >  newday }

List<Post> votePosts = posts.findAll { Post post ->
    VoteUtils.isVote(post.message)
}
List<Vote> votes = VoteUtils.toVotes(votePosts)

day.addVotes(votes)

HitMap<User> goodHits = new HitMap()
HitMap<User> badHits = new HitMap()
day.allVotes.each {
    it.target.good ? goodHits << it.user : badHits << it.user
}
StringBuffer buffer = new StringBuffer()
buffer.append("Votes on GOOD\n")
buffer.append("[list]")
UserStore.getUsers().each {
    buffer.append("[*] ${it.coloredName}: ${goodHits.getHitCount(it)}")
}
buffer.append("[/list]")

buffer.append("Votes on EVIL")
buffer.append("[list]")
UserStore.getUsers().each {
    buffer.append("[*] ${it.coloredName}: ${badHits.getHitCount(it)}")
}
buffer.append("[/list]")

postService.post(postThreadId, "Hello world".reverse());



