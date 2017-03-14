package pl.edu.agh.iet.akka_tracing.filtering

import com.typesafe.config.Config

import scala.collection.JavaConverters._
import scala.util.Try

class MessageFilteringConfigurationReader(config: Config, classLoader: ClassLoader) {
  private val PackagePrefix = "pl.edu.agh.iet.akka_tracing.filtering"

  private val NoOpMessageFilterNames = List(
    "noop", "NoOpMessageFilter", s"$PackagePrefix.NoOpMessageFilter"
  )
  private val ByClassesAllowMessageFilterNames = List(
    "allow", "whitelist", "allowClasses", "whitelistClasses",
    "ByClassesAllowMessageFilter", s"$PackagePrefix.ByClassesAllowMessageFilter"
  )
  private val ByClassesDenyMessageFilterNames = List(
    "deny", "blacklist", "denyClasses", "blacklistClasses", "ByClassesDenyMessageFilter",
    s"$PackagePrefix.ByClassesDenyMessageFilter"
  )
  private val ProbabilityMessageSamplerNames = List(
    "probability", "sampler", "sampling", "ProbabilityMessageSampler",
    s"$PackagePrefix.ProbabilityMessageSampler"
  )
  private val StackedConjunctionMessageFilterNames = List(
    "and", "conjunction", "StackedConjunctionMessageFilter",
    s"$PackagePrefix.StackedConjunctionMessageFilter"
  )
  private val StackedDisjunctionMessageFilterNames = List(
    "or", "disjunction", "StackedDisjunctionMessageFilter",
    s"$PackagePrefix.StackedDisjunctionMessageFilter"
  )

  def readMessageConfiguration(): MessageFilter = {
    createFilter(config)
  }

  def createFilter(config: Config): MessageFilter = {
    val className = Try(config.getString("filter")).getOrElse("noop")
    className match {
      case name if NoOpMessageFilterNames contains name => new NoOpMessageFilter
      case name if StackedConjunctionMessageFilterNames contains name =>
        val configs = config.getConfigList("arguments").asScala.toList
        val filters = configs.map { config => createFilter(config) }
        new StackedConjunctionMessageFilter(filters)
      case name if StackedDisjunctionMessageFilterNames contains name =>
        val configs = config.getConfigList("arguments").asScala.toList
        val filters = configs.map { config => createFilter(config) }
        new StackedDisjunctionMessageFilter(filters)
      case name if ByClassesAllowMessageFilterNames contains name =>
        val classList = config.getStringList("arguments").asScala.toList.map { allowedClassName =>
          classLoader.loadClass(allowedClassName)
        }
        new ByClassesAllowMessageFilter(classList)
      case name if ByClassesDenyMessageFilterNames contains name =>
        val classList = config.getStringList("arguments").asScala.toList.map { deniedClassName =>
          classLoader.loadClass(deniedClassName)
        }
        new ByClassesDenyMessageFilter(classList)
      case name if ProbabilityMessageSamplerNames contains name =>
        val probability = config.getDoubleList("arguments").asScala.head
        new ProbabilityMessageSampler(probability)
      case _ =>
        Try(config.getString("constructorClassName"))
          .toOption
          .fold[MessageFilterConstructor](
          new DefaultMessageFilterConstructor(
            classLoader.loadClass(className)
          )
        )(constructorClassName =>
          classLoader
            .loadClass(constructorClassName)
            .newInstance()
            .asInstanceOf[MessageFilterConstructor]
        )
          .fromConfig(config)
    }
  }
}

trait MessageFilterConstructor {
  def fromConfig(config: Config): MessageFilter
}

class DefaultMessageFilterConstructor(`class`: Class[_])
  extends MessageFilterConstructor {
  override def fromConfig(config: Config): MessageFilter = {
    `class`.newInstance().asInstanceOf[MessageFilter]
  }
}
