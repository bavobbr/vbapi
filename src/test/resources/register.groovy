import forum.ForumService

def email = "elliotmissy.568"
10.times {
    ForumService postService = new ForumService(null, "http://forum.shrimprefuge.be")
    def uniqueMail = email[0..it]+"."+email[it+1..-1]
    def uniqueName = "acku$it"
    postService.register(uniqueName, "bumbalu", uniqueMail+"@gmail.com")
    println "registered $uniqueMail as $uniqueName"
}

