package fakesdb.actions

// http://docs.amazonwebservices.com/AmazonSimpleDB/latest/DeveloperGuide/index.html?APIError.html
class SDBException(val httpStatus: Int, val xmlCode: String, val message: String) extends RuntimeException(message)
