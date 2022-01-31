import zhttp.http._
import zhttp.service.Server
import zio._

object HttpServer extends App {

  def server = for {
    app <- Prebook.httpApp
    _ <-  Server.app(app).withPort(8090).startDefault 
  } yield ()

  def app = server.provideLayer(Prebook.layer)
    .tapError(Console.printLine(_)) // display error message when server already started
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = 
    app.exitCode
}
