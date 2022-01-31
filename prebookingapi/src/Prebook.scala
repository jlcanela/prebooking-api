import zio._
import zio.metrics._
import zio.json._
import zhttp.http._
import zhttp.http.middleware._
import zhttp.service.Server

trait Prebook {
  def httpApp: HttpApp[Any, Throwable]
}

case class Metrics(metrics: Map[MetricKey, MetricState])

object Prebook {

  def layer: ZLayer[Any, Nothing, Prebook] = ZLayer.fromEffect(for {
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

    val app = Http.collect[Request] {
      case Method.GET -> !! / "text" => Response.text("Hello World!")
      case Method.GET -> !! / "json" =>
        Response.json("""{"greetings": "Hello World!"}""")
    }

    prebook ++ app
  }
}
