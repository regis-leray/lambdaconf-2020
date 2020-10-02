package zipfiles

import java.io.{File, FileInputStream, FileOutputStream}
import java.nio.file.Paths
import java.util.zip.{ZipOutputStream, ZipEntry => JZipEntry}

import scala.util.Using

object UnsafeZipArchive {

  def zipfiles(files: List[File], outZip: File): Unit =
    Using(new ZipOutputStream(new FileOutputStream(outZip))){ zipOut =>
      for (file <- files)
        writeFile(file, zipOut)
    }

  private def writeFile(fileToZip: File, zipOut: ZipOutputStream): Unit = {
    val fileName: String = fileToZip.getName
    if (fileToZip.isHidden) return

    Using(new FileInputStream(fileToZip)){ fis =>
      val zipEntry = new JZipEntry(fileName)
      zipOut.putNextEntry(zipEntry)
      val bytes = new Array[Byte](1024)
      var length = 0

      while ( {
        length = fis.read(bytes);
        length
      } >= 0) zipOut.write(bytes, 0, length)
    }
  }

  def main(args: Array[String]): Unit = {
    val files = List(
      Paths.get("/Users/regis/Desktop/1.pdf").toFile,
      Paths.get("/Users/regis/Desktop/2.pdf").toFile,

    )

    zipfiles(files, new File("/Users/regis/Desktop/hello.zip"))
  }
}





