package pl.edu.agh.iet.akka_tracing.filtering

import akka.actor.Actor

trait ActorFilter {
  def apply(actor: Actor): Boolean
}

class ByClassesAllowActorFilter(allowedClasses: List[Class[_]]) extends ActorFilter {
  override def apply(actor: Actor): Boolean = {
    allowedClasses.exists(`class` => `class`.isInstance(actor))
  }
}

class ByClassesDenyActorFilter(deniedClasses: List[Class[_]]) extends ActorFilter {
  override def apply(actor: Actor): Boolean = {
    !deniedClasses.exists(`class` => `class`.isInstance(actor))
  }
}

class StackedConjunctionActorFilter(filters: List[ActorFilter]) extends ActorFilter {
  override def apply(actor: Actor): Boolean = {
    filters.foldLeft(true) {
      case (result, filter) => result && filter(actor)
    }
  }
}

class StackedDisjunctionActorFilter(filters: List[ActorFilter]) extends ActorFilter {
  override def apply(actor: Actor): Boolean = {
    filters.foldLeft(false) {
      case (result, filter) => result || filter(actor)
    }
  }
}
