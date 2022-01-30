import zio._
import zhttp.http.Http

object Cli extends App {

  def program(args: List[String]) = args match {
    case List("serve") => HttpServer.app
    case List("test") => SimpleClient.app
    case _ => Console.printLine(s"command '${args}' not recognized")
  } 

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = 
    program(args).exitCode//.provideCustomLayer(env)

}