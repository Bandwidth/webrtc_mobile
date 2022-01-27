// swiftlint:disable all
import Amplify
import Foundation

public struct DeviceInfo: Model {
  public let id: String
  public var notifyType: String
  public var deviceToken: String
  public var sessionId: String?
  public var participantId: String?
  public var participantToken: String?
  public var createdAt: Temporal.DateTime?
  public var updatedAt: Temporal.DateTime?
  
  public init(id: String = UUID().uuidString,
      notifyType: String,
      deviceToken: String,
      sessionId: String? = nil,
      participantId: String? = nil,
      participantToken: String? = nil) {
    self.init(id: id,
      notifyType: notifyType,
      deviceToken: deviceToken,
      sessionId: sessionId,
      participantId: participantId,
      participantToken: participantToken,
      createdAt: nil,
      updatedAt: nil)
  }
  internal init(id: String = UUID().uuidString,
      notifyType: String,
      deviceToken: String,
      sessionId: String? = nil,
      participantId: String? = nil,
      participantToken: String? = nil,
      createdAt: Temporal.DateTime? = nil,
      updatedAt: Temporal.DateTime? = nil) {
      self.id = id
      self.notifyType = notifyType
      self.deviceToken = deviceToken
      self.sessionId = sessionId
      self.participantId = participantId
      self.participantToken = participantToken
      self.createdAt = createdAt
      self.updatedAt = updatedAt
  }
}