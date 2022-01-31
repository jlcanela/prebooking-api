import zhttp.http._
import zhttp.service.Server
import zio._

object HttpServer extends App {

  def server = for {
    prebook <- Prebook.httpApp
    healthcheck <- HealthCheck.healthcheck
    middlewares <- Middlewares.middlewares
    app = middlewares(prebook ++ healthcheck)
    _ <-  Server.app(app).withPort(8090).startDefault 
  } yield ()

  def app = server.provide(Prebook.layer, HealthCheck.layer, Middlewares.layer)
    .tapError(Console.printLine(_)) // display error message when server already started
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = 
    app.exitCode
}
