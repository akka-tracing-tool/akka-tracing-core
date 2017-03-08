package pl.edu.agh.iet.akka_tracing

import java.util.UUID

import akka.actor.Actor

case class MessageWrapper(id: UUID, msg: AnyRef)

//trait DistributedStackTraceMessage {
//  val stackTrace = Thread.currentThread().getStackTrace
//}

trait TracedActor {
  actor: Actor =>

  var MessageWrapperId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")

  override def aroundReceive(receive: Actor.Receive, msg: Any): Unit = {
    try {
      receive.applyOrElse(msg, unhandled)
    } catch {
      //TODO: make aspectj exception handler
      case exception: Exception => println("INTERCEPTED exception " + exception)
        //        val oldStackTrace = exception.getStackTrace
        //        val newStackTrace = oldStackTrace ++ msg.asInstanceOf[DistributedStackTraceMessage].stackTrace
        //        exception.setStackTrace(newStackTrace)
        throw exception
    }
  }
}
