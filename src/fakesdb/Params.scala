package fakesdb

import javax.servlet.http.HttpServletRequest
import scala.collection.mutable.HashMap

class Params extends HashMap[String, String] {
}

object Params {
  def apply(request: HttpServletRequest): Params = {
    val p = new Params
    val i = request.getParameterMap.asInstanceOf[java.util.Map[String, Array[String]]].entrySet.iterator
    while (i.hasNext) {
      val e = i.next ; p.update(e.getKey(), e.getValue()(0))
    }
    return p
  }
}
