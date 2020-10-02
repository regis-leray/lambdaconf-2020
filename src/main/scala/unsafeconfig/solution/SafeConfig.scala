package unsafeconfig.solution

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Try

object SafeConfig {
  // option can be expressed since we only have one type
  // Option[T] == F[_]

  // create a new type/alias of Either where we fixed Left side as 'Throwable'
  // we can now express Either as Single Type Constructor
  type ErrorOf[T] = Either[Throwable, T]

  // Try[T] == Try[_] == F[_]
  // Option[T] == Option[_] == G[_]
  // ErrorOf[T] == ErrorOf[_] == G[_]

  // Try[T] => Option[T] == F[_] => G[_]
  // Try[T] => ErrorOf[T] == F[_] => G[_]

  sealed trait FunctionK[F[_], G[_]] {
    def apply[A](fa: F[A]): G[A]
  }

  implicit val functionKOption = new FunctionK[Try, Option] {
    override def apply[T](t: Try[T]): scala.Option[T] = {
      t.toOption
    }
  }

  implicit val functionKEither = new FunctionK[Try, ErrorOf] {
    override def apply[T](t: Try[T]): ErrorOf[T] = {
      t.toEither
    }
  }

  sealed trait Get[T] {
    def get(c: Config, path: String): Try[T]
  }

  implicit val stringGet = new Get[String] {
    override def get(c: Config, path: String): Try[String] = Try(c.getString(path))
  }

  def get[F[_], T](config: Config, path: String)(implicit functionK: FunctionK[Try, F], g: Get[T]): F[T] = {
    functionK(g.get(config, path))
  }

  def main(args: Array[String]): Unit = {
    //how to use it
    val config = ConfigFactory.load()

    val valWithError: ErrorOf[String] = get[ErrorOf, String](config, "my.path")
    val valWithOption: Option[String] = get[Option, String](config, "my.path")
  }
}
