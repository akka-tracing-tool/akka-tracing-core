package pl.edu.agh.iet.akka_tracing.collector

import com.typesafe.config.Config
import pl.edu.agh.iet.akka_tracing.model.{ MessagesRelation, ReceiverMessage, SenderMessage }

import scala.concurrent.ExecutionContext

trait Collector {

  protected implicit def ec: ExecutionContext

  def handleSenderMessage(msg: SenderMessage): Unit

  def handleReceiverMessage(msg: ReceiverMessage): Unit

  def handleRelationMessage(msg: MessagesRelation): Unit
}

trait CollectorConstructor {
  def fromConfig(config: Config)(implicit ec: ExecutionContext): Collector
}

class NoOpCollector(implicit val ec: ExecutionContext) extends Collector {

  override def handleSenderMessage(msg: SenderMessage): Unit = {}

  override def handleReceiverMessage(msg: ReceiverMessage): Unit = {}

  override def handleRelationMessage(msg: MessagesRelation): Unit = {}
}

class NoOpCollectorConstructor extends CollectorConstructor {
  override def fromConfig(config: Config)(implicit ec: ExecutionContext): Collector = {
    new NoOpCollector
  }
}
