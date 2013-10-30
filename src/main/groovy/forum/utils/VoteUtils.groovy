package forum.utils

import groovy.util.slurpersupport.GPathResult
import forum.model.Post
import forum.model.User
import forum.model.Vote

import forum.UserStore

/**
 * This is a utility to parse custom VOTE tags, used by forum games
 */
class VoteUtils {

    static public List<Vote> toVotes(List<Post> posts) {
        return posts.collect { toVote(it) }
    }

    static public Vote toVote(Post post) {
        def target = findVoteTarget(post.message).toUpperCase()
        Vote vote = new Vote()
        User user = UserStore.findUser(post.username)
        vote.user = user
        if (target) {
            vote.target = UserStore.findUser(target)
        }
        vote.date = post.date
        vote.postNumber = post.postNumber
        return vote
    }

    static public boolean isVote(String post) {
        if (post =~ "!Vote: ") {
            return findVoteTarget(post) != null
        }
    }

    static public String findVoteTarget(String post) {
        GPathResult node = PostUtils.toNode(post)
        String target = null
        def votenodes = node.depthFirst().findAll {
            it.name() == "FONT" && it.text().trim().startsWith("!Vote:") && it.@color == "Red"
        }
        votenodes.removeAll { GPathResult qnode ->
            boolean isInQuote = PostUtils.hasParent(qnode) { GPathResult cnode ->
                return cnode.name() == "TD" && cnode.@class == "alt2"
            }
            return isInQuote
        }
        if (votenodes) {
            target = votenodes.last().text() - "!Vote: "
        }
        return target?.trim()
    }
}
