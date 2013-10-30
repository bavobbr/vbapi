import forum.model.Post
import forum.ForumService
import forum.model.Credentials

Credentials credentials = new Credentials(username: "Shrimpwars Bot", password: "boterkoek")
ForumService service = new ForumService(credentials, "http://forum.shrimprefuge.be")
List<Post> posts = service.scanPosts(15372);
Set unmatched = [] as HashSet
println "[list]"
posts.each { Post post ->
    if (post.username != "user") {
        vids = getYoutubeIds(post)
        //println "Posted by [b]$post.username[/b]"
        vids.each {
            def title = findTitle(it)
            if (title) {
                def url = "www.youtube.com/watch?v=" + it
                def cleanTitle = cleanTitle(title)
                def spotifyUri = findSpotifyUri(cleanTitle)
                if (spotifyUri) {
                    def spotifyForumLink = "[*][url=${spotifyUri}]${spotifyUri}[/url]"
                    println "$spotifyForumLink - [$title] at $url"
                }
                else {
                    unmatched << "no spotify match found for $title (in cleaned up version $cleanTitle)"
                }
            }
        }
    }
}
println "[/list]"
unmatched.each { println it }


def getYoutubeIds(Post post) {
    Set vids = [] as HashSet
    post.message.eachMatch(~/www.youtube.com[^<|'|&]*/) { match ->
        def id = getId(match)
        vids << id
    }
    post.message.eachMatch(~/DisplayYouTubeMovie\(this,&apos;([^&]*)/) { match ->
        def vid = match[1]
        def id = getId(vid)
        vids << id
    }
    vids.removeAll { String vid -> vid.contains('$') || vid.contains('"')}
    return vids
}



def getId(String url) {
    def id = url - "http://"
    id = id - "www.youtube.com"
    id = id - "/v/"
    id = id - "watch?v="
    return id
}


def findTitle(String id) {
    def base = "http://gdata.youtube.com/feeds/api/videos?q="
    URL url = new URL(base + URLEncoder.encode(id));
    def xml = new XmlSlurper().parseText(url.text)
    def vidnode = xml.depthFirst().find { it.name() == "entry"}
    titlenode = vidnode?.depthFirst().find { it.name() == "title"}
    return titlenode?.text()
}

def cleanTitle(String title) {
    title = title.toLowerCase();
    title = title.replaceAll(~/-|&|_|ft|feat|hd|hq|quality|stereo|lyrics|official|version|album/, "")
    title = title.replaceAll(~/\(.*\)/, "")
    title = title.replaceAll(~/\s{2,}/, " ")
    return title
}

def findSpotifyUri(String title) {
    def base = "http://ws.spotify.com/search/1/track?q="
    URL url = new URL(base + URLEncoder.encode(title));
    def xml = new XmlSlurper().parseText(url.text)
    def tracknode = xml.depthFirst().find { it.name() == "track"}
    def uri = tracknode?.@href
    sleep 2000
    return uri
}