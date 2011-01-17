package fakesdb.actions

// http://docs.amazonwebservices.com/AmazonSimpleDB/latest/DeveloperGuide/index.html?APIError.html
class SDBException(val httpStatus: Int, val xmlCode: String, val message: String) extends RuntimeException(message)

class NumberItemAttributesExceededException
  extends SDBException(409, "NumberItemAttributesExceeded", "Too many attributes in this item")

class NumberSubmittedItemsExceeded
  extends SDBException(409, "NumberSubmittedItemsExceeded", "Too many items in a single call. Up to 25 items per call allowed.")

class EmptyAttributeNameException
  extends SDBException(400, "InvalidParameterValue", "Value () for parameter Name is invalid. The empty string is an illegal attribute name")

class MissingItemNameException
  extends SDBException(400, "MissingParameter", "The request must contain the parameter ItemName.")

class InvalidParameterValue(message: String)
  extends SDBException(400, "InvalidParameterValue", message)

object InvalidParameterValue {
  def failIfOver1024(name: String, value: String): Unit = {
    if (value.getBytes.size > 1024) {
      throw new InvalidParameterValue("Value (\"%s\") for parameter %s is invalid. Value exceeds maximum length of 1024.".format(value, name));
    }
  }
}