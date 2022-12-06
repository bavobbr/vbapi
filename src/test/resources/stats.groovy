import forum.ForumService
import forum.model.Credentials
import groovy.sql.DataSet
import groovy.sql.Sql

Credentials credentials = new Credentials(username: "x", password: "y")
ForumService service = new ForumService(credentials, "https://forum.shrimprefuge.be/")

int last = 1683437
int scanLast = Math.min(9999999, last)
int batchSize = 100
int startOfBatch = 1650399+1
def batches = (scanLast-startOfBatch) / batchSize

def sql = Sql.newInstance("jdbc:mysql://localhost:3306/shrimp", "root", "", "com.mysql.jdbc.Driver")
println "DB connected"

1.upto(Math.round(batches)) {
	println "Batch $it - Scanning from $startOfBatch for $batchSize items"
	def posts = service.getPosts(startOfBatch, batchSize)
	def thread = service.getThread(23053, 10, 365)
	println thread.pages
	thread.posts.each {
		println "\n -------------------------------"
		println it.date
		println it.username
		println it.messageText
	}
	startOfBatch = startOfBatch + batchSize
	posts.each {
		println "${it.postId} ${it.date} ${it.username} ${it.forum}"
	}
	int validPosts = posts.count { it.valid }
	posts.each {
		DataSet db = sql.dataSet("Post")
		db.add(postId: it.postId, username: it.username, date: it.date, valid: it.valid, forum: it.forum)
	}
	println "added $validPosts to DB"
}

sql.close()




