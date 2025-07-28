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

  test("index.html has five links") {
    val messageString = retIndex.flatMap(_.as[String])
    val linkPattern = "<a href".r
    val linkCount = messageString.map(msg => linkPattern.findAllIn(msg).size)
    assertIO(linkCount, 5)
  }

  test("index.html has favicon attribution") {
    val messageString = retIndex.flatMap(_.as[String])
    val messageStringContains = messageString.map(msg => msg.contains("""favicon by <a href="https://www.vecteezy.com/free-vector/possum-silhouette">Jannatul Ferdous at Vecteezy</a>"""))
    assertIO(messageStringContains, true)
  }

  private[this] val retIndex: IO[Response[IO]] = {
    val getHW = Request[IO](Method.GET, uri"/")
    val routes = new HomePortalRoutes(config)
    routes.indexRoutes.orNotFound(getHW)
  }
}