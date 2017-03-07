package pl.edu.agh.iet.akka_tracing.collector

import java.io.{ BufferedWriter, File, FileWriter }
import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }
import com.typesafe.config.ConfigFactory
import org.scalatest.FlatSpecLike
import pl.edu.agh.iet.akka_tracing.collector.Collector.{ CollectorMessage, RelationMessage }

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class DatabaseCollectorTest(_system: ActorSystem) extends TestKit(_system) with FlatSpecLike with ImplicitSender {
  def this() = this(ActorSystem("DatabaseCollectorTest"))

  val configFile = File.createTempFile("akka_tracing.conf.collector.in-memory-db", "")
  val bufferedWriter = new BufferedWriter(new FileWriter(configFile))
  bufferedWriter.write(
    """
      |    database {
      |      profile = "slick.jdbc.H2Profile$"
      |      db {
      |        driver = "org.h2.Driver"
      |        url = "jdbc:h2:mem:test"
      |      }
      |    }
    """.stripMargin)
  bufferedWriter.close()
  val config = ConfigFactory.parseFile(configFile)

  import slick.jdbc.H2Profile.api._

  val db = Database.forURL("jdbc:h2:mem:test")


  "A database's tables" should "contains 1 row each" in {
    val collector = system.actorOf(DatabaseCollector.props(config))
    val uuid = UUID.randomUUID()
    collector ! CollectorMessage(uuid, Option("sender"), None)
    collector ! CollectorMessage(uuid, None, Option("receiver"))
    collector ! RelationMessage(UUID.randomUUID(), UUID.randomUUID())

    Thread.sleep(3000)

    val messagesRowsCount = Await.result(db.run(countMessagesQuery), 1 seconds).head
    val relationRowsCount = Await.result(db.run(countRelationQuery), 1 seconds).head

    assert(messagesRowsCount === 1)
    assert(relationRowsCount === 1)

  }
  val countMessagesQuery = sql"""select count(*) from "messages"""".as[Int]
  val countRelationQuery = sql"""select count(*) from "relation"""".as[Int]
}
