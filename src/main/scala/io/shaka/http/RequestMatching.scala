package io.shaka.http

import io.shaka.http.HttpHeader.ACCEPT

object RequestMatching {
  object && {
    def unapply[A](a: A) = Some((a, a))
  }

  implicit class HttpStringContext(val sc: StringContext) extends AnyVal {
    def url = sc.parts.mkString("(.+)")
      .replaceAllLiterally("?", "\\?")
      .r
  }

  object Accept {
    def unapply(request: Request) = if (request.headers.contains(ACCEPT)) request.headers(ACCEPT).headOption.map(ContentType.contentType) else None
  }

}
