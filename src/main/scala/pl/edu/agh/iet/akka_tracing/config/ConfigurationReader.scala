package pl.edu.agh.iet.akka_tracing.config

import com.typesafe.config.{ Config, ConfigFactory }
import ConfigUtils._
import pl.edu.agh.iet.akka_tracing.collector.{ Collector, CollectorConstructor }
import pl.edu.agh.iet.akka_tracing.filtering._
import pl.edu.agh.iet.akka_tracing.visualization.data.{ DataSource, DataSourceConstructor }

import scala.concurrent.ExecutionContext

class ConfigurationReader (config: Config, classLoader: ClassLoader)
                          (implicit ec: ExecutionContext) {
  private val rootConfig = config.getConfig("akka_tracing")
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

  def getCollector: Collector = {
    val PackagePrefix = "pl.edu.agh.iet.akka_tracing.collector"
    val NoOpCollectorClassNames = Seq("noop", "NoOpCollector", s"$PackagePrefix.NoOpCollector")
    val RelationalDatabaseCollectorClassNames = Seq(
      "relational", "RelationalDatabaseCollector", s"$PackagePrefix.RelationalDatabaseCollector"
    )

    val collectorClassName = collectorConfig.getOrElse[String]("className", "noop")

    val collectorConstructorClassName = collectorClassName match {
      case s if NoOpCollectorClassNames contains s =>
        s"$PackagePrefix.NoOpCollectorConstructor"
      case s if RelationalDatabaseCollectorClassNames contains s =>
        s"$PackagePrefix.RelationalDatabaseCollectorConstructor"
      case _ =>
        collectorConfig.getString("constructorClassName")
    }

    val constructor = classLoader
      .loadClass(collectorConstructorClassName)
      .newInstance()
      .asInstanceOf[CollectorConstructor]

    constructor.fromConfig(collectorConfig)
  }

  def getDataSource: DataSource = {
    val PackagePrefix = "pl.edu.agh.iet.akka_tracing.visualization.data"
    val NoOpCollectorClassNames = Seq("noop", "NoOpCollector", s"$PackagePrefix.NoOpCollector")
    val RelationalDatabaseCollectorClassNames = Seq(
      "relational", "RelationalDatabaseDataSource", s"$PackagePrefix.RelationalDatabaseDataSource"
    )

    val dataSourceClassName = collectorConfig.getOrElse[String]("dataSourceClassName", "noop")

    val dataSourceConstructorClassName = dataSourceClassName match {
      case s if NoOpCollectorClassNames contains s =>
        s"$PackagePrefix.NoOpDataSourceConstructor"
      case s if RelationalDatabaseCollectorClassNames contains s =>
        s"$PackagePrefix.RelationalDatabaseDataSourceConstructor"
      case _ =>
        collectorConfig.getString("constructorClassName")
    }

    val constructor = classLoader
      .loadClass(dataSourceConstructorClassName)
      .newInstance()
      .asInstanceOf[DataSourceConstructor]

    constructor.fromConfig(collectorConfig)
  }
}
