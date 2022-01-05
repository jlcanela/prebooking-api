import zhttp.http._
import zhttp.service.Server
import zio._

trait Prebook:
  def httpApp: ZIO[Any, Nothing, HttpApp[Any, Nothing]]

object Prebook:

  def layer: ULayer[Has[Prebook]] = ZLayer.fromEffect(for {
    ref <- Ref.make(0)
  } yield PrebookLive(ref))
  
  def httpApp: ZIO[Has[Prebook], Nothing, HttpApp[Any, Nothing]] = ZIO.serviceWith[Prebook](_.httpApp)

end Prebook

case class PrebookLive(ref: Ref[Int]) extends Prebook:
  def httpApp: ZIO[Any, Nothing, HttpApp[Any, Nothing]] = {

    val prebook: HttpApp[Any, Nothing] = Http.collectM[Request] {
      case Method.POST -> !! / "prebook" =>
        for {
          _ <- ref.update(_ + 1)
          count <- ref.get
        } yield Response.text(s"""{"count":${count}}""")
    }

    // Create HTTP route
    val app: HttpApp[Any, Nothing] = Http.collect[Request] {
      case Method.GET -> !! / "text" => Response.text("Hello World!")
      case Method.GET -> !! / "json" =>
        Response.json("""{"greetings": "Hello World!"}""")
    }

    return ZIO.succeed(prebook ++ app)
  }
