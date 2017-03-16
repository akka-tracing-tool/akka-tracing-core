package pl.edu.agh.iet.akka_tracing.filtering

import com.typesafe.config.Config

import scala.collection.JavaConverters._
import scala.util.Try

class ActorFilteringConfigurationReader(config: Config, classLoader: ClassLoader) {
  private val PackagePrefix = "pl.edu.agh.iet.akka_tracing.filtering"

  private val NoOpActorFilterNames = List(
    "noop", "NoOpActorFilter", s"$PackagePrefix.NoOpActorFilter"
  )
  private val ByClassesAllowActorFilterNames = List(
    "allow", "whitelist", "allowClasses", "whitelistClasses",
    "ByClassesAllowActorFilter", s"$PackagePrefix.ByClassesAllowActorFilter"
  )
  private val ByClassesDenyActorFilterNames = List(
    "deny", "blacklist", "denyClasses", "blacklistClasses", "ByClassesDenyActorFilter",
    s"$PackagePrefix.ByClassesDenyActorFilter"
  )
  private val StackedConjunctionActorFilterNames = List(
    "and", "conjunction", "StackedConjunctionActorFilter",
    s"$PackagePrefix.StackedConjunctionActorFilter"
  )
  private val StackedDisjunctionActorFilterNames = List(
    "or", "disjunction", "StackedDisjunctionActorFilter",
    s"$PackagePrefix.StackedDisjunctionActorFilter"
  )

  def readActorConfiguration(): ActorFilter = {
    createFilter(config)
  }

  def createFilter(config: Config): ActorFilter = {
    val className = Try(config.getString("filter")).getOrElse("noop")
    className match {
      case name if NoOpActorFilterNames contains name => new NoOpActorFilter
      case name if StackedConjunctionActorFilterNames contains name =>
        val configs = config.getConfigList("arguments").asScala.toList
        val filters = configs.map { config => createFilter(config) }
        new StackedConjunctionActorFilter(filters)
      case name if StackedDisjunctionActorFilterNames contains name =>
        val configs = config.getConfigList("arguments").asScala.toList
        val filters = configs.map { config => createFilter(config) }
        new StackedDisjunctionActorFilter(filters)
      case name if ByClassesAllowActorFilterNames contains name =>
        val classList = config.getStringList("arguments").asScala.toList.map { allowedClassName =>
          classLoader.loadClass(allowedClassName)
        }
        new ByClassesAllowActorFilter(classList)
      case name if ByClassesDenyActorFilterNames contains name =>
        val classList = config.getStringList("arguments").asScala.toList.map { deniedClassName =>
          classLoader.loadClass(deniedClassName)
        }
        new ByClassesDenyActorFilter(classList)
      case _ =>
        Try(config.getString("constructorClassName"))
          .toOption
          .fold[ActorFilterConstructor](
          new DefaultActorFilterConstructor(
            classLoader.loadClass(className)
          )
        )(constructorClassName =>
          classLoader
            .loadClass(constructorClassName)
            .newInstance()
            .asInstanceOf[ActorFilterConstructor]
        )
          .fromConfig(config)
    }
  }
}

trait ActorFilterConstructor {
  def fromConfig(config: Config): ActorFilter
}

class DefaultActorFilterConstructor(`class`: Class[_])
  extends ActorFilterConstructor {
  override def fromConfig(config: Config): ActorFilter = {
    `class`.newInstance().asInstanceOf[ActorFilter]
  }
}
