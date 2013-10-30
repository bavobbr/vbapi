package forum

import forum.model.User

class UserStore {

    static List<User> users = []

    static {
        users << new User(username: "JERRE_31", good: true)
        users << new User(username: "RADJE", good: false)
        users << new User(username: "BLONDJE", good: true)
        users << new User(username: "CIT", good: true)
        users << new User(username: "DIVOLINON", good: true)
        users << new User(username: "SENTINEL", good: true)
        users << new User(username: "ANDU", good: true)
        users << new User(username: "TURTLE", good: true)
        users << new User(username: "CSM", good: false)
        users << new User(username: "ROOZ", good: true)
        users << new User(username: "SINTENDO", good: true)
        users << new User(username: "HOLLY MARTINS", good: true)
        users << new User(username: "KURKHOER", good: false)
        users << new User(username: "ROAR", good: true)
        users << new User(username: "OMEGAII", good: false)
    }

    public static User findUser(String username) {
        if (username.length() <= 2) return null
        return users.find() { it.username.toUpperCase() =~ username.toUpperCase() }
    }
}
