package fakesdb.actions

class SDBException(val code: String, val message: String) extends RuntimeException(message)
