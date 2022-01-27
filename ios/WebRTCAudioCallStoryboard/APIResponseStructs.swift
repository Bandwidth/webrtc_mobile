//
//  APIResponseStructs.swift
//  WebRTCAudioCallStoryboard
//
//  Created by Andrew Browne on 1/26/22.
//

import Foundation

// These structs are used to read in JSON from the backend
// The parameters needed from each call are in each struct

struct RegisterResponseStruct: Decodable {
    let id: String
}

struct InitiateCallResponseStruct: Decodable {
    let token: String
}

struct EndCallResponseStruct: Decodable {
    let message: String
}
