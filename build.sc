import mill._, scalalib._

object prebookingapi extends ScalaModule {

  val a = 2
  val ZioVersion        = "2.0.0-RC1"
  val ZioJsonVersion    = "0.3.0-RC2"
  val ZioHttpVersion    = "2.0.0-RC2"
  val ZioConfigVersion  = "3.0.0-RC1"
  val ZioSchemaVersion  = "0.2.0-RC1-1"
  val ZioLoggingVersion = "2.0.0-RC4"
  val ZioZmxVersion     = "2.0.0-M1"
  
  // def scalaVersion = "3.1.0"
  def scalaVersion = "2.13.8"

  def ivyDeps = Agg(
    ivy"dev.zio::zio::${ZioVersion}",
 //   ivy"com.scalawilliam::xs4s-zio:0.9.1",
    ivy"io.d11::zhttp:${ZioHttpVersion}",
    ivy"dev.zio::zio-json:${ZioJsonVersion}",
 //   ivy"dev.zio::zio-cli:0.1.0"

  )

  override def mainClass = T { Some("Cli") }

}
