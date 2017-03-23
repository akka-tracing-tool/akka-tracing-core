package pl.edu.agh.iet.akka_tracing.visualization.data

import com.typesafe.config.Config
import pl.edu.agh.iet.akka_tracing.model.{ Message, MessagesRelation }

import scala.concurrent.{ ExecutionContext, Future }

trait DataSource {
  def ec: ExecutionContext

  def onStart: Future[Unit]

  def getMessages: Future[List[Message]]

  def getRelations: Future[List[MessagesRelation]]
}

trait DataSourceConstructor {
  def fromConfig(config: Config)(implicit ec: ExecutionContext): DataSource
}

class NoOpDataSource(implicit val ec: ExecutionContext) extends DataSource {
  override def onStart: Future[Unit] = Future.successful(())

  override def getMessages: Future[List[Message]] = Future.successful(List())

  override def getRelations: Future[List[MessagesRelation]] = Future.successful(List())
}

class NoOpDataSourceConstructor extends DataSourceConstructor {
  override def fromConfig(config: Config)(implicit ec: ExecutionContext): DataSource = {
    new NoOpDataSource
  }
}
