// swiftlint:disable all
import Amplify
import Foundation

extension Person {
  // MARK: - CodingKeys 
   public enum CodingKeys: String, ModelKey {
    case id
    case firstName
    case lastName
    case clientId
    case createdAt
    case updatedAt
  }
  
  public static let keys = CodingKeys.self
  //  MARK: - ModelSchema 
  
  public static let schema = defineSchema { model in
    let person = Person.keys
    
    model.authRules = [
      rule(allow: .public, operations: [.create, .update, .delete, .read])
    ]
    
    model.pluralName = "People"
    
    model.fields(
      .id(),
      .field(person.firstName, is: .required, ofType: .string),
      .field(person.lastName, is: .required, ofType: .string),
      .field(person.clientId, is: .required, ofType: .string),
      .field(person.createdAt, is: .optional, isReadOnly: true, ofType: .dateTime),
      .field(person.updatedAt, is: .optional, isReadOnly: true, ofType: .dateTime)
    )
    }
}