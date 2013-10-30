package forum.model

class Day {

    List<Vote> allVotes = []
    List<Vote> uniqueVotes = []

    void addVote(Vote vote) {
        allVotes << vote
        uniqueVotes.removeAll { Vote c -> c.user == vote.user}
        uniqueVotes.add(vote)
    }

    void addVotes(List<Vote> votes) {
        votes.each {
            if (it.valid) addVote(it)
        }
    }

    Map<User, Integer> getUniqueVoteSummary() {
        return uniqueVotes.countBy { Vote vote -> vote.target }
    }

    Map<User, Integer> getAllVoteSummary() {
        return allVotes.countBy { Vote vote -> vote.target }
    }

}
