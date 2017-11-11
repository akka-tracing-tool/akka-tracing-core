package pl.edu.agh.iet.akka_tracing.collector

import akka.actor.{ Actor, Props }
import com.typesafe.config.Config
import pl.edu.agh.iet.akka_tracing.model.{ MessagesRelation, ReceiverMessage, SenderMessage }

import scala.concurrent.ExecutionContext

trait Collector extends Actor {

  protected implicit val ec: ExecutionContext = context.dispatcher

  override def receive: Receive = {
    case msg: SenderMessage => handleSenderMessage(msg)
    case msg: ReceiverMessage => handleReceiverMessage(msg)
    case msg: MessagesRelation => handleRelationMessage(msg)
    case msg =>
      if (handleOtherMessages.isDefinedAt(msg)) {
        handleOtherMessages.apply(msg)
      } else {
        unhandled(msg)
      }
  }

  def handleSenderMessage(msg: SenderMessage): Unit

  def handleReceiverMessage(msg: ReceiverMessage): Unit

  def handleRelationMessage(msg: MessagesRelation): Unit

  def handleOtherMessages: PartialFunction[Any, Unit] = PartialFunction.empty
}

trait CollectorConstructor {
  def propsFromConfig(config: Config): Props
}

class NoOpCollector extends Collector {

  override def handleSenderMessage(msg: SenderMessage): Unit = {}

  override def handleReceiverMessage(msg: ReceiverMessage): Unit = {}

  override def handleRelationMessage(msg: MessagesRelation): Unit = {}
}

class NoOpCollectorConstructor extends CollectorConstructor {
  override def propsFromConfig(config: Config): Props = Props[NoOpCollector]
}
