// swiftlint:disable all
import Amplify
import Foundation

extension DeviceInfo {
  // MARK: - CodingKeys 
   public enum CodingKeys: String, ModelKey {
    case id
    case notifyType
    case deviceToken
    case sessionId
    case participantId
    case participantToken
    case createdAt
    case updatedAt
  }
  
  public static let keys = CodingKeys.self
  //  MARK: - ModelSchema 
  
  public static let schema = defineSchema { model in
    let deviceInfo = DeviceInfo.keys
    
    model.authRules = [
      rule(allow: .public, operations: [.create, .update, .delete, .read])
    ]
    
    model.pluralName = "DeviceInfos"
    
    model.fields(
      .id(),
      .field(deviceInfo.notifyType, is: .required, ofType: .string),
      .field(deviceInfo.deviceToken, is: .required, ofType: .string),
      .field(deviceInfo.sessionId, is: .optional, ofType: .string),
      .field(deviceInfo.participantId, is: .optional, ofType: .string),
      .field(deviceInfo.participantToken, is: .optional, ofType: .string),
      .field(deviceInfo.createdAt, is: .optional, isReadOnly: true, ofType: .dateTime),
      .field(deviceInfo.updatedAt, is: .optional, isReadOnly: true, ofType: .dateTime)
    )
    }
}