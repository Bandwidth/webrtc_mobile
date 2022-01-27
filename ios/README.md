# webrtc-ios-app
 
## Some Notes
- Push notifications must be enabled, and this requires an Apple Developer Account. There are a number of steps involved for this process, but these include 1) creating a certificate on the Apple developer website, 2) adding this certificate to your Pinpoint application, 3) adding these credentials to the XCode project, and 4) adding the push notifications capability to the app inside of XCode. [This](https://www.raywenderlich.com/11395893-push-notifications-tutorial-getting-started) is a great place to see how that is done
## Files in Application 
| Filename | Purpose
| -------- |  -------- 
| Main | This is the main storyboard defining all view and navigation controllers for the application
| AmplifyCaller | Handles all amplify calling (auth, REST API backend, creating/updating GraphQL database, etc)
| APIResponseStructs | Defines codable structs which allow swift to use the JSON responses from the backend (one of the weird quirks of swift is that you must manually define these structs)
| LocalDevicePersistence | Defines a static class which allows one to persist the information used to identify oneself across runs (client id, name, device token, etc)
| AppDelegate | Defines behavior upon launching the application or various other actions. Most relevant for push notifications
| SceneDelegate | Mostly boilerplate which handles the scenes in the app
| ViewController | handles the call window. This also calls the bandwidth object in order to connect to the session, using tokens obtained through AmplifyCaller from the backend
| PersonTableViewController | gets the list of people from AmplifyCaller and displays them in a clickable table view for the purpose of calling
| SignIn | Defines the sign in window actions
| SignUp | Defines the sign up behavior (both sign in and sign up call the AmplifyCaller methods to create/authenticate users and their database records)


## Basic flow

- Create a new user or sign up
- Upon signing in, the list of users in the database is displayed. Simply click on one of them to begin initiating a call
- To send an outbound call to the selected user, click "Connect". After accepting microphone permissions, you are in a call with that user
- To receive incoming calls from outside of the app, you must have a developer account (not yet tested)
  

## Yet to be implemented
- Subscription service for list of users (list is now statically generated, but could subscribe to updates)
- Error logging inside of the app (it is recommended to use a phone connected to XCode in order to see any errors)
- Multi-party calling
- Not having to sign in upon every launch
- Not showing yourself on the list users screen (this could be implemented using the local device persistence information)
- Better calling UI
- Incoming call flow could perhaps be integrated with CallKit for native dialing functionality and a more natural notification
- Video calling as opposed to solely audio
- Disconnect functionality improved
