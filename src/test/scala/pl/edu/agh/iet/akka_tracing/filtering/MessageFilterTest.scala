package pl.edu.agh.iet.akka_tracing.filtering

import org.scalatest.FlatSpec

class MessageFilterTest extends FlatSpec {

  import MessageFilterTest._

  "NoOpMessagefilter" should "return true" in {
    val filter = NoOpMessageFilter
    val messages = List(Test1("yes"), Test2("nope"))
    val expected = List(true, true)
    assert(messages.map(filter.apply) == expected)
  }

  "ByClassesAllowMessageFilter" should "return true on allowed messages and false on not allowed messages" in {
    val filter = new ByClassesAllowMessageFilter(List(classOf[Test1]))
    val messages = List(Test1("yes"), Test2("nope"))
    val expected = List(true, false)
    assert(messages.map(filter.apply) == expected)
  }

  "ByClassesDenyMessageFilter" should "return false on denied messages and true on not denied messages" in {
    val filter = new ByClassesDenyMessageFilter(List(classOf[Test1]))
    val messages = List(Test1("yes"), Test2("nope"))
    val expected = List(false, true)
    assert(messages.map(filter.apply) == expected)
  }

  "StackedConjunctionMessageFilter" should "behave like a conjunction of filters" in {
    val filter1 = new ByClassesAllowMessageFilter(List(classOf[Test1]))
    val filter2 = new ByClassesAllowMessageFilter(List(classOf[Test1], classOf[Test2]))
    val filters = List(filter1, filter2)
    val filter = new StackedConjunctionMessageFilter(filters)
    val messages = List(Test1("yes"), Test2("nope"))
    val expected = List(true, false)
    assert(messages.map(filter.apply) == expected)
  }

  "StackedDisjunctionMessageFilter" should "behave like a disjunction of filters" in {
    val filter1 = new ByClassesAllowMessageFilter(List(classOf[Test1]))
    val filter2 = new ByClassesAllowMessageFilter(List(classOf[Test2]))
    val filters = List(filter1, filter2)
    val filter = new StackedDisjunctionMessageFilter(filters)
    val messages = List(Test1("yes"), Test2("nope"))
    val expected = List(true, true)
    assert(messages.map(filter.apply) == expected)
  }
}

object MessageFilterTest {
  case class Test1(test: String)
  case class Test2(test: String)
}
