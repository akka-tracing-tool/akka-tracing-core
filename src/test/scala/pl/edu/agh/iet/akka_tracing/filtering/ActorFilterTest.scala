package pl.edu.agh.iet.akka_tracing.filtering

import akka.actor.Actor
import org.scalatest.FlatSpec

class ActorFilterTest extends FlatSpec {

  import ActorFilterTest._

  "NoOpActorfilter" should "return true" in {
    val filter = new NoOpActorFilter
    val actors = List[Class[_]](classOf[Test1], classOf[Test2])
    val expected = List(true, true)
    assert(actors.map(filter.apply) == expected)
  }

  "ByClassesAllowActorFilter" should "return true on allowed actors and false on not allowed actors" in {
    val filter = new ByClassesAllowActorFilter(List(classOf[Test1]))
    val actors = List[Class[_]](classOf[Test1], classOf[Test2])
    val expected = List(true, false)
    assert(actors.map(filter.apply) == expected)
  }

  "ByClassesDenyActorFilter" should "return false on denied actors and true on not denied actors" in {
    val filter = new ByClassesDenyActorFilter(List(classOf[Test1]))
    val actors = List[Class[_]](classOf[Test1], classOf[Test2])
    val expected = List(false, true)
    assert(actors.map(filter.apply) == expected)
  }

  "StackedConjunctionActorFilter" should "behave like a conjunction of filters" in {
    val filter1 = new ByClassesAllowActorFilter(List(classOf[Test1]))
    val filter2 = new ByClassesAllowActorFilter(List(classOf[Test1], classOf[Test2]))
    val filters = List(filter1, filter2)
    val filter = new StackedConjunctionActorFilter(filters)
    val actors = List[Class[_]](classOf[Test1], classOf[Test2])
    val expected = List(true, false)
    assert(actors.map(filter.apply) == expected)
  }

  "StackedDisjunctionActorFilter" should "behave like a disjunction of filters" in {
    val filter1 = new ByClassesAllowActorFilter(List(classOf[Test1]))
    val filter2 = new ByClassesAllowActorFilter(List(classOf[Test2]))
    val filters = List(filter1, filter2)
    val filter = new StackedDisjunctionActorFilter(filters)
    val actors = List[Class[_]](classOf[Test1], classOf[Test2])
    val expected = List(true, true)
    assert(actors.map(filter.apply) == expected)
  }
}

object ActorFilterTest {

  class Test1 extends Actor {
    override def receive: Receive = {
      case _ => println("Received a message")
    }
  }

  class Test2 extends Actor {
    override def receive: Receive = {
      case _ => println("Received a message")
    }
  }

}
