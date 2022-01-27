// swiftlint:disable all
import Amplify
import Foundation

public struct Person: Model {
  public let id: String
  public var firstName: String
  public var lastName: String
  public var clientId: String
  public var createdAt: Temporal.DateTime?
  public var updatedAt: Temporal.DateTime?
  
  public init(id: String = UUID().uuidString,
      firstName: String,
      lastName: String,
      clientId: String) {
    self.init(id: id,
      firstName: firstName,
      lastName: lastName,
      clientId: clientId,
      createdAt: nil,
      updatedAt: nil)
  }
  internal init(id: String = UUID().uuidString,
      firstName: String,
      lastName: String,
      clientId: String,
      createdAt: Temporal.DateTime? = nil,
      updatedAt: Temporal.DateTime? = nil) {
      self.id = id
      self.firstName = firstName
      self.lastName = lastName
      self.clientId = clientId
      self.createdAt = createdAt
      self.updatedAt = updatedAt
  }
}
