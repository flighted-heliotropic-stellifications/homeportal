package io.github.flightedheliadicstellifications.homeportal

import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.MediaType
import org.http4s.headers.`Content-Type`
import scala.io.Source
import com.typesafe.config.Config
import org.http4s.StaticFile
import org.http4s.EntityDecoder
import cats.effect.IO
import fs2.io.file.Files
import fs2.io.file
import cats.effect.unsafe.implicits.global
import scala.sys.process._
import scala.util.Success
import scala.util.Failure
import scala.util.Try

object HomePortalRoutes {

  def printRoutes(config: Config): HttpRoutes[IO] = {
    val htmlDir = config.getString("app.html-dir")
    val outputDir = config.getString("app.print.output-dir")
    val dsl = new Http4sDsl[IO]{}
    import dsl._
    HttpRoutes.of[IO] {
      case request @ GET -> Root / "print" =>
        StaticFile
          .fromResource(s"/$htmlDir/print.html", Some(request))
          .getOrElseF(NotFound())
      case request @ POST -> Root / "print" =>
        EntityDecoder.mixedMultipartResource[IO]().use(decoder =>
          request.decodeWith(decoder, strict = true){ multipart =>
            val name = multipart.parts.head.filename.getOrElse("misc.pdf")
            val pathString = s"$outputDir/$name"
            val path = file.Path(pathString)
            multipart.parts.map(_.body).reduce(_ ++ _)
            .through(Files[IO].writeAll(path))
            .compile
            .drain
            .unsafeRunSync()

            Try {
              val cmd = s"lpr $pathString"
              val output = cmd.!!
              if (output.isEmpty()) {
                Ok()
              } else {
                InternalServerError(output)
              }
            }
            match {
              case Success(x) => x
              case Failure(ex) => 
                InternalServerError(ex.getMessage()) 
            }
          })
    }
  }


  def indexRoutes(config: Config): HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO]{}
    import dsl._
    val htmlDir = config.getString("app.html-dir")
    val indexTemplate: String = Source.fromFile(s"$htmlDir/index.html").mkString

    val calibrePort = config.getString("app.ports.calibre")
    val calibreWebPort = config.getString("app.ports.calibre-web")
    val jellyfinPort = config.getString("app.ports.jellyfin")
    val homeserverPort = config.getString("app.ports.homeportal")
    val homeserverHost = config.getString("app.hosts.homeportal")

    val finalIndex = indexTemplate
      .replaceAll("""\{\{CALIBRE_PORT\}\}""", calibrePort)
      .replaceAll("""\{\{CALIBRE_WEB_PORT\}\}""", calibreWebPort)
      .replaceAll("""\{\{JELLYFIN_PORT\}\}""", jellyfinPort)
      .replaceAll("""\{\{SERVICE_PORT\}\}""", homeserverPort)
      .replaceAll("""\{\{HOST\}\}""", homeserverHost)

    HttpRoutes.of[IO] {
      case GET -> Root | GET -> Root / "index.html" =>
        Ok(finalIndex).map(_.withContentType(`Content-Type`(MediaType.text.html)))
    }
  }

}
