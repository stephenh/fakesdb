package fakesdb.actions

class SDBException(val httpCode: Int, val xmlCode: String, val message: String) extends RuntimeException(message)
