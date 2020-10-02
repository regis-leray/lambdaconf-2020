package zipfiles.zio

import java.io.{ByteArrayOutputStream, IOException}
import java.nio.file.Paths
import java.util.zip.{ZipOutputStream, ZipEntry => JZipEntry}

import zio.blocking._
import zio.console.Console
import zio.stream.{Stream, ZSink, ZStream}
import zio.{Chunk, ExitCode, Managed, Task, URIO, ZIO}

object ZioZipArchive extends zio.App {

  sealed trait Command

  case object OpenZipFile extends Command
  case class StartEntry(name: String) extends Command
  case class AddDataToZipEntry(data: Chunk[Byte]) extends Command
  case object EndEntry extends Command
  case object CloseZipFile extends Command

  private[this] class ZipCompressor() {
    private val buf = new ByteArrayOutputStream(2048)
    private val zip = new ZipOutputStream(buf)

    def execute(f: ZipOutputStream => Unit): ZIO[Blocking, IOException, Chunk[Byte]] = effectBlockingIO {
      f(zip)
      val zippedData = buf.toByteArray
      buf.reset()
      Chunk.fromArray(zippedData)
    }.refineToOrDie[IOException]
  }

  type ZipEntry[R] = (String, ZStream[R, Throwable, Byte])

  def compresszip[R <: Blocking](sources: Stream[Nothing, ZipEntry[R]]): ZStream[R, Throwable, Byte] = for {
    compressor <- ZStream.managed(Managed.make(Task(new ZipCompressor()))(_.execute(_.close()).ignore))

    commands = ZStream(OpenZipFile) ++ sources.flatMap { case (name, source) =>
      ZStream(StartEntry(name)) ++ source.mapChunks(c => Chunk(AddDataToZipEntry(c))) ++ ZStream(EndEntry)
    } ++ ZStream(CloseZipFile)

    data <- commands.mapM {
      case OpenZipFile =>
        ZIO.succeed(Chunk.empty)

      case StartEntry(filePath) =>
        compressor.execute(_.putNextEntry(new JZipEntry(filePath)))

      case AddDataToZipEntry(data) =>
        compressor.execute(_.write(data.toArray))

      case EndEntry =>
        compressor.execute(_.closeEntry)

      case CloseZipFile =>
        compressor.execute(_.close)
    }.flattenChunks
  } yield data

  override def run(args: List[String]): URIO[Blocking with Console, ExitCode] = {
    val files = ZStream(
      ("RL_resum.pdf" -> ZStream.fromFile(Paths.get("/Users/regis/Desktop/RL_resum.pdf")) ),
      ("RL_resum.pages" -> ZStream.fromFile(Paths.get("/Users/regis/Desktop/RL_resum.pages")) )
    )

    val target = ZSink.fromFile(Paths.get("/Users/regis/Desktop/hello.zip"))

    compresszip(files).run(target).exitCode
  }


}
