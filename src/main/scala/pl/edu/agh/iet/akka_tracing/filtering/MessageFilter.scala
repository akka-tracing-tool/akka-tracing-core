package pl.edu.agh.iet.akka_tracing.filtering

trait MessageFilter {
  def apply(message: Any): Boolean
}

class ByClassesAllowMessageFilter(allowedClasses: List[Class[_]]) extends MessageFilter {
  override def apply(message: Any): Boolean = {
    allowedClasses.exists(`class` => `class`.isInstance(message))
  }
}

class ByClassesDenyMessageFilter(deniedClasses: List[Class[_]]) extends MessageFilter {
  override def apply(message: Any): Boolean = {
    !deniedClasses.exists(`class` => `class`.isInstance(message))
  }
}

class StackedConjunctionMessageFilter(filters: List[MessageFilter]) extends MessageFilter {
  override def apply(message: Any): Boolean = {
    filters.foldLeft(true) {
      case (result, filter) => result && filter(message)
    }
  }
}

class StackedDisjunctionMessageFilter(filters: List[MessageFilter]) extends MessageFilter {
  override def apply(message: Any): Boolean = {
    filters.foldLeft(false) {
      case (result, filter) => result || filter(message)
    }
  }
}
