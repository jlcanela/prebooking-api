import zhttp.http._
import zhttp.service.Server
import zio._

trait Prebook {
  def httpApp: HttpApp[Any, Throwable]
}

object Prebook {

  def layer = ZLayer.fromEffect(for {
    ref <- Ref.make(0)
  } yield PrebookLive(ref))
  
  def httpApp = ZIO.serviceWith[Prebook](_.httpApp)

}

case class PrebookLive(ref: Ref[Int]) extends Prebook {
  def httpApp: HttpApp[Any, Throwable] = {

    val prebook = Http.collectZIO[Request] {
      case Method.POST -> !! / "prebook" =>
        for {
          _ <- ref.update(_ + 1)
          count <- ref.get
        } yield Response.text(s"""{"count":${count}}""")
      case Method.GET -> !! / "prebook" =>
        for {
          count <- ref.get
        } yield Response.text(s"""{"count":${count}}""")
     }

    val app =  Http.collect[Request] {
      case Method.GET -> !! / "test" => Response.text("Hello World!")
      case Method.GET -> !! / "text" => Response.text("Hello World!")
      case Method.GET -> !! / "json" =>
        Response.json("""{"greetings": "Hello World!"}""")
    }

    prebook ++ app
  }
}