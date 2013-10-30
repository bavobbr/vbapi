package forum.model

class Member {

    String username
    Integer memberId
    Date lastActive
    Integer totalPosts
    Date joined
    URL avatar


    public String toString() {
        return "name [$username] id [$memberId] active [$lastActive] [$totalPosts] [$joined] [$avatar]"
    }

}
