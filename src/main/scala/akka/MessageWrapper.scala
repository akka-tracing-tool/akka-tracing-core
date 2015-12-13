package akka

import java.util.UUID

case class MessageWrapper(id: UUID, msg: AnyRef)
