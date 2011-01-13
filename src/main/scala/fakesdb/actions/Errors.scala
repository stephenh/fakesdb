package fakesdb.actions

class SDBException(val code: String, val message: String, val httpStatus: Int = 400) extends RuntimeException(message)
