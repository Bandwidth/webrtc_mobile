//
//  ViewController.swift
//  WebRTCAudioCallStoryboard
//
//  Created by Michael Hamer on 1/11/21.
//

// This class defines the call view controller
// It handles all rendering of media streams,
// And invokes the backend endpoints through calling amplifyCaller methods

import UIKit
import BandwidthWebRTC
import WebRTC

class ViewController: UIViewController {
    @IBOutlet weak var statusLabel: UILabel!
    @IBOutlet weak var endCallButton: UIButton!
    @IBOutlet weak var muteBarButtonItem: UIBarButtonItem!
    @IBOutlet weak var connectBarButtonItem: UIBarButtonItem!
    @IBOutlet weak var infoLabel: UILabel!
    
    private var mute = false
    private var isConnected = false
    
    private var bandwidth = RTCBandwidth()
    private var audioTracks = [RTCAudioTrack]()
    
    public var callee = Person(id: "",
                               firstName: "",
                               lastName: "",
                               clientId: "")
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        bandwidth.delegate = self
        infoLabel.text = "\(callee.firstName) \(callee.lastName)"
    }
    

    @IBAction func connect(_ sender: Any) {
        if isConnected {
            bandwidth.disconnect()
            
            statusLabel.text = "Offline"
            
//            callButton.isEnabled = false
            endCallButton.isEnabled = false
            
            muteBarButtonItem.isEnabled = false
            
            connectBarButtonItem.title = "Connect"
            isConnected = false
            
            endCall {
                DispatchQueue.main.async {
                    self.statusLabel.text = "Online, no call"
    //                self.callButton.isEnabled = true
                    self.endCallButton.isEnabled = false
                }
            }
            
        } else {
            // Grab the Bandwidth provided token from your hosted server application.
            // retrieve caller and callee ids
            let localDeviceInfo = DeviceConfigLoader.load()
            let callerId = localDeviceInfo.clientId
            let calleeId = callee.clientId
            
            amplifyCaller.APIInitiateCall(callerId: callerId, calleeId: calleeId) { token in
                self.connectToCall(token: token)
//                self.bandwidth.connect(using: token) { stream in
//                    DispatchQueue.main.async {
//                        self.connectBarButtonItem.title = "Disconnect"
//
//                        self.isConnected = true
//                    }
//
//                    print("Connected to the backend server")
//
//                    self.bandwidth.publish(alias: nil) { stream in
//                        DispatchQueue.main.async {
//                            self.audioTracks = stream.mediaStream.audioTracks
//
//                            // Enable the mute buton once we've started publishing
//                            self.muteBarButtonItem.isEnabled = true
////                            self.callButton.isEnabled = true
//                        }
//                    }
//                }
            }
        }
    }
    
    func endCall(completion: @escaping (String) -> Void) {
        print("Ending call through server application")
        
        let localDeviceInfo = DeviceConfigLoader.load()
        let callerId = localDeviceInfo.clientId
        
        amplifyCaller.APIEndCall(clientId: callerId) { message in
            
            DispatchQueue.main.async {
                print(message)
                completion(message)
            }
        }
    }

    @IBAction func endCall(_ sender: Any) {
        endCall {
            DispatchQueue.main.async {
                self.statusLabel.text = "Online, no call"
//                self.callButton.isEnabled = true
                self.endCallButton.isEnabled = false
            }
        }
    }
    
    @IBAction func mute(_ sender: Any) {
        // Toggle the local mute state.
        mute.toggle()
        
        // Toggle the title of the mute button to display to the user.
        muteBarButtonItem.title = mute ? "Unmute" : "Mute"
        
        // Set the audio of our local WebRTC connection.
        audioTracks.forEach { $0.isEnabled = !mute }
    }
    public func connectToCall(token: String) {
        self.bandwidth.connect(using: token) { stream in
            DispatchQueue.main.async {
                self.connectBarButtonItem.title = "Disconnect"
                self.statusLabel.text = "Online"
                self.isConnected = true
                self.endCallButton.isEnabled = true
            }
            
            print("Connected to the backend server")
            
            self.bandwidth.publish(alias: nil) { stream in
                DispatchQueue.main.async {
                    self.audioTracks = stream.mediaStream.audioTracks
                    
                    // Enable the mute buton once we've started publishing
                    self.muteBarButtonItem.isEnabled = true
                }
            }
        }
    }
    public func joinCall(url: String) {
        
        // Get token from provided url
        let token = getTokenFromUrl(url: url)!
        print("TOKEN IS \(token)")
        
        
        // Connect to call with provided token
        connectToCall(token: token)
    }
    public func getTokenFromUrl(url: String, param: String="tok") -> String? {
        guard let url = URLComponents(string: url) else { return nil }
        return url.queryItems?.first(where: { $0.name == "tok"})?.value
    }
}




extension ViewController: RTCBandwidthDelegate {
    func bandwidth(_ bandwidth: RTCBandwidth, streamAvailable stream: RTCStream) {
        
    }
    
    func bandwidth(_ bandwidth: RTCBandwidth, streamUnavailable stream: RTCStream) {
        
    }
}

