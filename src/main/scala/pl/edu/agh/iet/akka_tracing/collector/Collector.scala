package pl.edu.agh.iet.akka_tracing.collector

import java.util.UUID

import akka.actor.{ Actor, Props }
import com.typesafe.config._
import pl.edu.agh.iet.akka_tracing.database.{ CollectorDBMessage, CollectorDBMessagesRelation, DatabaseUtils }

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext }
import scala.language.postfixOps

object Collector {

  case class CollectorMessage(id: UUID, sender: Option[String], receiver: Option[String])

  case class RelationMessage(id1: UUID, id2: UUID)

}

trait Collector extends Actor {

  import Collector._

  protected def handleCollectorMessage(msg: CollectorMessage): Unit

  protected def handleRelationMessage(msg: RelationMessage): Unit

  override def receive: Receive = {
    case msg: CollectorMessage =>
      handleCollectorMessage(msg)
    case msg: RelationMessage =>
      handleRelationMessage(msg)
  }
}

final class RelationalDatabaseCollector(config: Config) extends Collector {

  import Collector._

  val databaseUtils = new DatabaseUtils(config)
  val dc = databaseUtils.getDatabaseConfig
  implicit val executor: ExecutionContext = context.dispatcher

  import dc.profile.api._
  import databaseUtils._

  val db = dc.db
  val future = databaseUtils.init
  val messages = TableQuery[CollectorDBMessages]
  val relations = TableQuery[CollectorDBMessagesRelations]

  override protected def handleCollectorMessage(msg: CollectorMessage): Unit = msg match {
    case CollectorMessage(id, sender, None) =>
      Await.result(future, 5 seconds)
      val f = db.run(messages += CollectorDBMessage(id, sender.get, None))
      Await.result(f, 5 seconds)
    case CollectorMessage(id, None, receiver) =>
      Await.result(future, 5 seconds)
      val f = db.run(messages.filter(_.id === id).map(_.receiver).update(receiver))
      Await.result(f, 5 seconds)
  }

  override protected def handleRelationMessage(msg: RelationMessage): Unit = msg match {
    case RelationMessage(id1, id2) =>
      Await.result(future, 5 seconds)
      val f = db.run(relations += CollectorDBMessagesRelation(id1, id2))
      Await.result(f, 5 seconds)
  }
}

object RelationalDatabaseCollector {
  def props(config: Config): Props = Props(classOf[RelationalDatabaseCollector], config)
}
