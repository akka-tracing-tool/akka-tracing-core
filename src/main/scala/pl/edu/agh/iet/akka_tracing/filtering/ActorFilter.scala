package pl.edu.agh.iet.akka_tracing.filtering

trait ActorFilter {
  def apply(actorClass: Class[_]): Boolean
}

class ByClassesAllowActorFilter(allowedClasses: List[Class[_]]) extends ActorFilter {
  override def apply(actorClass: Class[_]): Boolean = {
    allowedClasses.exists(`class` => `class`.isAssignableFrom(actorClass))
  }
}

class ByClassesDenyActorFilter(deniedClasses: List[Class[_]]) extends ActorFilter {
  override def apply(actorClass: Class[_]): Boolean = {
    !deniedClasses.exists(`class` => `class`.isAssignableFrom(actorClass))
  }
}

class StackedConjunctionActorFilter(filters: List[ActorFilter]) extends ActorFilter {
  override def apply(actorClass: Class[_]): Boolean = {
    filters.foldLeft(true) {
      case (result, filter) => result && filter(actorClass)
    }
  }
}

class StackedDisjunctionActorFilter(filters: List[ActorFilter]) extends ActorFilter {
  override def apply(actorClass: Class[_]): Boolean = {
    filters.foldLeft(false) {
      case (result, filter) => result || filter(actorClass)
    }
  }
}

object NoOpActorFilter extends ActorFilter {
  override def apply(actorClass: Class[_]): Boolean = true
}