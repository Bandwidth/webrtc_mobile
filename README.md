# Native iOS/Android calling via Bandwidth WebRTC

> **NOTICE: SAMPLE CODE** This project is a rapid prototype it is meant as a sample app not production code!

- [x] AWS Amplify project setup instructions
- [x] AWS Lambda sample back-end code
- [x] Android sample native app
- [ ] AWS CDK deployment scripts
- [ ] iOS sample native app

## Contents:
- [Android: Setup Firebase Project](https://firebase.google.com/docs/cloud-messaging/android/first-message?authuser=0#create_a_firebase_project)
- [Android: WebRTC Sample App README](https://github.com/Bandwidth/webrtc_mobile/tree/main/android#readme)
- iOS README -- TODO
- [Backend: AWS Amplify Install & Configure](https://github.com/Bandwidth/webrtc_mobile#backend--installation--building)

## Sample Message Flow

![Sample Message Flow](/WebRTC-Mobile-App-Ladder-Diagram.png)

## Backend : Installation & Building

> The included CDK project to deploy the backend does not currently work due to some CDK vs Amplify versioning issues which should be resolved in a few weeks.  We appologize for the inconvenience - we have documented the complete sequence to create and deploy a working Amplify backend using the AWS Console and AWS CLI below.

### Login to the AWS console:
   aws.amazon.com

### Go to Amplify:
   https://console.aws.amazon.com/amplify/home

### Amplify -> New App -> Build an App
  - Enter name for the app (eg: bturner-webrtc-mobile)
  - Wait for Amplify console to complete deployment
  - Click "Launch Studio"
  - Wait for Amplify Studio to launch
  - In upper-right, click "Local setup instructions"
  - Small window displays a CLI command like this:
       ```console
       (EXAMPLE) amplify pull --appId <<APP_ID>> --envName staging
       ```
  - Copy the CLI command

### Install AWS CLI:
   https://aws.amazon.com/cli/

### Open new Terminal window

  - You will need your AWS credentials (API Key and Token) from an [AWS IAM](https://console.aws.amazon.com/iamv2/home#/users) user
  - **Configure AWS credentials:**
    ```console
    aws configure
    ```

  - **Install AWS Amplify CLI:**
    ```console
    curl -sL https://aws-amplify.github.io/amplify-cli/install | bash && $SHELL
    ```
    - NOTE: The command above is different when installing on Windows

  - **Create a new directory to hold your Amplify project**
    ```console
    mkdir my-amplify-project
    cd my-amplify-project
    ```
  - **Pull the Amplify project (copied CLI command from earlier)**
    ```console
    amplify pull --appId <<APP_ID>> --envName staging
    ```
    - Browser will open and request access for Amplify CLI
      - NOTE: You MUST already have launched Amplify Studio before this step!
    - choose 'javascript' for 'type of app'
    - choose 'none' for 'javascript framework'
    - choose defaults for the directories
    - choose defaults for build/start commands
    - choose YES for 'modify this backend'

  - **Add Authentication**
    ```console
    amplify auth add
    ```
    - Default configuration
    - Username
    - No additional capabilities

  - **Add Analytics**
    ```console
    amplify analytics add
    ```
    - Amazon Pinpoint
    - Select default options

  - **Add GraphQL API**
    ```console
    amplify api add
    ```
    - GraphQL
    - Blank Schema
    - Edit Schema: YES
    - Replace defaults in editor with schema below, then save:

```
type Person @model @auth(rules: [{ allow: public }]) {
  id: ID!
  firstName: String!
  lastName: String!
  clientId: String!
}

type DeviceInfo @model @auth(rules: [{ allow: public }]) {
  id: ID!
  notifyType: String!
  deviceToken: String!

  sessionId: String
  participantId: String
  participantToken: String
}
```

  - **Add Function**
    ```console
    amplify function add
    ```
    - choose "Lambda function"
    - choose "NodeJS"
    - choose "Hello World"
    - choose YES to "advanced settings"
      - choose "Yes" to "other resources"
      - Add API, Analytics, Storage
         - Allow ALL operations for all resources
      - No recurring invocations
      - No Lambda layers
      - YES environment variables:
      ```
          PINPOINT_APP_ID -- Use placeholder value
          GRAPH_QL_KEY -- Use placeholder value
          GRAPH_QL_API_URL  -- Use placeholder values
          BW_ACCOUNT_ID -- Enter your Bandwidth account ID
          BW_USERNAME -- Enter your Bandwidth username
          BW_PASSWORD -- Enter your Bandwidth password
      ```
      - No secret values
      - Yes edit lambda function
        - Replace hello-world Lambda function with code from the following file, then save:

         [Lambda Function: index.js](https://github.com/Bandwidth/webrtc_mobile/blob/main/backend/index.js)
         
      - Now change directory into the Lambda's src (location of "index.js")
         - eg: my-amplify-project/amplify/backend/function/<<LAMBDA_NAME>>/src
      - Edit the "package.json" file located here
         - eg: my-amplify-project/amplify/backend/function/<<LAMBDA_NAME>>/src/package.json
         - Add a line, then save:
         ```console
         {
         ...
            "type":"module",
         ...
         }
         ```
      - Install additional NPM packages:
         ```console
         npm i @bandwidth/webrtc aws-sdk axios graphql-tag graphql uuid
         ```
      
  - **Add REST API**
    ```console
    amplify api add
    ```
    - REST
    - Accept the default label
    - Path:  ``` /api ```
    - Use existing lambda function
    - No restrictions
    - No more APIs

  - **Push project**
    ```console
    amplify push
    ```
    - select defaults to generate code, compile schemas, etc.
    - wait for project to finish deploying
    - Copy the output values:
    ```
       GraphQL endpoint: (EXAMPLE) https://7bEXAMPLEfyu.appsync-api.us-east-1.amazonaws.com/graphql
       GraphQL API KEY: (EXAMPLE) da2-egolfEXAMPLEdv37wjtm
       REST API endpoint: (EXAMPLE) https://jmEXAMPLE4i.execute-api.us-east-1.amazonaws.com/staging
    ```

  - **Configure Amazon Pinpoint**
    https://console.aws.amazon.com/pinpoint/home
    - Choose the newly made Pinpoint project
    - Copy the ProjectID
      eg: ``` (EXAMPLE) 4d8065387dexample9f1d680b399 ```
    - Settings -> Push Notifications -> Edit
    - ANDROID:
      - Add [FCM Server Key](https://firebase.google.com/docs/cloud-messaging/server)
         - This requires creating an account and application with FCM.
         - Configure the [Firebase Channel](https://docs.aws.amazon.com/pinpoint/latest/userguide/channels-push-manage.html) of Pinpoint with the server key you have acquired in the AWS Console.
    - iOS:
      - **TODO:** Similar steps will be performed to upload a `.p12` certificate to enable iOS notifications when an iOS version is available.


   - **Update GraphQL with AutoMerge**
      ```console
      amplify api update
      ```
      - GraphQL
      - Enable conflict detection
      - Auto Merge
      - YES override default per-model
      - Select ALL models
      - Select Auto Merge for each model

  - **Update Environment Variables for the Lambda** ([docs here](https://aws.amazon.com/blogs/mobile/configure-environment-variables-and-secrets-for-your-lambda-functions-with-amplify-cli/))
      ```console
      amplify function update
          PINPOINT_APP_ID   -- from Pinpoint ProjectID
          GRAPH_QL_KEY      -- from Amplify push output
          GRAPH_QL_API_URL  -- from Amplify push output
      ```

  - **Push final project**
    ```console
    amplify push
    ```
    - Back end should now be up & configured to run!


### Testing

- Using curl or Postman, etc. you can invoke the Lambda API to see if it is running correctly
- POST to the REST API endpoint returned by ``` amplify status ```
- NOTE: be sure to add ``` /api ``` to the end of the REST endpoint

   ``` (EXAMPLE) https://jmEXAMPLE4i.execute-api.us-east-1.amazonaws.com/staging/api ```
- **Register a new user (POST):**
    ```
    {
        "action":"register",
        "notifyType": "GCM",
        "deviceToken": "....",
    }
    ```
    - Returns a clientId:
    ```
    {
        "id": "c1aae098-7da1-4361-812f-91eea97a1f17",
        ...
    }
    ```
    
- **Delete clients (POST):**
    ```
    {
        "action":"deleteClientIds",
        "clientIds":["c1aae098-7da1-4361-812f-91eea97a1f17"]
    }
    ```
    - Returns:
    ```
    {
      "deletedIds": [{"id": "c1aae098-7da1-4361-812f-91eea97a1f17"}]
    }
    ```

## Resources Used

The backend is hosted on AWS with the following resources:

| Resource | Description                                                                                                                                                                                                                                                                                                                                                                                                      |
| -------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Amplify  | Wrapper for entire project, acts like git to host all resources and code                                                                                                                                                                                                                                                                                                                                         |
| Pinpoint | Notification service for push notifications. This service itself invokes Firebase Cloud Management (FCM) for Android and APNS for iOS devices                                                                                                                                                                                                                                                                    |
| Lambda   | Two Lambdas are used in this project. One invokes Pinpoint to send push notifications and the other acts as a REST endpoint to manage calls, register users, and invoke the Pinpoint Lambda. Both are written in Node.                                                                                                                                                                                           |
| AppSync  | Wrapped by Amplify; AppSync manages the app state and synchronizes said state with the frontends. Data is held in a DynamoDB table and transferred through generated GraphQL wrappers.                                                                                                                                                                                                                           |
| Cognito  | Cognito handles user registration. Upon first entering the app, users can create an account. Cognito requires a username, password, and email                                                                                                                                                                                                                                                                    |
| DynamoDB | Never actually directly accessed - wrapped by AppSync and Amplify. Two entry tables are hosted here, 1) Person table detailing each user and 2) DeviceInfo detailing sensitive device tokens and webRTC session/participation information. A full implementation would likely decouple the DeviceInfo table entirely from the Amplify project, as not doing so provides all of this information to the front end |

## Front-End / Back-End Flow

Rough API call flow between front end and back end

### Native iOS/Android app launches

- Request user's first & last name
- Load Device Token for push notifications from native OS
- Call API with:
  ```
  {
      "action":"register",
      "notifyType": "GCM",
      "deviceToken": "....",
  }
  ```

- Receive clientId in return:
  ```
  {
      "id": "d44bece7-4eee-4587-8682-a50a974bacde",
      ....
  }
  ```

- Insert or Update Person record in GraphQL database
  ```
  type Person @model @auth(rules: [{ allow: public }]) {
      id: ID!
      firstName: String!
      lastName: String!
      clientId: String!
  }
  ```

- Query for list of People from GraphQL
- Display list for user to choose from

### User chooses a Person to call:

- When the user clicks on one of the People, use self-clientId and target-clientId to invoke "initiateCall":
  ```
  {
      "action":"initiateCall",
      "calleeId":"... clientId of callee ...",
      "callerId":"... clientId of caller ..."
  }
  ```
  
- Receive "participantToken" in reply:
  ```
  {
    "token": "eyJhbGcEXAMPLEiIsInR5cCI6IkpXVCJ9.eyJhIEXAMPLEiOiIxODY1ZTVkYy1mY2I5LTRhOTUtOTA1Ny1jNjNmEXAMPLEEXAMPLEEXAMPENDI4MzEwMDksImlzcyI6InByZDowMCJ9.hl4VFBB-apesVgaEPEXAMPLENjAmwt9J9Zok"
  }
  ```
  
- Use token to join WebRTC session

### When the call ends, each user hangs up:

- Each user cleans up their own call leg:
  ```
  {
      "action":"endCall",
      "clientId":"... clientId of self ..."
  }
  ```
  
- User can now make or receive another call

### Bulk cleanup option:

- Delete a list of clientIds, closing down WebRTC sessions, participants, etc.
- Does not delete Person records
  ```
  {
      "action":"deleteClientIds",
      "clientIds":["..clientId A..", "..clientId B..", "..clientId C.."]
  }
  ```

## Recommended Reading

<ul>
    <li>The Code in this project is based on several example projects:
        <ul>
            <li>https://docs.aws.amazon.com/pinpoint/latest/developerguide/send-messages-push.html</li>
            <li>https://docs.amplify.aws/lib/graphqlapi/graphql-from-nodejs/q/platform/js/</li>
            <li>https://github.com/Bandwidth-Samples/webrtc-hello-world-js</li>
        </ul>
    </li>
</ul>

