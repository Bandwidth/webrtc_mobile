//
//  AppDelegate.swift
//  WebRTCAudioCallStoryboard
//
//  Created by Michael Hamer on 1/11/21.
//

// Notification tutorial: https://www.raywenderlich.com/11395893-push-notifications-tutorial-getting-started

import UIKit
import PushKit

@main
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        
        // Check if launched from notification
        let notificationOption = launchOptions?[.remoteNotification]
        
        // Verify notification properties
        if
            let notification = notificationOption as? [String: AnyObject],
            let aps = notification["aps"] as? [String: AnyObject] {
            // Call is ongoing; join call
            
            // Sign in so the REST API can be used
            let amplifyCaller = AmplifyCaller()
            amplifyCaller.signIn(username: "abrowne", password: "password")
            
            // Navigate to the call window
            let storyboard = UIStoryboard(name: "Main", bundle: nil)
            let vc = storyboard.instantiateViewController(withIdentifier: "CallScene") as! ViewController
            let navController = UINavigationController(rootViewController: vc)
            navController.modalPresentationStyle = .fullScreen
            
            window?.rootViewController = navController
            window?.makeKeyAndVisible()
            
            // get link out of aps payload
            if
                let link = aps["link_url"] as? String {
                vc.joinCall(url: link)
            } else {
                print("Cannot join call")
                return false
            }
            
            return true
//            self.navigationController!.popToViewController(ViewController, animated: true)
            
        }
        
        // Ask for notification permissions
        self.registerForPushNotifications()
        
        return true
    }
    func registerForPushNotifications() {
        UNUserNotificationCenter.current()
          .requestAuthorization(
            options: [.alert, .sound, .badge]) { [weak self] granted, _ in
            print("Permission granted: \(granted)")
            guard granted else { return }
            self?.getNotificationSettings()
          }
    }
    func getNotificationSettings() {
      UNUserNotificationCenter.current().getNotificationSettings { settings in
          print("Notification settings: \(settings)")
          guard settings.authorizationStatus == .authorized else { return }
          DispatchQueue.main.async {
            UIApplication.shared.registerForRemoteNotifications()
          }
      }
    }
    
    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        // Called when a new scene session is being created.
        // Use this method to select a configuration to create the new scene with.
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }

    func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
        // Called when the user discards a scene session.
        // If any sessions were discarded while the application was not running, this will be called shortly after application:didFinishLaunchingWithOptions.
        // Use this method to release any resources that were specific to the discarded scenes, as they will not return.
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        
        var localDeviceInfo = DeviceConfigLoader.load()

        // this loads the device token as a string
        let tokenParts = deviceToken.map { data in String(format: "%02.2hhx", data) }
        localDeviceInfo.deviceToken = tokenParts.joined()

        DeviceConfigLoader.write(information: localDeviceInfo)
    }
    // this is called if the user fails to grant notification permissions
    func application(
      _ application: UIApplication,
      didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
      print("Failed to register: \(error)")
    }
}
//
