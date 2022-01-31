import zio._
import zio.metrics._
import zio.json._
import zhttp.http._
import zhttp.http.middleware._
import zhttp.service.Server

trait HealthCheck {
  def healthcheck: HttpApp[Any, Throwable]
}

object HealthCheck {

  def layer: ZLayer[Any, Nothing, HealthCheck] = ZIO.succeed(HealthCheckLive()).toLayer

  def healthcheck = ZIO.serviceWith[HealthCheck](_.healthcheck)

}

case class HealthCheckLive() extends HealthCheck {

    implicit val codecLabel: JsonCodec[MetricLabel] = DeriveJsonCodec.gen[MetricLabel]
    implicit val codecBoundaries: JsonCodec[ZIOMetric.Histogram.Boundaries] = DeriveJsonCodec.gen[ZIOMetric.Histogram.Boundaries]
    implicit val codecKey: JsonCodec[MetricKey] = DeriveJsonCodec.gen[MetricKey]
    implicit val codecType: JsonCodec[MetricType] = DeriveJsonCodec.gen[MetricType]
    implicit val codecState: JsonCodec[MetricState] = DeriveJsonCodec.gen[MetricState]

    // unable to compile DeriveJsonCodec.gen[Map[(MetricKey, MetricState)]]
    // "magnolia: could not infer DeriveJsonEncoder.Typeclass for type Map[zio.metrics.MetricKey,zio.metrics.MetricState]"
    // implicit val codec: JsonCodec[Map[MetricKey, MetricState]] = DeriveJsonCodec.gen[Map[MetricKey, MetricState]]
    implicit val codec: JsonCodec[List[(MetricKey, MetricState)]] = DeriveJsonCodec.gen[List[(MetricKey, MetricState)]]

    def healthcheck: HttpApp[Any, Throwable] = Http.collectZIO[Request] {
      case Method.GET -> !! / "healthcheck" =>
        // Should not use toList to encode to Json, but bug with implicit val codec
        // ZIO.succeed(Response.text(MetricClient.unsafeStates.toJson))
        ZIO.succeed(Response.text(MetricClient.unsafeStates.toList.toJson))
    }
}
