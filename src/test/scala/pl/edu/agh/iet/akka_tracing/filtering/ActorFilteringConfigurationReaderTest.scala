package pl.edu.agh.iet.akka_tracing.filtering

import com.typesafe.config.{ Config, ConfigFactory }
import org.scalatest.FlatSpec

import scala.io.Source

case class ActorTest1(arg: Int)

case class ActorTest2(arg: String)

class SimpleActorFilter extends ActorFilter {
  override def apply(actorClass: Class[_]): Boolean = true
}

class ComplexActorFilter(val pass: Boolean) extends ActorFilter {
  override def apply(actorClass: Class[_]): Boolean = pass
}

class ComplexActorFilterConstructor extends ActorFilterConstructor {
  override def fromConfig(config: Config): ActorFilter = {
    new ComplexActorFilter(config.getBoolean("pass"))
  }
}

class ActorFilteringConfigurationReaderTest extends FlatSpec {
  "ActorFilteringConfigurationReader" should "create noop filter from noop config" in {
    val configFile = Source.fromURL(getClass.getClassLoader.getResource("actors_test1.conf")).mkString
    val config = ConfigFactory.parseString(configFile).getConfig("actors")

    val configurationReader = new ActorFilteringConfigurationReader(config, getClass.getClassLoader)
    val readFilter = configurationReader.readActorConfiguration()

    assert(readFilter.isInstanceOf[NoOpActorFilter])
  }

  it should "create noop filter from no config" in {
    val config = ConfigFactory.empty()

    val configurationReader = new ActorFilteringConfigurationReader(config, getClass.getClassLoader)
    val readFilter = configurationReader.readActorConfiguration()

    assert(readFilter.isInstanceOf[NoOpActorFilter])
  }

  it should "create conjunction filter with whitelist and noop filters from conjunction config" in {
    val configFile = Source.fromURL(getClass.getClassLoader.getResource("actors_test2.conf")).mkString
    val config = ConfigFactory.parseString(configFile).getConfig("actors")

    val configurationReader = new ActorFilteringConfigurationReader(config, getClass.getClassLoader)
    val readFilter = configurationReader.readActorConfiguration()

    assert(readFilter.isInstanceOf[StackedConjunctionActorFilter])

    val conjunctionActorFilter = readFilter.asInstanceOf[StackedConjunctionActorFilter]
    assert(conjunctionActorFilter.filters.length == 2)
    assert(conjunctionActorFilter.filters.head.isInstanceOf[ByClassesAllowActorFilter])
    assert(conjunctionActorFilter.filters(1).isInstanceOf[NoOpActorFilter])

    val firstFilter = conjunctionActorFilter.filters.head.asInstanceOf[ByClassesAllowActorFilter]
    assert(firstFilter.allowedClasses.length == 2)
    assert(firstFilter.allowedClasses == List(classOf[ActorTest1], classOf[ActorTest2]))
  }

  it should "create disjunction filter with blacklist and probability filters from disjunction config" in {
    val configFile = Source.fromURL(getClass.getClassLoader.getResource("actors_test3.conf")).mkString
    val config = ConfigFactory.parseString(configFile).getConfig("actors")

    val configurationReader = new ActorFilteringConfigurationReader(config, getClass.getClassLoader)
    val readFilter = configurationReader.readActorConfiguration()

    assert(readFilter.isInstanceOf[StackedDisjunctionActorFilter])

    val conjunctionActorFilter = readFilter.asInstanceOf[StackedDisjunctionActorFilter]
    assert(conjunctionActorFilter.filters.length == 2)
    assert(conjunctionActorFilter.filters.head.isInstanceOf[ByClassesDenyActorFilter])
    assert(conjunctionActorFilter.filters(1).isInstanceOf[NoOpActorFilter])

    val firstFilter = conjunctionActorFilter.filters.head.asInstanceOf[ByClassesDenyActorFilter]
    assert(firstFilter.deniedClasses.length == 2)
    assert(firstFilter.deniedClasses == List(classOf[ActorTest1], classOf[ActorTest2]))
  }

  it should "create an user specified filter using default constructor if user does not provide constructor class" in {
    val configFile = Source.fromURL(getClass.getClassLoader.getResource("actors_test4.conf")).mkString
    val config = ConfigFactory.parseString(configFile).getConfig("actors")

    val configurationReader = new ActorFilteringConfigurationReader(config, getClass.getClassLoader)
    val readFilter = configurationReader.readActorConfiguration()

    assert(readFilter.isInstanceOf[SimpleActorFilter])
  }

  it should "create an user specified filter using user specified constructor if it is provided" in {
    val configFile = Source.fromURL(getClass.getClassLoader.getResource("actors_test5.conf")).mkString
    val config = ConfigFactory.parseString(configFile).getConfig("actors")

    val configurationReader = new ActorFilteringConfigurationReader(config, getClass.getClassLoader)
    val readFilter = configurationReader.readActorConfiguration()

    assert(readFilter.isInstanceOf[ComplexActorFilter])
    assert(readFilter.asInstanceOf[ComplexActorFilter].pass)
  }
}
