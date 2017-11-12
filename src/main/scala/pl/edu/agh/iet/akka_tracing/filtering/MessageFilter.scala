package pl.edu.agh.iet.akka_tracing.filtering

import scala.util.Random

trait MessageFilter {
  def apply(message: Any): Boolean
}

class ByClassesAllowMessageFilter(val allowedClasses: List[Class[_]]) extends MessageFilter {
  override def apply(message: Any): Boolean = {
    allowedClasses.exists(`class` => `class`.isInstance(message))
  }
}

class ByClassesDenyMessageFilter(val deniedClasses: List[Class[_]]) extends MessageFilter {
  override def apply(message: Any): Boolean = {
    !deniedClasses.exists(`class` => `class`.isInstance(message))
  }
}

class StackedConjunctionMessageFilter(val filters: List[MessageFilter]) extends MessageFilter {
  override def apply(message: Any): Boolean = {
    filters.forall(filter => filter(message))
  }
}

class StackedDisjunctionMessageFilter(val filters: List[MessageFilter]) extends MessageFilter {
  override def apply(message: Any): Boolean = {
    filters.exists(filter => filter(message))
  }
}

class ProbabilityMessageSampler(val probability: Double, random: Random = Random) extends MessageFilter {
  assert(0.0 <= probability && probability <= 1.0)

  override def apply(message: Any): Boolean = {
    random.nextDouble() < probability
  }
}

class NoOpMessageFilter extends MessageFilter {
  override def apply(message: Any): Boolean = true
}
