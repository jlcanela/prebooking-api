import zio._
import zio.metrics._
import zio.json._
import zhttp.http._
import zhttp.http.middleware._

trait Middlewares {
    def middlewares: HttpMiddleware[Any, Nothing]
}

object Middlewares {

    def layer: ZLayer[Any, Nothing, Middlewares] = ZIO.succeed(MiddlewaresLive()).toLayer

    def middlewares = ZIO.serviceWith[Middlewares](_.middlewares)

}

case class MiddlewaresLive() extends Middlewares {
 
  // web-requests metric
  lazy val webRequestsCounter: ZIOMetric.Counter[Any] =
    ZIOMetric.count("web-requests")

  lazy val webRequestsMiddleware: HttpMiddleware[Any, Nothing] =
      Middleware.runBefore(webRequestsCounter.increment)

  // service-return-codes metric
  lazy val httpResponseStatusCodes: ZIOMetric.SetCount[Int] =
    ZIOMetric.occurrencesWith("service-return-codes", "count")(_.toString)
    
  lazy val httpResponseStatusCodesMiddleware: HttpMiddleware[Any, Nothing] =
    Middleware.identity.mapZIO { response: Response =>
      httpResponseStatusCodes.observe(response.status.toString).as(response)
    }

  // web-request-durations metric
  lazy val requestDurations: ZIOMetric.Histogram[Any] =
    ZIOMetric.observeDurations(
      "web-request-durations",
      ZIOMetric.Histogram.Boundaries.linear(0, 100, 10)
    )(_.toMillis.toDouble)

  lazy val requestsDurationsMiddleware: HttpMiddleware[Any, Nothing] =
    Middleware.interceptZIO[Request, Response]({ request =>
      ZIO.succeed(java.lang.System.currentTimeMillis)
    })({ case (response, startTime) =>
      requestDurations
        .observe((java.lang.System.currentTimeMillis() - startTime).toDouble)
        .as(response)
    })

  // lazy val requestDurationsSummary: ZIOMetric.Summary[Double] =
  //   ZIOMetric.observeSummary(
  //     "summary",
  //     60.minutes,
  //     10,
  //     0,
  //     Chunk(0.5, 0.9, 0.95, 0.99, 0.999)
  //   )

  def middlewares = webRequestsMiddleware ++ requestsDurationsMiddleware ++ httpResponseStatusCodesMiddleware


}