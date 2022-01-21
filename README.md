# Documentation for our Backend 


## Installation & Building
<ol>
<li>
Install Amplify CLI

```console
curl -sL https://aws-amplify.github.io/amplify-cli/install | bash && $SHELL
```
</li>
<li>
Create an Amplify project in Amplify Studio and pull a local version

```console
amplify pull --appId {appId} --envName staging
```

</li>
<li>
Replace the local Amplify project source with the source from this repo
</li>
<li>

Update the Amplify project configuration with the following environment variables ([click here](https://aws.amazon.com/blogs/mobile/configure-environment-variables-and-secrets-for-your-lambda-functions-with-amplify-cli/)):

```console
amplify function update
```

<ul>
    <li>PINPOINT_APP_ID</li>
    <li>GRAPH_QL_KEY</li>
    <li>GRAPH_QL_API_URL</li>
    <li>BW_ACCOUNT_ID</li>
    <li>BW_USERNAME</li>
    <li>BW_PASSWORD</li>
</ul>
    
</li>
<li>

Update Pinpoint Application with [FCM Server Key](https://firebase.google.com/docs/cloud-messaging/server). This requires creating an account and application with FCM. Configure the [Firebase Channel](https://docs.aws.amazon.com/pinpoint/latest/userguide/channels-push-manage.html) of Pinpoint with the server key you have acquired in the AWS Console. TODO: Similar steps will be performed to upload a `.p12` certificate to enable iOS notifications when an iOS version is available.

</li>
</ol>

## Resources Used

The backend is hosted on AWS with the following resources:

| Resource | Description |
| --- | ----------- |
| Amplify | Wrapper for entire project, acts like git to host all resources and code |
| Pinpoint | Notification service for push notifications. This service itself invokes Firebase Cloud Management (FCM) for Android and APNS for iOS devices |
| Lambda | Two Lambdas are used in this project. One invokes Pinpoint to send push notifications and the other acts as a REST endpoint to manage calls, register users, and invoke the Pinpoint Lambda. Both are written in Node. |
| AppSync | Wrapped by Amplify; AppSync manages the app state and synchronizes said state with the frontends. Data is held in a DynamoDB table and transferred through generated GraphQL wrappers. |
| Cognito | Cognito handles user registration. Upon first entering the app, users can create an account. Cognito requires a username, password, and email |
| DynamoDB | Never actually directly accessed - wrapped by AppSync and Amplify. Two entry tables are hosted here, 1) Person table detailing each user and 2) DeviceInfo detailing sensitive device tokens and webRTC session/participation information. A full implementation would likely decouple the DeviceInfo table entirely from the Amplify project, as not doing so provides all of this information to the front end |
## Application Flow 

<ol>
    <li>User is prompted to allow push notifications. Upon accepting, the device's OS will return a unique device token, which is needed by Pinpoint to push notify the device.</li>
    <li>User must create an account to use the service. Upon opening the app, the option is presented to sign up. Signing up requires a first name, last name, username, password, email. After providing this information, 
    Cognito creates a new user in the user pool. The endpoint is subsequently hit with the newly created user's first name, last name, device token, and device type. The endpoint provides to the user their client id. 
    A first name, last name, and client id are then used to create a person entry in the database.
    </li>
    <li>Upon signing in, the list of all available</li>
</ol>

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