/* Amplify Params - DO NOT EDIT
	ENV
	REGION
	PINPOINT_APP_ID
Amplify Params - DO NOT EDIT */

// Based on https://docs.aws.amazon.com/pinpoint/latest/developerguide/send-messages-push.html


// GraphQL endpoint: https://fuga4zxjtjhr5nrv7trxqct2zi.appsync-api.us-east-1.amazonaws.com/graphql
// GraphQL API KEY: da2-pjxkbv3xk5a6vmwbpvzrc2xznu

'use strict'; 
// const AWS = require('aws-sdk');
// const axios = require('axios');
// const gql = require('graphql-tag');
// const graphql = require('graphql');
// const BandwidthWebRTC = require("@bandwidth/webrtc");
// const v1 = require('uuid');

import axios from "axios"
import gql from 'graphql-tag';
import graphql from 'graphql';
const { print } = graphql;

import aws from 'aws-sdk'
const { AWS } = aws;

import BandwidthWebRTC from "@bandwidth/webrtc"
import uuid from 'uuid';
const { v1: uuidv1 } = uuid;

const region = process.env.AWS_REGION;

// This is your Pinpoint Application ID
// Make sure the push channel is enabled for the project you choose
const pinpoint_app_id = process.env.PINPOINT_APP_ID;

const wrapResult = (body) => {
  return {
      statusCode: 200,
      body: JSON.stringify(body),
      headers: {
          "Access-Control-Allow-Origin": "*",
      }
  }
};

const randomMaxInt = () => Math.floor(Math.random() * Number.MAX_SAFE_INTEGER);


const getDeviceInfo = gql`
  query getDeviceInfo {
    getDeviceInfo {
      items {
        clientId
        deviceType
        deviceId
        deviceToken
        participantId
        participantToken
      }
    }
  }
`


export const handler = async (event, context) => {
  try {
    console.log("BEGIN");
    console.log("EVENT :: ", JSON.stringify(event));


    // Attempt to read from the GraphQL database
    try {
      const graphqlData = await axios({
        url: process.env.GRAPH_QL_API_URL,
        method: 'post',
        headers: {
            'x-api-key': process.env.GRAPH_QL_KEY
        },
        data: {
            query: print(getDeviceInfo),
        }
      });
      const body = {
          graphqlData: graphqlData.data.getDeviceInfo
      }
      return wrapResult(body);
    } catch (err) {
        console.log('error posting to appsync: ', err);
        return wrapResult(err);
    } 
  } catch (err) {
    console.log("ERROR :: ", err);
    return wrapResult(err);
  }
};

//     var pinpoint = new AWS.Pinpoint();
    
//     var title = 'Incoming Call';
//     var message = `${event.firstName} ${event.lastName} is calling you`;
    
//     // This object contains the unique device token for the person you want to send
//     // the message to, and the push service you want to send the message to
//     // Options for 'service':
//     //     use 'GCM' if the recipient is an Android device
//     //     use 'APNS' if the recipient is an iOS device
//     var recipient = {
//         'token': event['deviceToken'],
//         'service': event['service']
//     };
    
//     // this will open the app to a specific page or interface - the incoming call
//     var action = 'DEEP_LINK';
    
//     // Tells the application which call to join
//     var url = event['url'];
    
//     // Priority of the push notification
//     // Higher priorities can wake a sleeping device, lower priorities may 
//     // not be received if the device is low battery
//     var priority = 'normal';
    
//     // Time to Live, the duration in seconds the push notification service provider
//     // will attempt to deliver the message before dropping it.
//     var ttl = 30;
    
//     // Whether or not the notification is sent as a silent notification (which doesn't display)
//     // A silent notification can be useful for number badges for example on an app
//     var silent = false;
    
//     function CreateMessageRequest() {
//         var token = recipient['token'];
//         var service = recipient['service'];
        
//         if (service == 'GCM') { // Android
//             var messageRequest = {
//               'Addresses': {
//                 [token]: {
//                   'ChannelType' : 'GCM'
//                 }
//               },
//               'MessageConfiguration': {
//                 'GCMMessage': {
//                   'Action': action,
//                   'Body': message,
//                   'Priority': priority,
//                   'SilentPush': silent,
//                   'Title': title,
//                   'TimeToLive': ttl,
//                   'Url': url
//                 }
//               }
//             };
//         }
//         else if (service == 'APNS') { // iOS
//             var messageRequest = {
//               'Addresses': {
//                 [token]: {
//                   'ChannelType' : 'APNS'
//                 }
//               },
//               'MessageConfiguration': {
//                 'APNSMessage': {
//                   'APNSPushType': 'voip', 
//                   'Action': action,
//                   'Body': message,
//                   'Priority': priority,
//                   'SilentPush': silent,
//                   'Title': title,
//                   'TimeToLive': ttl,
//                   'Url': url
//                 }
//               }
//             };
//         }
        
//         return messageRequest;
//     }
    
//     function ShowOutput(data) {
//         if (data['MessageResponse']['Result'][recipient['token']]['DeliveryStatus'] == 'SUCCESSFUL') {
//             var status = "Message Sent! Response Information: ";
//         } else {
//             var status = "The message wasn't sent. Response information: ";
//         }
//         console.log(status);
//         console.dir(data, { depth: null });
//     }
    
//     function SendMessage() {
//         var token = recipient['token'];
//         var service = recipient['service'];
//         var messageRequest = CreateMessageRequest();
        
//         // Using a shared credentials file, with a specified IAM profile
//         var credentials = new AWS.SharedIniFileCredentials({ profile: 'default' });
//         AWS.config.credentials = credentials;
        
//         // Specify AWS Region
//         AWS.config.update({ region: region });
        
