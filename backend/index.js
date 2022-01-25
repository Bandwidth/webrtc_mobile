
/*
  -- DEMO APP --
  Bandwidth WebRTC App-to-App VoIP

      This file contains the back-end server component implemented as an AWS Lambda.

      There are several independent components interacting here:
      - Bandwidth WebRTC : REST API calls to create WebRTC conference sessions and add/remove participants
      - AWS Pinpoint     : Notification service used to send PUSH notifications to the end-user mobile device
      - AWS AppSync      : GraphQL database API used to store the DeviceInfo and Person records

  The code in this file is based on several example projects:

    AWS Pinpoint
      https://docs.aws.amazon.com/pinpoint/latest/developerguide/send-messages-push.html

    AWS AppSync
      https://docs.amplify.aws/lib/graphqlapi/graphql-from-nodejs/q/platform/js/

    Bandwidth WebRTC
      https://github.com/Bandwidth-Samples/webrtc-hello-world-js


  Installation & building:
  ------------------------

    - Install AWS Amplify CLI
      curl -sL https://aws-amplify.github.io/amplify-cli/install | bash && $SHELL

    - Create an Amplify project in Amplify Studio and pull a local version
      amplify pull --appId .... --envName staging

    - Replace the local Amplify project source with the source from this repo

    - Update the Amplify project configuration with credentials and endpoints
      amplify function update
        > Environment variables configuration
        > Update existing environment variables
          PINPOINT_APP_ID 
          GRAPH_QL_KEY 
          GRAPH_QL_API_URL 
          BW_ACCOUNT_ID 
          BW_USERNAME 
          BW_PASSWORD 

    - Push the updated Amplify project
      amplify push

*/

'use strict'; 

import axios from "axios"
import gql from 'graphql-tag';
import graphql from 'graphql';
const { print } = graphql;

import AWS from 'aws-sdk'

import BandwidthWebRTC from "@bandwidth/webrtc"
import { v1 as uuidv1 } from 'uuid';


// ---------------------------------------
// Utilities
//

const wrapResult = (body) => {
  let statusCode = 200;
  if (body.error) statusCode = 500;
  return {
      statusCode: statusCode,
      body: JSON.stringify(body),
      headers: {
          "Access-Control-Allow-Origin": "*",
      }
  }
};


// ---------------------------------------
// Lambda Handler
//

export const handler = async (event, context) => {
  try {
    console.log("EVENT :: ", JSON.stringify(event));

    const body = JSON.parse(event.body);
    console.log("BODY :: ", JSON.stringify(body));

    // -----------------------------------------
    // --- ACTION :: REGISTER ---
    //
    if (body.action === "register") {
      const created = await createDeviceInfo(body.notifyType, body.deviceToken);
      return wrapResult(created);

    // -----------------------------------------
    // --- ACTION :: INITIATE CALL ---
    //
    } else if (body.action === "initiateCall") {
      const calling = await initiateCall(body.callerId, body.calleeId);
      return wrapResult(calling);

    // -----------------------------------------
    // --- ACTION :: END CALL ---
    //
    } else if (body.action === "endCall") {
      const clientInfo = await getDeviceInfo(body.clientId);
      await deleteSession(clientInfo.sessionId);
      await deleteParticipant(clientInfo.participantId);
      const ret = await updateDeviceInfo(clientInfo.id, "", "", "",clientInfo._version);
      return wrapResult({ message: "success", result: ret });

    // -----------------------------------------
    // --- ACTION :: DELETE CLIENT ID ---
    //
    } else if (body.action === "deleteClientIds") {
      let result = [];
      while (body.clientIds.length) {
        const id = body.clientIds.pop();
        const clientInfo = await getDeviceInfo(id);
        await deleteSession(clientInfo.sessionId);
        await deleteParticipant(clientInfo.participantId);
        const ret = await deleteDeviceInfo(clientInfo.id,clientInfo._version);
        result.push(ret);
      } 
      return wrapResult({ deletedIds: result });

    // -----------------------------------------
    // --- ACTION :: UNKNOWN ---
    //
    } else {
      return wrapResult({ error: "ERROR :: Unknown action!" });
    }

  } catch (err) {
    console.log("ERROR :: ", err);
    return wrapResult({ error: err });
  }
};


