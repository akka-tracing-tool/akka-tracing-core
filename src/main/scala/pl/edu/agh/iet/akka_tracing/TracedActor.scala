package pl.edu.agh.iet.akka_tracing

import java.util.UUID

import akka.actor.Actor

case class MessageWrapper(id: UUID, msg: AnyRef)

trait TracedActor extends Actor {
  var MessageWrapperId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")

  override def aroundReceive(receive: Receive, msg: Any): Unit = super.aroundReceive(receive, msg)
}
