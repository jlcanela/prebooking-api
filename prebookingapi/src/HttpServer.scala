import zhttp.http._
import zhttp.service.Server
import zio._

object HttpServer extends App {

  def server = for {
    ha <- Prebook.httpApp
    _ <-  Server.app(ha).withPort(8090).startDefault 
  } yield ()

  def app = server.provideLayer(Prebook.layer)
  // Run it like any simple app
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = 
    app.exitCode
}