//         // Create a new Pinpoint object
//         var pinpoint = new AWS.Pinpoint();
//         var params = {
//             'ApplicationId': pinpoint_app_id,
//             'MessageRequest': messageRequest
//         };
        
//         // Attempt to send the message
//         pinpoint.sendMessages(params, function(err, data) {
//             if (err) console.log(err);
//             else     ShowOutput(data);
//         })
        
//     }
    
//     SendMessage();
// };


//
// Client-to-Client VoIP using Bandwidth WebRTC
//

// This is the server-side AWS Lambda implementation
// - Receives device registrations (for push-notification)
//   - Stores device info in persistent tables
// - Receives call initiation requests (from client to client)
//   - Creates Bandwidth WebRTC session and adds the caller as a participant


// ---------------------------------------
// GraphQL
//




// ---------------------------------------
// Bandwidth WebRTC framework
//

// Bandwidth WebRTC configuration
const accountId = process.env.BW_ACCOUNT_ID;
const username = process.env.BW_USERNAME;
const password = process.env.BW_PASSWORD;

// Check to make sure required environment variables are set
if (!accountId || !username || !password) {
  console.error(
      "ERROR! Please set the BW_ACCOUNT_ID, BW_USERNAME, and BW_PASSWORD environment variables before running this app"
  );
  process.exit(1);
}

// Global variables
const {Client: WebRTCClient, ApiController: WebRTCController} = BandwidthWebRTC;
const webrtcClient = new WebRTCClient({
  basicAuthUserName: username,
  basicAuthPassword: password
});
const webRTCController = new WebRTCController(webrtcClient);


/**
 * Initiate a call to another client
 *   - Requires "CLIENT_ID" of the callee
 *   - Returns the "PARTICIPANT_ID" for the caller to join the WebRTC session
 * 
 */
 const initiateCall =  async (callerId,calleeId) => {
  console.log("initiateCall");
  try {
    // Get the caller and callee deviceInfo records
    // TODO: Sanitize the inputs (callerId,calleeId)
   
    let callerInfo = await API.graphql({ query: queries.getDeviceInfo, variables: { id: callerId } });
    let calleeInfo = await API.graphql({ query: queries.getDeviceInfo, variables: { id: calleeId } });
    
    // Check for existing session
    if (callerInfo.data.getDeviceInfo.participantId !== undefined) {
      // Caller is in another session
      //    TODO: Drop existing session, or cancel this initiation?
      return { message: "ERROR :: Caller already in a session!" };
    }

    if (calleeInfo.data.getDeviceInfo.participantId !== undefined) {
      // Callee is in another session
      //    TODO: Drop existing session, or cancel this initiation?
      return { message: "ERROR :: Callee already in a session!" };
    }

    // Create a new session and add the participants to it
    let sessionId = await getSessionId(uuidv1());
    let [callerPId, callerToken] = await createParticipant(uuidv1());
    let [calleePId, calleeToken] = await createParticipant(uuidv1());
    await addParticipantToSession(callerPId.id, sessionId);
    await addParticipantToSession(calleePId.id, sessionId);

    // Save the updated DeviceInfo records
    callerInfo = callerInfo.data.getDeviceInfo;
    callerInfo.participantId = callerPId;
    callerInfo.participantToken = callerToken;

    calleeInfo = calleeInfo.data.getDeviceInfo;
    calleeInfo.participantId = calleePId;
    calleeInfo.participantToken = calleeToken;

    await API.graphql({ query: mutations.updateDeviceInfo, variables: {input: callerInfo}});
    await API.graphql({ query: mutations.updateDeviceInfo, variables: {input: calleeInfo}});

    // Now that we have added them to the session, we can send back the token they need to join
    return {
      message: "created participant and setup session",
      token: token,
    };
  } catch (error) {
    console.log("Failed to start the browser call:", error);
    return { message: "failed to set up participant" };
  }
};


// ------------------------------------------------------------------------------------------
// All the functions for interacting with Bandwidth WebRTC services below here
//

/**
 * Return the session id
 * This will either create one via the API, or return the one already created for this session
 * @param tag
 * @return a Session id
 */
async function getSessionId(tag) {

  // create the session
  // tags are useful to audit or manage billing records
  const sessionBody = { tag: tag };

  try {
    let sessionResponse = await webRTCController.createSession(
      accountId,
      sessionBody
    );

    return sessionResponse.result.id;
  } catch (error) {
    console.log("Failed to create session:", error);
    throw new Error(
      "Error in createSession, error from BAND:" + error.errorMessage
    );
  }
}

/**
 *  Create a new participant
 * @param tag to tag the participant with, no PII should be placed here
 * @return list: (a Participant json object, the participant token)
 */
async function createParticipant(tag) {
  // create a participant for this browser user
  const participantBody = {
    tag: tag,
    publishPermissions: ["AUDIO"],
    deviceApiVersion: "V3"
  };

  try {
    let createResponse = await webRTCController.createParticipant(
      accountId,
      participantBody
    );

    return [createResponse.result.participant, createResponse.result.token];
  } catch (error) {
    console.log("failed to create Participant", error);
    throw new Error(
      "Failed to createParticipant, error from BAND:" + error.errorMessage
    );
  }
}

/**
 * @param participantId a Participant id
 * @param sessionId The session to add this participant to
 */
async function addParticipantToSession(participantId, sessionId) {
  const body = { sessionId: sessionId };

  try {
    await webRTCController.addParticipantToSession(
      accountId,
      sessionId,
      participantId,
      body
    );
  } catch (error) {
    console.log("Error on addParticipant to Session:", error);
    throw new Error(
      "Failed to addParticipantToSession, error from BAND:" + error.errorMessage
    );
  }
}
