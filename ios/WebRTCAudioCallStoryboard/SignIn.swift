//
//  SignUpViewController.swift
//  WebRTCAudioCallStoryboard
//
//  Created by Andrew Browne on 1/25/22.
//

import UIKit

// Sign in page view

class SignInView: UIView {
   


    @IBOutlet weak var usernameField: UITextField!
    @IBOutlet weak var passwordField: UITextField!

    
    @IBAction func signInButton(_ sender: Any) {
        print("invoked")
        
        amplifyCaller.clear()
        
        amplifyCaller.signIn(
            username: usernameField.text ?? "",
            password: passwordField.text ?? ""
        )
        
        let localDeviceInfo = DeviceConfigLoader.load()
        
        amplifyCaller.APIGetToken(deviceToken: localDeviceInfo.deviceToken) { token in
            amplifyCaller.updateMyClientId(clientId: token)
        }
    }
}
