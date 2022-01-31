import zio._
import zio.metrics._
import zio.json._
import zhttp.http._
import zhttp.http.middleware._
import zhttp.service.Server

trait Prebook {
  def httpApp: HttpApp[Any, Throwable]
}

object Prebook {

  def layer: ZLayer[Any, Nothing, Prebook] = ZLayer.fromEffect(for {
    ref <- Ref.make(0)
  } yield PrebookLive(ref))

  def httpApp = ZIO.serviceWith[Prebook](_.httpApp)

}

case class PrebookLive(ref: Ref[Int]) extends Prebook {

  implicit val codecLabel: JsonCodec[MetricLabel] = DeriveJsonCodec.gen[MetricLabel]
  implicit val codecBoundaries: JsonCodec[ZIOMetric.Histogram.Boundaries] = DeriveJsonCodec.gen[ZIOMetric.Histogram.Boundaries]
  implicit val codecKey: JsonCodec[MetricKey] = DeriveJsonCodec.gen[MetricKey]
  implicit val codecType: JsonCodec[MetricType] = DeriveJsonCodec.gen[MetricType]
  implicit val codecState: JsonCodec[MetricState] = DeriveJsonCodec.gen[MetricState]
  implicit val codec: JsonCodec[Map[MetricKey, MetricState]] = DeriveJsonCodec.gen[Map[MetricKey, MetricState]]

  lazy val webRequestsCounter: ZIOMetric.Counter[Any] =
    ZIOMetric.count("web-requests")

  lazy val requestDurations: ZIOMetric.Histogram[Any] =
    ZIOMetric.observeDurations(
      "web-request-durations",
      ZIOMetric.Histogram.Boundaries.linear(0, 100, 10)
    )(_.toMillis.toDouble)

  lazy val requestDurationsSummary: ZIOMetric.Summary[Double] =
    ZIOMetric.observeSummary(
      "summary",
      60.minutes,
      10,
      0,
      Chunk(0.5, 0.9, 0.95, 0.99, 0.999)
    )

  lazy val httpResponseStatusCodes: ZIOMetric.SetCount[Int] =
    ZIOMetric.occurrencesWith("service-return-codes", "count")(_.toString)

  lazy val webRequestsMiddleware: HttpMiddleware[Any, Nothing] =
    Middleware.runBefore(webRequestsCounter.increment)

  lazy val requestsDurationsMiddleware: HttpMiddleware[Any, Nothing] =
    Middleware.interceptZIO[Request, Response]({ request =>
      ZIO.succeed(java.lang.System.currentTimeMillis)
    })({ case (response, startTime) =>
      requestDurations
        .observe((java.lang.System.currentTimeMillis() - startTime).toDouble)
        .as(response)
    })

  lazy val httpResponseStatusCodesMiddleware: HttpMiddleware[Any, Nothing] =
    Middleware.identity.mapZIO { response: Response =>
      httpResponseStatusCodes.observe(response.status.toString).as(response)
    }

  lazy val metricsMiddleware: HttpMiddleware[Any, Nothing] =
    Middleware.interceptZIO[Request, Response]({
      case Method.GET -> !! / "healthcheck" =>
        ZIO.succeed(Some(MetricClient.unsafeStates))
      case request => ZIO.succeed(None)
    })({
      case (response, None)   => ZIO.succeed(response)
      case (_, Some(metrics)) => ZIO.succeed(Response.text(metrics.toJson))
    })

  val middlewares = webRequestsMiddleware ++ requestsDurationsMiddleware ++ httpResponseStatusCodesMiddleware

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

    val healthcheck = Http.collectZIO[Request] {
      case Method.GET -> !! / "healthcheck" =>
        ZIO.succeed(Response.text(MetricClient.unsafeStates.toList.toJson))
    }

    val app = Http.collect[Request] {
      case Method.GET -> !! / "text" => Response.text("Hello World!")
      case Method.GET -> !! / "json" =>
        Response.json("""{"greetings": "Hello World!"}""")
    }

    middlewares(prebook ++ app  ++ healthcheck)
  }
}
