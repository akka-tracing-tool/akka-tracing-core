package pl.edu.agh.iet.akka_tracing.filtering

import com.typesafe.config.Config
import pl.edu.agh.iet.akka_tracing.config.ConfigUtils._

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
    val className = config.getOrElse[String]("filter", "noop")
    className match {
      case name if NoOpMessageFilterNames contains name => new NoOpMessageFilter
      case name if StackedConjunctionMessageFilterNames contains name =>
        val configs = config.getOrElse[List[Config]]("arguments", List())
        val filters = configs.map { config => createFilter(config) }
        new StackedConjunctionMessageFilter(filters)
      case name if StackedDisjunctionMessageFilterNames contains name =>
        val configs = config.getOrElse[List[Config]]("arguments", List())
        val filters = configs.map { config => createFilter(config) }
        new StackedDisjunctionMessageFilter(filters)
      case name if ByClassesAllowMessageFilterNames contains name =>
        val classList = config.getOrElse[List[String]]("arguments", List()).map { allowedClassName =>
          classLoader.loadClass(allowedClassName)
        }
        new ByClassesAllowMessageFilter(classList)
      case name if ByClassesDenyMessageFilterNames contains name =>
        val classList = config.getOrElse[List[String]]("arguments", List()).map { deniedClassName =>
          classLoader.loadClass(deniedClassName)
        }
        new ByClassesDenyMessageFilter(classList)
      case name if ProbabilityMessageSamplerNames contains name =>
        val probability = config.getOrElse[List[Double]]("arguments", List(1.0d)).head
        new ProbabilityMessageSampler(probability)
      case _ =>
        config.getOption[String]("constructorClassName").fold[MessageFilterConstructor](
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
