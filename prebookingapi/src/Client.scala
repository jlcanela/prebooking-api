import zhttp.http.{Header, HttpData}
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio._
import zhttp.http.Headers
import zhttp.http.Method
import zhttp.http.URL

object SimpleClient extends App {

  val env     = ChannelFactory.auto ++ EventLoopGroup.auto()

  val post: Method = Method.POST
  val url: String  = "http://localhost:8090/prebook"
  val headers: Headers = Headers.apply("Content-Type", "application/json")
  val content: HttpData[Any, Nothing] = HttpData.fromString("""{"day":"2022/01/01", "user":1234}""")
  
  val call = for {    
    u <- ZIO.fromEither(URL.fromString(url))
    res <- Client.request(post, u, headers, content)
    body <- res.getBodyAsString
  } yield body

  val status = for {
    res <- Client.request(url, headers)
    body <- res.getBodyAsString
  } yield body
  val n = 25000
  
  val program = for {
    start <- clock.nanoTime
    count <- ZIO.mergeAllParN(16)(Iterable.fill(n)(call))(0)((acc, _) => acc + 1)
    end <- clock.nanoTime
    _ <- console.putStrLn(s"${(end-start)/(1000000)} ms for ${count} items")
    res <- status
    _ <- console.putStrLn(res)
  } yield ()

  def app = program.provideCustomLayer(env)
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = 
    app.exitCode

}