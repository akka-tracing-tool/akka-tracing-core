package pl.edu.agh.iet.akka_tracing.model

import java.util.UUID

import org.json4s.JValue

case class Message(id: UUID,
                   sender: String,
                   receiver: Option[String],
                   contents: Option[JValue])

case class MessagesRelation(id1: UUID, id2: UUID)