// ---------------------------------------
// GraphQL
//

const queryGraphQl = async (opName,variables) => {
  try {
    console.log(` :: START ${opName} :: `);
    const params = {
      url: process.env.GRAPH_QL_API_URL,
      method: 'post',
      headers: {
          'x-api-key': process.env.GRAPH_QL_KEY
      },
      data: {
        query: print(graphQl[opName]),
        variables
      }
    };
    console.log(` :: PARAMS ${opName} :: `)
    console.log(JSON.stringify(params))

    const graphqlData = await axios(params);
    console.log(` :: GRAPHQL ${opName} :: `)
    console.log(graphqlData);

    // TODO: Error checking...
    if (graphqlData.data && graphqlData.data.errors)
      console.log(graphqlData.data.errors);

    if (graphqlData.data && graphqlData.data.errors) {
      console.log(` :: ERROR ${opName} :: `)
      console.log(JSON.stringify(graphqlData.data.errors));
      throw new Error(`ERROR :: ${JSON.stringify(graphqlData.data.errors)}`);
    }

    const body = graphqlData.data.data[opName];
    console.log(` :: RESULT ${opName} :: `)
    console.log(JSON.stringify(body));
    console.log(` :: END ${opName} :: `);
    return body;
  } catch (err) {
    console.log("ERROR :: ", err);
    return { error: err };
  }
};

const graphQl = {
  getDeviceInfo: gql`
      query getDeviceInfo($id: ID!) {
        getDeviceInfo(id: $id) {
          id
          _version
          deviceToken
          notifyType
          participantId
          participantToken
          sessionId
        }
      }`,

  createDeviceInfo: gql`
      mutation createDeviceInfo($input: CreateDeviceInfoInput!) {
        createDeviceInfo(input: $input) {
          id
          notifyType
          deviceToken
          participantId
          participantToken
        }
      }`,

  updateDeviceInfo: gql`
      mutation updateDeviceInfo($input: UpdateDeviceInfoInput!) {
        updateDeviceInfo(input: $input) {
          id
          notifyType
          deviceToken
          participantId
          participantToken
          sessionId
        }
      }`,

  deleteDeviceInfo: gql`
      mutation deleteDeviceInfo($input: DeleteDeviceInfoInput!) {
        deleteDeviceInfo(input: $input) {
          id
        }
      }`,

  listPeople: gql`
    query listPeople($eq: String!) {
      listPeople(filter: {clientId: {eq: $eq}}) {
        items {
          firstName
          lastName
        }
      }
    }`
};
      
const getDeviceInfo = async (clientId) => {
  return await queryGraphQl("getDeviceInfo",{ id: clientId });
}

const createDeviceInfo = async (notifyType,deviceToken) => {
  return await queryGraphQl("createDeviceInfo",{ input: { notifyType, deviceToken } });
}

const updateDeviceInfo = async (clientId,sessionId,participantId,participantToken,version=0) => {
  return await queryGraphQl("updateDeviceInfo",{
      input: {
          id: clientId,
          sessionId: sessionId,
          participantId: participantId,
          participantToken: participantToken,
          _version: version,
        }
      });
}

const deleteDeviceInfo = async (id,version) => {
  return await queryGraphQl("deleteDeviceInfo",{ input: { id, "_version": version } });
}

const getPersonByClientId = async (clientId) => {
  const ret = await queryGraphQl("listPeople",{ eq: clientId });

  if (ret && ret.items)
    return ret.items[0];
  else
    return ret;
}


// ---------------------------------------
// AWS Pinpoint
//

