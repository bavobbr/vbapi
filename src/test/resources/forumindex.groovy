import forum.model.Credentials
import forum.ForumService

Credentials credentials = new Credentials(username: "user", password: "pass")
ForumService postService = new ForumService(credentials, "http://forum.shrimprefuge.be")
postService.changeSignature("PWND")
