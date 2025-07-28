package io.github.flightedheliadicstellifications.homeportal

import cats.syntax.all._
import com.comcast.ip4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import org.http4s.server.staticcontent._
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import cats.effect.IO

object HomePortalServer {

  def run: IO[Nothing] = {

    val config: Config = ConfigFactory.load()
    val homePortalPort = config.getInt("app.ports.homeportal")

    val homePortalRoutes = new HomePortalRoutes(config)

    val httpApp = (
        homePortalRoutes.indexRoutes <+>
        homePortalRoutes.printRoutes <+>
        resourceServiceBuilder("/").toRoutes
      ).orNotFound

    val finalHttpApp = Logger.httpApp(true, true)(httpApp)

    EmberServerBuilder.default[IO]
          .withHost(ipv4"0.0.0.0")
          .withPort(Port.fromInt(homePortalPort).get)
          .withHttpApp(finalHttpApp)
          .build
  }.useForever
}
