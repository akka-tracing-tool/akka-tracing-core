package pl.edu.agh.iet.akka_tracing.filtering

trait ActorFilter {
  def apply(actorClass: Class[_]): Boolean
}

class ByClassesAllowActorFilter(val allowedClasses: List[Class[_]]) extends ActorFilter {
  override def apply(actorClass: Class[_]): Boolean = {
    allowedClasses.exists(`class` => `class`.isAssignableFrom(actorClass))
  }
}

class ByClassesDenyActorFilter(val deniedClasses: List[Class[_]]) extends ActorFilter {
  override def apply(actorClass: Class[_]): Boolean = {
    !deniedClasses.exists(`class` => `class`.isAssignableFrom(actorClass))
  }
}

class StackedConjunctionActorFilter(val filters: List[ActorFilter]) extends ActorFilter {
  override def apply(actorClass: Class[_]): Boolean = {
    !filters.exists(filter => !filter(actorClass))
  }
}

class StackedDisjunctionActorFilter(val filters: List[ActorFilter]) extends ActorFilter {
  override def apply(actorClass: Class[_]): Boolean = {
    filters.exists(filter => filter(actorClass))
  }
}

class NoOpActorFilter extends ActorFilter {
  override def apply(actorClass: Class[_]): Boolean = true
}
