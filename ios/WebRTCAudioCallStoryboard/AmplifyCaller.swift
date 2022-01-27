//
//  AmplifyCaller.swift
//  sample-app-webrtc
//
//  Created by Andrew Browne on 1/21/22.
//

// This class acts as a wrapper for all Amplify invocations
// This includes creating/updating people, as well as hitting the endpoint rest API

import Foundation
import Amplify
import AWSCognitoAuthPlugin
import AWSAPIPlugin
import AWSDataStorePlugin

public var amplifyCaller = AmplifyCaller()



public class AmplifyCaller {
    
    public var people: [Person] = [Person]()
    
    public init() {
        configure()
        signOutLocally()
    }
    public func configure() {
        
        do {
            try Amplify.add(plugin: AWSCognitoAuthPlugin())
            try Amplify.add(plugin: AWSDataStorePlugin(modelRegistration: AmplifyModels()))
            try Amplify.add(plugin: AWSAPIPlugin(modelRegistration: AmplifyModels()))
            
            Amplify.Logging.logLevel = .error
            
            try Amplify.configure()
        } catch {
            print("Could not initialize Amplify: \(error)")
        }
        
        print("Amplify configured successfully")

    }
    public func signIn(username: String, password: String) {
        Amplify.Auth.signIn(username: username, password: password) {
            result in
            switch result {
            case .success:
                print("Amplify: Sign in succeeded")
            case .failure(let error):
                print("Amplify: Sign in failed \(error)")
            }
        }
    }
    public func signOutLocally() {
        Amplify.Auth.signOut() {
            result in
            switch result {
            case .success:
                print("Amplify: successfully signed out locally")
            case .failure(let error):
                print("Sign out failed with error \(error)")
            }
        }
    }
    public func signUp(username: String, password: String, email: String) {
        let userAttributes = [AuthUserAttribute(.email, value: email)]
        let options = AuthSignUpRequest.Options(userAttributes: userAttributes)
        Amplify.Auth.signUp(username: username, password: password, options: options) { result in
            switch result {
            case .success(let signUpResult):
                if case let .confirmUser(deliveryDetails, _) = signUpResult.nextStep {
                    print("Delivery details \(String(describing: deliveryDetails))")
                } else {
                    print("SignUp Complete")
                }
            case .failure(let error):
                print("Amplify: An error occurred while registering a user \(error)")
            }

        }
        
        signIn(username: username, password: password)
                    
        }
    public func createPerson(firstName: String, lastName: String, clientId: String) {
        var localDeviceInfo = DeviceConfigLoader.load()
        
        let person = Person(firstName: firstName,
                            lastName: lastName,
                            clientId: clientId)
        
        Amplify.DataStore.save(person) { result in
            switch result {
            case .success(let createdPerson):
                print("Amplify: Person created successfully!")
                localDeviceInfo.personId = createdPerson.id
                print("ID: \(createdPerson.id)")
                DeviceConfigLoader.write(information: localDeviceInfo)
            case .failure(let error):
                print("Amplify: failed to create graphQL \(error)")
            }
        }
        
        
//        Amplify.API.mutate(request: .create(person)) {
//            event in
//            switch event {
//            case .success(let result):
//                switch result {
//                case .success(let person):
//                    print("Amplify: successfully added person: \(person)")
//                    localDeviceInfo.personId = person.id
//                    DeviceConfigLoader.write(information: localDeviceInfo)
//                case .failure(let graphQLError):
//                    print("Amplify: failed to create graphQL \(graphQLError)")
//                }
//            case .failure(let apiError):
//                print("Failed to create a person", apiError)
//            }
//        }
    }
    public func getPersonById(id: String) -> Person {
        // this Hello12345 fixes a nasty edge case whereby an empty record would be created
        // If the person is invalid (marked by Hello12345) then no update will occur
        var person = Person(firstName: "Hello12345", lastName: "World", clientId: "4")
        
        Amplify.DataStore.query(Person.self, byId: id) {
            switch $0 {
            case .success(let result):
                // check to ensure that the result is an actual person
                if (result != nil) {
                    person = result!
                } else {
                    print("ERROR: person is nil")
                }
            case .failure(let error):
                print("Error on query \(error)")
            }
        }
        
        return person
    }
    public func updateMyClientId(clientId: String) {
        var localDeviceInfo =  DeviceConfigLoader.load()
        
        var person = getPersonById(id: localDeviceInfo.personId)
        
        if person.firstName == "Hello12345" {
            print("NOT WORKING")
            return
        }
        
        person.clientId = clientId

        Amplify.DataStore.save(person){
            switch $0 {
            case .success:
                print("Updated existing person")
            case .failure(let error):
                print("Error updating person \(error)")
            }
        }
        
        // write new client id into persistence
        localDeviceInfo.clientId = clientId
        DeviceConfigLoader.write(information: localDeviceInfo)
    }
    public func listPeople() -> [Person] {
        Amplify.DataStore.query(Person.self) {
            result in
            switch result {
            case .success(let people_list):
                self.people = people_list
            case .failure(let error):
                print("Amplify: Failure on listPeople, error: \(error)")
            }
        }

        return self.people
    }
    func APIGetToken(deviceToken: String, path: String="/api", completion: @escaping (String) -> Void) {
        print("Amplify API: Fetching media token from server application")
        print("Received device token: \(deviceToken)")
        let message = #"{"action":"register","notifyType":"APNS","deviceToken":"\#(deviceToken)"}"#
        print("message \(message)")
        let request = RESTRequest(path: path, body: message.data(using: .utf8))
        
        Amplify.API.post(request: request) { result in
            switch result {
            case .success(let data):
                do {
                    print("SUCCESS")
                    let decodedData = try JSONDecoder().decode(RegisterResponseStruct.self, from: data)
                    DispatchQueue.main.async {
                        completion(decodedData.id)
                    }
                } catch(let error) {
                    print("Decoding error \(error)")
                }
                
            case .failure(let error):
                print("FAILED 2")
                print("APIGetTokenError: \(error)")
            }
        }.resume()
    }
    func APIInitiateCall(callerId: String, calleeId: String, path: String="/api", completion: @escaping (String) -> Void) {
        print("Amplify API: Initiating call")
        
        let message = #"{"action":"initiateCall","calleeId": "\#(calleeId)", "callerId": "\#(callerId)"}"#
        let request = RESTRequest(path: path, body: message.data(using: .utf8))

        
        Amplify.API.post(request: request) { result in
            switch result {
            case .success(let data):
                do {
                    let decodedData = try JSONDecoder().decode(InitiateCallResponseStruct.self, from: data)
                    DispatchQueue.main.async {
                        completion(decodedData.token)
                    }

                } catch(let error) {
                    print("Decoding error: \(error)")
                }
            case .failure(let error):
                print("APIInitiateCallError: \(error)")
            }
        }.resume()
        
    }
    
    func APIEndCall(clientId: String, path: String="/api", completion: @escaping (String) -> Void) {
        print("Amplify API: Ending Call")
        
        let message = #"{"action":"endCall","clientId": "\#(clientId)"}"#
        let request = RESTRequest(path: path, body: message.data(using: .utf8))

        Amplify.API.post(request: request) { result in
            switch result {
            case .success(let data):
                do {
                    let decodedData = try JSONDecoder().decode(EndCallResponseStruct.self, from: data)
                    DispatchQueue.main.async {
                        completion(decodedData.message)
                    }

                } catch(let error) {
                    print("Decoding error: \(error)")
                }
            case .failure(let error):
                print("APIEndCallError: \(error)")
            }
        }

    }
    public func clear() {
        Amplify.DataStore.clear { result in
            switch result {
            case .success:
                print("DataStore cleared")
            case .failure(let error):
                print("Error clearing DataStore: \(error)")
            }
        }
    }
}
