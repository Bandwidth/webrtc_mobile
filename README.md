# Documentation for our Backend 

## Resources Used

The backend is hosted on AWS with the following resources:

| Resource | Description |
| --- | ----------- |
| Amplify | Wrapper for entire project, acts like git to host all resources and code |
| Pinpoint | Notification service for push notifications. This service itself invokes Firebase Cloud Management (FCM) for Android and APNS for iOS devices |
| Lambda | Two Lambdas are used in this project. One invokes Pinpoint to send push notifications and the other acts as a REST endpoint to manage calls, register users, and invoke the Pinpoint Lambda |
| AppSync | Wrapped by Amplify; AppSync manages the app state and synchronizes said state with the frontends. Data is held in a DynamoDB table and transferred through generated GraphQL wrappers. |
| Cognito | Cognito handles user registration. Upon first entering the app, users can create an account. Cognito requires a username, password, and email |
| 
## Application Flow 

<ol>
    <li>User must create an account to use the service.</li>
</ol>

## Reccomended Reading
<ul>
    <li></li>
</ul>