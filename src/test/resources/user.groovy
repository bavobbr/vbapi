import forum.ForumService
import forum.model.Credentials

Credentials credentials = new Credentials(username: "x", password: "y")
ForumService postService = new ForumService(credentials, "http://forum.shrimprefuge.be")
def thread = postService.getThread(6514, 50, 2)
println thread.title
thread.posts.each {
  println it.username
}