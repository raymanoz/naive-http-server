package io.shaka.http

import java.awt.image.DataBufferByte
import java.io.File
import javax.imageio.ImageIO._

import io.shaka.http.ContentType.{TEXT_CSS, TEXT_CSV, TEXT_HTML}
import io.shaka.http.Http._
import io.shaka.http.HttpServerSpecSupport.withHttpServer
import io.shaka.http.Request.GET
import io.shaka.http.StaticResponse.{classpathDocRoot, static}
import io.shaka.http.Status.NOT_FOUND
import org.scalatest.FunSuite

import scala.io.Source._

class ServingStaticResourcesSpec extends FunSuite {
  val docRoot = "./src/test/resources/web"

  test("can serve a static file from filesystem") {
    withHttpServer { (httpServer, rootUrl) =>
      httpServer.handler {
        case GET(path) => static(docRoot, path)
      }
      val response = http(GET(s"$rootUrl/test.html"))
      assert(response.entityAsString === fromFile(s"$docRoot/test.html").mkString)
    }
  }

  test("can serve image file from filesystem"){
    withHttpServer { (httpServer, rootUrl) =>
      httpServer.handler {
        case GET(path) => static(docRoot, path)
      }
      val response = http(GET(s"$rootUrl/clocks.png"))
      assert(response.entity.get.content === read(new File(s"$docRoot/clocks.png")).getData.getDataBuffer.asInstanceOf[DataBufferByte].getData)
    }
  }

  //  test("can serve pdf file from filesystem"){

  test("return NOT_FOUND when static resource does not exist") {
    withHttpServer { (httpServer, rootUrl) =>
      httpServer.handler {
        case GET(path) => static(docRoot, path)
      }
      assert(http(GET(s"$rootUrl/test2.html")).status === NOT_FOUND)
    }

  }

  test("correctly set content-type when serving static files") {
    withHttpServer { (httpServer, rootUrl) =>
      httpServer.handler {
        case GET(path) => static(docRoot, path)
      }
      assert(http(GET(s"$rootUrl/test.html")).contentType.get === TEXT_HTML)
      assert(http(GET(s"$rootUrl/test.css")).contentType.get === TEXT_CSS)
      assert(http(GET(s"$rootUrl/testdir/test.csv")).contentType.get === TEXT_CSV)
    }
  }

  test("shows directory listing when serving static resources") {
    withHttpServer { (httpServer, rootUrl) =>
      httpServer.handler {
        case GET(path) => static(docRoot, path)
      }
      val response = http(GET(s"$rootUrl/testdir"))
      assert(response.entityAsString.split("\n").sorted === List("test.csv", "testsubdir", "test.txt").sorted)
    }
  }

  test("can server a static file from the (file:) classpath") {
    withHttpServer { (httpServer, rootUrl) =>
      httpServer.handler {
        case GET(path) => static(classpathDocRoot("web"), path)
      }
      val response = http(GET(s"$rootUrl/test.html"))
      assert(response.entityAsString === fromFile(s"$docRoot/test.html").mkString)
    }
  }

  test("shows directory listing when serving static resources from (file:) classpath") {
    withHttpServer { (httpServer, rootUrl) =>
      httpServer.handler {
        case GET(path) => static(classpathDocRoot("web"), path)
      }
      val response = http(GET(s"$rootUrl/testdir"))
      assert(response.entityAsString.split("\n").sorted === List("test.csv", "testsubdir", "test.txt").sorted)
    }
  }

  test("can server a static file from a jar") {
    withHttpServer { (httpServer, rootUrl) =>
      httpServer.handler {
        case GET(path) => static(classpathDocRoot("docroot"), path)
      }
      val response = http(GET(s"$rootUrl/index.html"))
      assert(response.entityAsString === "<html>\n<body>\n<h1>Hellow world</h1>\n</body>\n</html>")
    }
  }

}
