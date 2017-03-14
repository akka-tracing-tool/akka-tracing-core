package pl.edu.agh.iet.akka_tracing.filtering

import com.typesafe.config.{ Config, ConfigFactory }
import org.scalatest.FlatSpec

import scala.io.Source

case class MessageTest1(arg: Int)

case class MessageTest2(arg: String)

class SimpleMessageFilter extends MessageFilter {
  override def apply(message: Any): Boolean = true
}

class ComplexMessageFilter(val pass: Boolean) extends MessageFilter {
  override def apply(message: Any): Boolean = pass
}

class ComplexMessageFilterConstructor extends MessageFilterConstructor {
  override def fromConfig(config: Config): MessageFilter = {
    new ComplexMessageFilter(config.getBoolean("pass"))
  }
}

class MessageFilteringConfigurationReaderTest extends FlatSpec {
  "MessageFilteringConfigurationReader" should "create noop filter from noop config" in {
    val configFile = Source.fromURL(getClass.getClassLoader.getResource("messages_test1.conf")).mkString
    val config = ConfigFactory.parseString(configFile).getConfig("messages")

    val configurationReader = new MessageFilteringConfigurationReader(config, getClass.getClassLoader)
    val readFilter = configurationReader.readMessageConfiguration()

    assert(readFilter.isInstanceOf[NoOpMessageFilter])
  }

  it should "create noop filter from no config" in {
    val config = ConfigFactory.empty()

    val configurationReader = new MessageFilteringConfigurationReader(config, getClass.getClassLoader)
    val readFilter = configurationReader.readMessageConfiguration()

    assert(readFilter.isInstanceOf[NoOpMessageFilter])
  }

  it should "create probability filter from probability config" in {
    val configFile = Source.fromURL(getClass.getClassLoader.getResource("messages_test2.conf")).mkString
    val config = ConfigFactory.parseString(configFile).getConfig("messages")

    val configurationReader = new MessageFilteringConfigurationReader(config, getClass.getClassLoader)
    val readFilter = configurationReader.readMessageConfiguration()

    assert(readFilter.isInstanceOf[ProbabilityMessageSampler])
    assert(readFilter.asInstanceOf[ProbabilityMessageSampler].probability == 0.5)
  }

  it should "create conjunction filter with whitelist and probability filters from conjunction config" in {
    val configFile = Source.fromURL(getClass.getClassLoader.getResource("messages_test3.conf")).mkString
    val config = ConfigFactory.parseString(configFile).getConfig("messages")

    val configurationReader = new MessageFilteringConfigurationReader(config, getClass.getClassLoader)
    val readFilter = configurationReader.readMessageConfiguration()

    assert(readFilter.isInstanceOf[StackedConjunctionMessageFilter])

    val conjunctionMessageFilter = readFilter.asInstanceOf[StackedConjunctionMessageFilter]
    assert(conjunctionMessageFilter.filters.length == 2)
    assert(conjunctionMessageFilter.filters.head.isInstanceOf[ByClassesAllowMessageFilter])
    assert(conjunctionMessageFilter.filters(1).isInstanceOf[ProbabilityMessageSampler])

    val firstFilter = conjunctionMessageFilter.filters.head.asInstanceOf[ByClassesAllowMessageFilter]
    assert(firstFilter.allowedClasses.length == 2)
    assert(firstFilter.allowedClasses == List(classOf[MessageTest1], classOf[MessageTest2]))

    val secondFilter = conjunctionMessageFilter.filters(1).asInstanceOf[ProbabilityMessageSampler]
    assert(secondFilter.probability == 0.5)
  }

  it should "create disjunction filter with blacklist and probability filters from disjunction config" in {
    val configFile = Source.fromURL(getClass.getClassLoader.getResource("messages_test4.conf")).mkString
    val config = ConfigFactory.parseString(configFile).getConfig("messages")

    val configurationReader = new MessageFilteringConfigurationReader(config, getClass.getClassLoader)
    val readFilter = configurationReader.readMessageConfiguration()

    assert(readFilter.isInstanceOf[StackedDisjunctionMessageFilter])

    val conjunctionMessageFilter = readFilter.asInstanceOf[StackedDisjunctionMessageFilter]
    assert(conjunctionMessageFilter.filters.length == 2)
    assert(conjunctionMessageFilter.filters.head.isInstanceOf[ByClassesDenyMessageFilter])
    assert(conjunctionMessageFilter.filters(1).isInstanceOf[ProbabilityMessageSampler])

    val firstFilter = conjunctionMessageFilter.filters.head.asInstanceOf[ByClassesDenyMessageFilter]
    assert(firstFilter.deniedClasses.length == 2)
    assert(firstFilter.deniedClasses == List(classOf[MessageTest1], classOf[MessageTest2]))

    val secondFilter = conjunctionMessageFilter.filters(1).asInstanceOf[ProbabilityMessageSampler]
    assert(secondFilter.probability == 0.5)
  }

  it should "create an user specified filter using default constructor if user does not provide constructor class" in {
    val configFile = Source.fromURL(getClass.getClassLoader.getResource("messages_test5.conf")).mkString
    val config = ConfigFactory.parseString(configFile).getConfig("messages")

    val configurationReader = new MessageFilteringConfigurationReader(config, getClass.getClassLoader)
    val readFilter = configurationReader.readMessageConfiguration()

    assert(readFilter.isInstanceOf[SimpleMessageFilter])
  }

  it should "create an user specified filter using user specified constructor if it is provided" in {
    val configFile = Source.fromURL(getClass.getClassLoader.getResource("messages_test6.conf")).mkString
    val config = ConfigFactory.parseString(configFile).getConfig("messages")

    val configurationReader = new MessageFilteringConfigurationReader(config, getClass.getClassLoader)
    val readFilter = configurationReader.readMessageConfiguration()

    assert(readFilter.isInstanceOf[ComplexMessageFilter])
    assert(readFilter.asInstanceOf[ComplexMessageFilter].pass)
  }
}
