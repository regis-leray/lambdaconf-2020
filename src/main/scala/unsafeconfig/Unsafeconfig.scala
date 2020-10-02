package unsafeconfig

import com.typesafe.config.{Config, ConfigFactory}
import scala.util.Try
import scala.jdk.CollectionConverters.CollectionHasAsScala

/***
 *
 * Unsafe code :
 * -------------
 * -> unsafe library typesafe.config :( - yeah its written typesafe :)
 * -> throw exception
 * -> class casting
 * -> too much if
 * -> duplicate code
 * -> no type safety with dataType
 *
 */
object UnsafeConfig {

  def getConfigSafely[T](config: Config, field: String, dataType: Class[T]): Option[T] = {
    if (config.hasPath(field)) {
      val ret = dataType match {
        case dt if dt == classOf[Config] => config.getConfig(field)
        case dt if dt == classOf[String] => config.getString(field)
        case dt if dt == classOf[Int] => config.getInt(field)
        //TODO: Handle more list types.
        case dt if dt == classOf[List[String]] => config.getStringList("inputs").asScala.toList
        //TODO: Handle Map data types in a better way
        case dt if dt == classOf[Map[Any, Any]] =>
          config.getConfig(field).entrySet()
            .asScala
            .map(e => (e.getKey, e.getValue.render().replace("\"", ""))).toMap
      }
      Some(ret.asInstanceOf[T])
    }
    else {
      None
    }
  }


  def getConfigOrFail[T](config: Config, field: String, dataType: Class[T]): T = {
    if (config.hasPath(field)) {
      val ret = dataType match {
        case dt if dt == classOf[Config] => config.getConfig(field)
        case dt if dt == classOf[String] => config.getString(field)
        case dt if dt == classOf[Int] => config.getInt(field)
        //TODO: Handle more list types.
        case dt if dt == classOf[List[String]] => config.getStringList("inputs").asScala.toList
        case dt if dt == classOf[Map[Any, Any]] =>
          config.getConfig(field).entrySet()
            .asScala
            .map(e => (e.getKey, e.getValue.render().replace("\"", ""))).toMap
      }
      ret.asInstanceOf[T]
    }
    else {
      throw new RuntimeException(s"$field is not a valid entry.")
    }
  }
}

/**
 *
 * 1. Remove class cast // if conditions
 * 2. Remove throw / make safe
 * 3. Remove duplication
 * 4. Polymorphic function + higher kind type
 *
 */
object SafeConfig {
  sealed trait Get[T] {
    def get(c: Config, path: String): T
  }

  implicit val stringGet: Get[String] = ???

  implicit val intGet: Get[Int] = ???

  implicit val listStringGet: Get[List[String]] = ???

  def get[T](config: Config, path: String)(g: Get[T]): T = ???
}


object Main {
  import unsafeconfig.SafeConfig._

  def main(args: Array[String]): Unit = {
    val cfg = ConfigFactory.load()
//
//    println(get[String](cfg, "notfound"))
//    println(get[String](cfg, "notfound"))
  }
}
