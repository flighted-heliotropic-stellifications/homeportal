package io.github.flightedheliadicstellifications.homeserver

import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import munit.CatsEffectSuite
import io.github.flightedheliadicstellifications.homeportal.HomePortalRoutes
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory


class RootSpec extends CatsEffectSuite {

  val config: Config = ConfigFactory.load()

  test("root returns status code 200") {
    assertIO(retIndex.map(_.status) ,Status.Ok)
  }

  test("index.html has four links") {
    val messageString = retIndex.flatMap(_.as[String])
    val linkPattern = "<a href".r
    val linkCount = messageString.map(msg => linkPattern.findAllIn(msg).size)
    assertIO(linkCount, 4)
    
  }

  private[this] val retIndex: IO[Response[IO]] = {
    val getHW = Request[IO](Method.GET, uri"/")
    val routes = new HomePortalRoutes(config)
    routes.indexRoutes.orNotFound(getHW)
  }
}