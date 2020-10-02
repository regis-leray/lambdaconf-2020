package zipfiles.fs2

import java.io.ByteArrayOutputStream
import java.nio.file.Paths
import java.util.zip.{ZipOutputStream, ZipEntry => JZipEntry}

import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import fs2.{Chunk, Pure, Stream}


object Fs2ZipArchive extends IOApp {

  sealed trait Command
  case object OpenZipFile extends Command
  case class StartEntry(name: String) extends Command
  case class AddDataToZipEntry(data: Chunk[Byte]) extends Command
  case object EndEntry extends Command
  case object CloseZipFile extends Command

  private[this] class ZipCompressor(blocker: Blocker) extends AutoCloseable{
    private val buf = new ByteArrayOutputStream(2048)
    private val zip = new ZipOutputStream(buf)

    def execute(f: ZipOutputStream => Unit): IO[Chunk[Byte]] =
      blocker.delay[IO, Chunk[Byte]] {
        f(zip)
        val zippedData = buf.toByteArray
        buf.reset()
        Chunk.array(zippedData)
      }

    override def close(): Unit = zip.close()
  }

  type ZipEntry = (String, Stream[IO, Byte])

  def zip(sources: Stream[IO, ZipEntry])(blocker: Blocker): Stream[IO, Byte] = for {
    compressor <- Stream.resource(
      Resource.fromAutoCloseable(IO(new ZipCompressor(blocker)))
    )

    commands = Stream(OpenZipFile) ++ sources.flatMap { case (name, source) =>
      Stream(StartEntry(name)) ++ source.chunkAll.map(AddDataToZipEntry) ++ Stream(EndEntry)
    } ++ Stream(CloseZipFile)

    data <- commands.evalMap {
      case OpenZipFile =>
        IO.pure(Chunk.empty[Byte])

      case StartEntry(filePath) =>
        compressor.execute(_.putNextEntry(new JZipEntry(filePath)))

      case AddDataToZipEntry(data) =>
        compressor.execute(_.write(data.toArray))

      case EndEntry =>
        compressor.execute(_.closeEntry)

      case CloseZipFile =>
        compressor.execute(_.close)
    }.flatMap(Stream.chunk)
  } yield data

  override def run(args: List[String]): IO[ExitCode] = {
    val program = for {
      blocker <- Stream.resource(Blocker[IO])
      data: Stream[Pure, (String, Stream[IO, Byte])] = Stream(
        ("RL_resum.pdf" -> fs2.io.file.readAll[IO](Paths.get("/Users/regis/Desktop/RL_resum.pdf"), blocker, 2048)),
        ("RL_resum.pages" -> fs2.io.file.readAll[IO](Paths.get("/Users/regis/Desktop/RL_resum.pages"), blocker, 2048))
      )

      writeToFile = fs2.io.file.writeAll[IO](Paths.get("/Users/regis/Desktop/hello.zip"), blocker)

      _ <- writeToFile(
        zip(data)(blocker)
      )
    } yield ()

    program.compile.drain.as(ExitCode.Success)
  }
}
