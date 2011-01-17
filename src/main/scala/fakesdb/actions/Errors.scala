package fakesdb.actions

// http://docs.amazonwebservices.com/AmazonSimpleDB/latest/DeveloperGuide/index.html?APIError.html
class SDBException(val httpStatus: Int, val xmlCode: String, val message: String) extends RuntimeException(message)

class NumberItemAttributesExceededException
  extends SDBException(409, "NumberItemAttributesExceeded", "Too many attributes in this item")

class EmptyAttributeNameException
  extends SDBException(400, "InvalidParameterValue", "Value () for parameter Name is invalid. The empty string is an illegal attribute name")

class MissingItemNameException
  extends SDBException(400, "MissingParameter", "The request must contain the parameter ItemName.")