function CreateMessageRequest(params) {
    if (params.service == 'GCM') { // Android
        return {
          'Addresses': {
            [params.token]: {
              'ChannelType' : 'GCM'
            }
          },
          'MessageConfiguration': {
            'GCMMessage': {
              'Action': params.action,
              'Body': params.message,
              'Priority': params.priority,
              'SilentPush': params.silent,
              'Title': params.title,
              'TimeToLive': params.ttl,
              'Url': params.url
            }
          }
        };
    }
    else if (params.service == 'APNS') { // iOS
        return {
          'Addresses': {
            [params.token]: {
              'ChannelType' : 'APNS'
            }
          },
          'MessageConfiguration': {
            'APNSMessage': {
              'APNSPushType': 'voip', 
              'Action': params.action,
              'Body': params.message,
              'Priority': params.priority,
              'SilentPush': params.silent,
              'Title': params.title,
              'TimeToLive': params.ttl,
              'Url': params.url
            }
          }
        };
    }
    
    // TODO: Error, unknown service type!
    throw new Error("ERROR :: Unknown notification type!");
}

async function SendMessage(notifyType,deviceToken,firstName,lastName,participantToken) {
  try {
    console.log(" :: BEGIN SendMessage :: ");

    const msgParams = {
      title: 'Incoming Call',
      message: `${firstName} ${lastName} is calling you`,
      
      // This object contains the unique device token for the person you want to send
      // the message to, and the push service you want to send the message to
      // Options for 'service':
      //     use 'GCM' if the recipient is an Android device
      //     use 'APNS' if the recipient is an iOS device
      
      token: deviceToken,
      service: notifyType,
      
      // this will open the app to a specific page or interface - the incoming call
      action: "DEEP_LINK",
      
      // Tells the application which call to join
      url: "https://my.app.com/incomingCall?tok="+participantToken,
      
      // Priority of the push notification
      // Higher priorities can wake a sleeping device, lower priorities may 
      // not be received if the device is low battery
      priority:'normal',
      
      // Time to Live, the duration in seconds the push notification service provider
      // will attempt to deliver the message before dropping it.
      ttl: 30,
      
      // Whether or not the notification is sent as a silent notification (which doesn't display)
      // A silent notification can be useful for number badges for example on an app
      silent: false,
    };

    const messageRequest = CreateMessageRequest(msgParams);
    
    // // Using a shared credentials file, with a specified IAM profile
    // const credentials = new AWS.SharedIniFileCredentials({ profile: 'default' });
    // AWS.config.credentials = credentials;
    
    // Specify AWS Region
    // AWS.config.update({ region: process.env.AWS_REGION });
    
    // Create a new Pinpoint object
    const pinpoint = new AWS.Pinpoint();
    const params = {
        'ApplicationId': process.env.PINPOINT_APP_ID,
        'MessageRequest': messageRequest
    };
    
    // Attempt to send the message
    const result = await pinpoint.sendMessages(params).promise();
    console.log("RESULT :: ", result);

    if (result.MessageResponse && result.MessageResponse.Result[deviceToken].DeliveryStatus === 'SUCCESSFUL') {
      console.log(" :: END SendMessage :: SUCCESS ");
      return { message: "success" };
    } else {

      console.log("RESULT-DETAIL :: ", result.MessageResponse.Result[deviceToken]);

      const ret = { 
        message: "failure",
        error: result
      };
      console.log(" :: END SendMessage :: return = ", ret);
      return ret;
    }

  } catch(err) {
    console.log(" :: END SendMessage :: FAILED :: err = ", err);
    return { message: "ERROR :: Failed to notify callee", error: err };
  }
}

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
  console.log(" :: BEGIN initiateCall :: ");
  try {
    // Get the caller and callee deviceInfo records
    // TODO: Sanitize the inputs (callerId,calleeId)
   
    let callerInfo = await getDeviceInfo(callerId);
    let calleeInfo = await getDeviceInfo(calleeId);

    const callerPerson = await getPersonByClientId(callerId);
    
    console.log(" :: CALLER initiateCall :: ");
    console.log(callerInfo);
    console.log(callerPerson);
    console.log(" :: CALLEE initiateCall :: ");
    console.log(calleeInfo);

    // Check for existing session
    if (callerInfo.participantId && callerInfo.participantId.length > 0) {
      // Caller is in a session
      console.log(" :: CALLER BUSY on initiateCall :: ");
      return { message: "ERROR :: Caller already in a session!", error: "BUSY" };
    }

    if (calleeInfo.participantId && calleeInfo.participantId.length > 0) {
      // Callee is in a session
      console.log(" :: CALLEE BUSY on initiateCall :: ");
      return { message: "ERROR :: Callee already in a session!", error: "BUSY" };
    }

    // Create a new session and add the participants to it
    let sessionId = await getSessionId(uuidv1());
    let [callerPId, callerToken] = await createParticipant(callerId);
    let [calleePId, calleeToken] = await createParticipant(calleeId);

    console.log(" :: CALLER PARTICIPANT initiateCall :: ");
    console.log("ID = ", callerPId.id, "TOK = ", callerToken)

    console.log(" :: CALLEE PARTICIPANT initiateCall :: ");
    console.log("ID = ", calleePId.id, "TOK = ", calleeToken)

    await addParticipantToSession(callerPId.id, sessionId);
    await addParticipantToSession(calleePId.id, sessionId);

    // Save the updated DeviceInfo records
    callerInfo.participantId = callerPId.id;
    callerInfo.participantToken = callerToken;
    callerInfo.sessionId = sessionId;

    calleeInfo.participantId = calleePId.id;
    calleeInfo.participantToken = calleeToken;
    calleeInfo.sessionId = sessionId;

    await updateDeviceInfo(callerInfo.id, callerInfo.sessionId, callerInfo.participantId, callerInfo.participantToken, callerInfo._version);
    await updateDeviceInfo(calleeInfo.id, calleeInfo.sessionId, calleeInfo.participantId, calleeInfo.participantToken, calleeInfo._version);

    // Notify Callee to join the session
    let first = "Unknown";
    let last = "Caller";
    if (callerPerson && callerPerson.firstName && callerPerson.lastName) {
      first = callerPerson.firstName;
      last = callerPerson.lastName;
    }
    await SendMessage(calleeInfo.notifyType,calleeInfo.deviceToken,first,last,calleeInfo.participantToken);

    // Now that we have added them to the session, we can send back the token they need to join
    console.log(" :: END initiateCall :: ");
    return { token: callerInfo.participantToken };
  } catch (error) {
    console.log("Failed:", error);
    return { message: "ERROR :: Call initiation failed!", error: error };
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
    // callbackUrl: "https://example.com",  -- TODO: Callback URL for participant lifecycle
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

async function deleteSession(sessionId) {
  try {
    await webRTCController.deleteSession(accountId,sessionId);
  } catch (error) {
    console.log("Error on deleteSession:", error);
    // throw new Error(
    //   "Failed to deleteSession, error from BAND:" + error.errorMessage
    // );
  }
}

async function removeParticipantFromSession(sessionId,participantId) {
  try {
    await webRTCController.removeParticipantFromSession(accountId,sessionId,participantId);
  } catch (error) {
    console.log("Error on removeParticipantFromSession:", error);
    // throw new Error(
    //   "Failed to removeParticipantFromSession, error from BAND:" + error.errorMessage
    // );
  }
}

async function deleteParticipant(participantId) {
  try {
    await webRTCController.deleteParticipant(accountId,participantId);
  } catch (error) {
    console.log("Error on deleteParticipant:", error);
    // throw new Error(
    //   "Failed to deleteParticipant, error from BAND:" + error.errorMessage
    // );
  }
}
