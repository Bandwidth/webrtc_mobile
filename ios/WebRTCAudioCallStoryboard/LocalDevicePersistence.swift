//
//  LocalDevicePersistence.swift
//  WebRTCAudioCallStoryboard
//
//  Created by Andrew Browne on 1/26/22.
//

import Foundation

// This file handles persisting your client ID, name, deviceToken, and person entry id, for the purposes of identifying yourself
// across runs as well as knowing how to update the backend's database
// While this is not yet handled, a useful employment of this could be to not render yourself in the
// List of users

struct LocalDeviceInformation : Codable {
    var firstName: String
    var lastName: String
    var clientId: String
    var deviceToken: String
    var notifyType: String
    var personId: String
}

class DeviceConfigLoader {
    static private var plistURL: URL {
        let documents = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
        return documents.appendingPathComponent("localDeviceInformation.plist")
    }
    
    static func load() -> LocalDeviceInformation {
        let decoder = PropertyListDecoder()
        
        guard let data = try? Data.init(contentsOf: plistURL),
              let information = try? decoder.decode(LocalDeviceInformation.self, from: data)
        else { return LocalDeviceInformation(firstName: "Test", lastName: "Test", clientId: "Test", deviceToken: "Test", notifyType: "APNS", personId: "Test")}
        
        return information
    }
    static func copyPreferencesFromBundle() {
        if let path = Bundle.main.path(forResource: "localDeviceInformation", ofType: "plist"),
           let data = FileManager.default.contents(atPath: path),
           FileManager.default.fileExists(atPath: plistURL.path) == false {
            FileManager.default.createFile(atPath: plistURL.path, contents: data, attributes: nil)
        }
    }
    static func write(information: LocalDeviceInformation) {
        let encoder = PropertyListEncoder()
        
        if let data = try? encoder.encode(information) {
            if FileManager.default.fileExists(atPath: plistURL.path) {
                try? data.write(to: plistURL)
            } else {
                FileManager.default.createFile(atPath: plistURL.path, contents: data, attributes: nil)
            }
        }
    }
}
