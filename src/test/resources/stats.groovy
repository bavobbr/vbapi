import forum.ForumService
import forum.model.Credentials
import groovy.sql.DataSet
import groovy.sql.Sql

Credentials credentials = new Credentials(username: "", password: "")
ForumService service = new ForumService(credentials, "http://forum.shrimprefuge.be")

int last = 1454175
int scanLast = Math.min(9999999, last)
int batchSize = 100
int startOfBatch = 1334396+1
def batches = (scanLast-startOfBatch) / batchSize

1.upto(Math.round(batches)) {
	println "Batch $it - Scanning from $startOfBatch for $batchSize items"
	def posts = service.getPosts(startOfBatch, batchSize)
	startOfBatch = startOfBatch + batchSize
	posts.each {
		println "${it.postId} ${it.date} ${it.username} ${it.forum}"
	}
	int validPosts = posts.count { it.valid }
	def sql = Sql.newInstance("jdbc:mysql://localhost:3306/shrimp", "root", "", "com.mysql.jdbc.Driver")
	posts.each {
		DataSet db = sql.dataSet("Post")
		db.add(postId: it.postId, username: it.username, date: it.date, valid: it.valid, forum: it.forum)
	}
	sql.close()
	println "added $validPosts to DB"
}




