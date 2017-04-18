package pl.edu.agh.iet.akka_tracing.collector

import java.util.UUID

import com.typesafe.config.Config
import org.json4s.JValue
import pl.edu.agh.iet.akka_tracing.collector.Collector.{ CollectorReceiverMessage, CollectorSenderMessage, RelationMessage }

import scala.concurrent.ExecutionContext

object Collector {

  case class CollectorSenderMessage(id: UUID, sender: String, message: Option[JValue])

  case class CollectorReceiverMessage(id: UUID, receiver: String)

  case class RelationMessage(id1: UUID, id2: UUID)

}

trait Collector {

  import Collector._

  protected implicit def ec: ExecutionContext

  def handleSenderMessage(msg: CollectorSenderMessage): Unit

  def handleReceiverMessage(msg: CollectorReceiverMessage): Unit

  def handleRelationMessage(msg: RelationMessage): Unit
}

trait CollectorConstructor {
  def fromConfig(config: Config)(implicit ec: ExecutionContext): Collector
}

class NoOpCollector(implicit val ec: ExecutionContext) extends Collector {
  override def handleSenderMessage(msg: CollectorSenderMessage): Unit = {}

  override def handleReceiverMessage(msg: CollectorReceiverMessage): Unit = {}

  override def handleRelationMessage(msg: RelationMessage): Unit = {}
}

class NoOpCollectorConstructor extends CollectorConstructor {
  override def fromConfig(config: Config)(implicit ec: ExecutionContext): Collector = {
    new NoOpCollector
  }
}
