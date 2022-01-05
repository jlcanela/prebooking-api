import mill._, scalalib._

object prebookingapi extends ScalaModule {

  val ZIO_V2 = "2.0.0-RC1"
  val ZIO_V1 = "1.0.13"

  val a = 1
  
  def scalaVersion = "3.1.0"

  def ivyDeps = Agg(
    ivy"dev.zio::zio::${ZIO_V1}",
 //   ivy"com.scalawilliam::xs4s-zio:0.9.1",
    ivy"io.d11::zhttp:1.0.0.0-RC21",
 //   ivy"dev.zio::zio-cli:0.1.0"

  )

  override def mainClass = T { Some("HttpServer") }

}
