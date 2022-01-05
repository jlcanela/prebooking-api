import zhttp.http._
import zhttp.service.Server
import zio._

object HttpServer extends App {

  def server = for {
    app <- Prebook.httpApp
    s <-  Server.start(8090, app.silent)
  } yield s
  // Run it like any simple app
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = 
    server.provideLayer(Prebook.layer).exitCode
}
