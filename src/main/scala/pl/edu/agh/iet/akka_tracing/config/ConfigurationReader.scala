package pl.edu.agh.iet.akka_tracing.config

import akka.actor.{ ActorRef, ActorSystem }
import com.typesafe.config.{ Config, ConfigFactory }
import pl.edu.agh.iet.akka_tracing.collector.CollectorConstructor
import pl.edu.agh.iet.akka_tracing.config.ConfigUtils._
import pl.edu.agh.iet.akka_tracing.filtering._
import pl.edu.agh.iet.akka_tracing.visualization.data.{ DataSource, DataSourceConstructor }

import scala.concurrent.ExecutionContext

class ConfigurationReader (config: Config, classLoader: ClassLoader) {
  private val rootConfig = config \ "akka_tracing"
  private val filtersConfig =
    rootConfig.getOrElse[Config]("filters", ConfigFactory.empty("Filters"))
  private val actorFilterConfig =
    filtersConfig.getOrElse[Config]("actors", ConfigFactory.empty("Actor filter"))
  private val messageFilterConfig =
    filtersConfig.getOrElse[Config]("messages", ConfigFactory.empty("Message filter"))
  private val collectorConfig =
    rootConfig.getOrElse[Config]("collector", ConfigFactory.empty("Collector"))

  def getMessageFilter: MessageFilter = {
    new MessageFilteringConfigurationReader(messageFilterConfig, classLoader)
      .readMessageConfiguration()
  }

  def getActorFilter: ActorFilter = {
    new ActorFilteringConfigurationReader(actorFilterConfig, classLoader)
      .readActorConfiguration()
  }

  def createCollector(system: ActorSystem): ActorRef = {
    val PackagePrefix = "pl.edu.agh.iet.akka_tracing.collector"
    val NoOpCollectorClassNames = Seq("noop", "NoOpCollector", s"$PackagePrefix.NoOpCollector")
    val RelationalDatabaseCollectorClassNames = Seq(
      "relational", "RelationalDatabaseCollector", s"$PackagePrefix.RelationalDatabaseCollector"
    )
    val CouchDbCollectorClassNames = Seq(
      "couch", "couchdb", "CouchDbCollector", s"$PackagePrefix.CouchDbCollector"
    )

    val collectorClassName = collectorConfig.getOrElse[String]("className", "noop")

    val collectorConstructorClassName = collectorClassName match {
      case s if NoOpCollectorClassNames contains s =>
        s"$PackagePrefix.NoOpCollectorConstructor"
      case s if RelationalDatabaseCollectorClassNames contains s =>
        s"$PackagePrefix.RelationalDatabaseCollectorConstructor"
      case s if CouchDbCollectorClassNames contains s =>
        s"$PackagePrefix.CouchDbCollectorConstructor"
      case _ =>
        collectorConfig.get[String]("constructorClassName")
    }

    val constructor = classLoader
      .loadClass(collectorConstructorClassName)
      .newInstance()
      .asInstanceOf[CollectorConstructor]

    system.actorOf(constructor.propsFromConfig(collectorConfig), "collector")
  }

  def createDataSource(implicit ec: ExecutionContext): DataSource = {
    val PackagePrefix = "pl.edu.agh.iet.akka_tracing.visualization.data"
    val NoOpDataSource = Seq("noop", "NoOpDataSource", s"$PackagePrefix.NoOpDataSource")
    val RelationalDatabaseCollectorClassNames = Seq(
      "relational", "RelationalDatabaseDataSource", s"$PackagePrefix.RelationalDatabaseDataSource"
    )
    val CouchDbCollectorClassNames = Seq(
      "couch", "couchdb", "CouchDbDataSource", s"$PackagePrefix.CouchDbDataSource"
    )

    val dataSourceClassName = collectorConfig.getOrElse[String]("dataSourceClassName", "noop")

    val dataSourceConstructorClassName = dataSourceClassName match {
      case s if NoOpDataSource contains s =>
        s"$PackagePrefix.NoOpDataSourceConstructor"
      case s if RelationalDatabaseCollectorClassNames contains s =>
        s"$PackagePrefix.RelationalDatabaseDataSourceConstructor"
      case s if CouchDbCollectorClassNames contains s =>
        s"$PackagePrefix.CouchDbDataSourceConstructor"
      case _ =>
        collectorConfig.get[String]("constructorClassName")
    }

    val constructor = classLoader
      .loadClass(dataSourceConstructorClassName)
      .newInstance()
      .asInstanceOf[DataSourceConstructor]

    constructor.fromConfig(collectorConfig)
  }
}
