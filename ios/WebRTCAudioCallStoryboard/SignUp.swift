//
//  SignUp.swift
//  WebRTCAudioCallStoryboard
//
//  Created by Andrew Browne on 1/25/22.
//

import UIKit

// Sign Up Page View

class SignUpView: UIView {

    @IBOutlet weak var usernameField: UITextField!
    @IBOutlet weak var passwordField: UITextField!
    @IBOutlet weak var firstNameField: UITextField!
    @IBOutlet weak var lastNameField: UITextField!
    @IBOutlet weak var emailField: UITextField!
    
    @IBAction func signUpButton(_ sender: Any) {
        
        let username = usernameField.text ?? ""
        let password = passwordField.text ?? ""
        let firstName = firstNameField.text ?? ""
        let lastName = lastNameField.text ?? ""
        let email = emailField.text ?? ""
        
        amplifyCaller.signUp(
            username: username,
            password: password,
            email: email
        )
        
        var localDeviceInfo = DeviceConfigLoader.load()
        
        amplifyCaller.APIGetToken(deviceToken: localDeviceInfo.deviceToken) { token in
            
            amplifyCaller.createPerson(
                firstName: firstName,
                lastName: lastName,
                clientId: token
            ) 

            localDeviceInfo.firstName = firstName
            localDeviceInfo.lastName = lastName
            localDeviceInfo.clientId = token
            localDeviceInfo.notifyType = "APNS"
            
            DeviceConfigLoader.write(information: localDeviceInfo)
        }
        
        amplifyCaller.signIn(username: username, password: password)
        
    }
}
