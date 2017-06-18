package pl.edu.agh.iet.akka_tracing.config

import com.typesafe.config.Config

import scala.collection.JavaConverters._
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.util.Try

private[akka_tracing] object ConfigUtils {

  private val FakePath = "fakePath"

  trait ConfigValueReader[T] {
    def fromConfig(config: Config, path: String): Option[T]
  }

  implicit val booleanConfigValueReader = new ConfigValueReader[Boolean] {
    override def fromConfig(config: Config, path: String): Option[Boolean] = {
      Try(config.getBoolean(path)).toOption
    }
  }

  implicit val doubleConfigValueReader = new ConfigValueReader[Double] {
    override def fromConfig(config: Config, path: String): Option[Double] = {
      Try(config.getDouble(path)).toOption
    }
  }

  implicit val intConfigValueReader = new ConfigValueReader[Int] {
    override def fromConfig(config: Config, path: String): Option[Int] = {
      Try(config.getInt(path)).toOption
    }
  }

  implicit val stringConfigValueReader = new ConfigValueReader[String] {
    override def fromConfig(config: Config, path: String): Option[String] = {
      Try(config.getString(path)).toOption
    }
  }

  implicit val configConfigValueReader = new ConfigValueReader[Config] {
    override def fromConfig(config: Config, path: String): Option[Config] = {
      Try(config.getConfig(path)).toOption
    }
  }

  implicit def traversableConfigValueReader[T: ConfigValueReader, C[_]]
  (implicit cbf: CanBuildFrom[Nothing, T, C[T]]) = new ConfigValueReader[C[T]] {
    override def fromConfig(config: Config, path: String): Option[C[T]] = {
      Try({
        val reader = implicitly[ConfigValueReader[T]]
        val list = config.getList(path).asScala
        val builder = cbf()
        builder.sizeHint(list.size)
        list foreach { item =>
          val entryConfig = item.atPath(FakePath)
          builder += reader.fromConfig(entryConfig, FakePath).get
        }
        builder.result()
      }).toOption
    }
  }

  implicit class RichConfig(config: Config) {
    def getOption[T: ConfigValueReader](path: String): Option[T] =
      implicitly[ConfigValueReader[T]].fromConfig(config, path)

    def getOrElse[T: ConfigValueReader](path: String, default: => T): T = {
      getOption(path)(implicitly[ConfigValueReader[T]]).getOrElse(default)
    }
  }

}
