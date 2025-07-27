package io.github.flightedheliadicstellifications.homeportal

import cats.effect.IOApp

object Main extends IOApp.Simple {
  val run = HomePortalServer.run
}
