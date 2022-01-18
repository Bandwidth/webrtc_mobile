/* Amplify Params - DO NOT EDIT
	ENV
	REGION
	PINPOINT_APP_ID
Amplify Params - DO NOT EDIT */

// Based on https://docs.aws.amazon.com/pinpoint/latest/developerguide/send-messages-push.html

// GraphQL endpoint: https://fuga4zxjtjhr5nrv7trxqct2zi.appsync-api.us-east-1.amazonaws.com/graphql
// GraphQL API KEY: da2-pjxkbv3xk5a6vmwbpvzrc2xznu
// Sri's Android Sim Token: ft28mzyASh6LxRBbVrs72I:APA91bEAk2neRMEcbdVg1crEuZhPqWWknFcLpqxc6RmvJlvlxA704n5FiPWsgJsBWJGVD1zDD9eZw5Iaa5JE8ME1kplZ5s3KYTAX4qrFIFbSSKS6NlBd5sfe1jgaryeLgifA7WvYbwmK
// PINPOINT_APP_ID	50227f7582ed413db7bff87066a8ba33

'use strict'; 

import axios from "axios"
import gql from 'graphql-tag';
import graphql from 'graphql';
const { print } = graphql;

import AWS from 'aws-sdk'

import BandwidthWebRTC from "@bandwidth/webrtc"
import uuid from 'uuid';
const { v1: uuidv1 } = uuid;


// ---------------------------------------
// Utilities
//

const wrapResult = (body) => {
  return {
      statusCode: 200,
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
    console.log(" :: BEGIN handler :: ");
    console.log("EVENT :: ", JSON.stringify(event));

    const body = JSON.parse(event.body);
    console.log("BODY :: ", JSON.stringify(body));

    if (body.action === "register") {
      // --- REGISTER ---
      const created = await createDeviceInfo(body.notifyType, body.deviceId, body.deviceToken);
      return wrapResult(created);

    } else if (body.action === "initiateCall") {
      // --- INITIATE ---
      const calling = await initiateCall(body.callerId, body.calleeId);
      return wrapResult(calling);

    } else {
      // --- UNKNOWN ---
      return wrapResult({ message: "ERROR :: Unknown action!" });
    }

  } catch (err) {
    console.log("ERROR :: ", err);
    return wrapResult(err);
  }
};


// ---------------------------------------
// GraphQL
//

const getDeviceInfoQ = gql`
  query getDeviceInfo($id: ID!) {
    getDeviceInfo(id: $id) {
      id
      deviceId
      deviceToken
      notifyType
      participantId
      participantToken
    }
  }
`

const getDeviceInfo = async (clientId) => {
  try {
    console.log(" :: START GetDeviceInfo :: id = ",clientId);
    const params = {
      url: process.env.GRAPH_QL_API_URL,
      method: 'post',
      headers: {
          'x-api-key': process.env.GRAPH_QL_KEY
      },
      data: {
          query: print(getDeviceInfoQ),
          variables: {
            id: clientId
          }
      }
    };
    console.log(" :: PARAMS GetDeviceInfo :: ")
    console.log(JSON.stringify(params))

    const graphqlData = await axios(params);
    console.log(" :: GRAPHQL GetDeviceInfo :: ")
    console.log(graphqlData);

    // TODO: Error checking...
    // if (graphqlData.data === null || graphqlData.errors !== undefined)

    const body = graphqlData.data.data.getDeviceInfo;
    console.log(" :: RESULT GetDeviceInfo :: ")
    console.log(JSON.stringify(body));
    console.log(" :: END GetDeviceInfo :: ");
    return body;
  } catch (err) {
    console.log("ERROR :: ", err);
    return err;
  }
};

const listDeviceInfosQ = gql`
  query {
    listDeviceInfos {
      items {
        id
        notifyType
        deviceId
        deviceToken
        participantId
        participantToken
      }
    }
  }
`

const listDeviceInfos = async () => {
  try {
    console.log(" :: START listDeviceInfos :: ");
    const params = {
      url: process.env.GRAPH_QL_API_URL,
      method: 'post',
      headers: {
          'x-api-key': process.env.GRAPH_QL_KEY
      },
      data: {
          query: print(listDeviceInfosQ),
      }
    };
    console.log(" :: PARAMS listDeviceInfos :: ")
    console.log(JSON.stringify(params))

    const graphqlData = await axios(params);
    console.log(" :: GRAPHQL listDeviceInfos :: ")
    console.log(graphqlData);

    const body = graphqlData.data.data.listDeviceInfos.items;
    console.log(" :: RESULT listDeviceInfos :: ")
    console.log(JSON.stringify(body));
    console.log(" :: END listDeviceInfos :: ");
    return body;
  } catch (err) {
    console.log("ERROR :: ", err);
    return err;
  }
};


const createDeviceInfoQ = gql`
mutation createDeviceInfo($input: CreateDeviceInfoInput!) {
  createDeviceInfo(input: $input) {
    id
    notifyType
    deviceId
    deviceToken
    participantId
    participantToken
  }
}
`

const createDeviceInfo = async (notifyType,deviceId,deviceToken) => {
  try {
    console.log(" :: START CreateDeviceInfo :: ");

    const params = {
      url: process.env.GRAPH_QL_API_URL,
      method: 'post',
      headers: {
          'x-api-key': process.env.GRAPH_QL_KEY
      },
      data: {
        query: print(createDeviceInfoQ),
        variables: {
          input: { notifyType, deviceId, deviceToken }
        }
      }
    };
    console.log(" :: PARAMS CreateDeviceInfo :: ")
    console.log(JSON.stringify(params));

    const graphqlData = await axios(params);
    console.log(" :: GRAPHQL CreateDeviceInfo :: ")
    console.log(graphqlData);

    if (graphqlData.data && (graphqlData.data.data === null || graphqlData.data.errors !== null) ) {
      console.log(" :: RESULT CreateDeviceInfo :: ")
      console.log(JSON.stringify(graphqlData.data.errors));
      throw new Error("ERROR :: "+JSON.stringify(graphqlData.data.errors));
    }

    const body = graphqlData.data.data.createDeviceInfo;
    console.log(" :: RESULT CreateDeviceInfo :: ")
    console.log(JSON.stringify(body));
    console.log(" :: END CreateDeviceInfo :: ");
    return body;
  } catch (err) {
    console.log("ERROR :: ", err);
    return err;
  }
};


const updateDeviceInfoQ = gql`
mutation updateDeviceInfo($input: UpdateDeviceInfoInput!) {
  updateDeviceInfo(input: $input) {
    id
    notifyType
    deviceId
    deviceToken
    participantId
    participantToken
  }
}
`

const updateDeviceInfo = async (clientId,participantId,participantToken) => {
  try {
    console.log(" :: START updateDeviceInfo :: ");

    const params = {
      url: process.env.GRAPH_QL_API_URL,
      method: 'post',
      headers: {
          'x-api-key': process.env.GRAPH_QL_KEY
      },
      data: {
        query: print(updateDeviceInfoQ),
        variables: {
          input: {
            id: clientId,
            participantId: participantId,
            participantToken: participantToken,
            _version: 0,
          }
        }
      }
    };
    console.log(" :: PARAMS updateDeviceInfo :: ")
    console.log(JSON.stringify(params));

    const graphqlData = await axios(params);
    console.log(" :: GRAPHQL updateDeviceInfo :: ")
    console.log(graphqlData);

    const body = graphqlData.data.data.updateDeviceInfo;
    console.log(" :: RESULT updateDeviceInfo :: ")
    console.log(JSON.stringify(body));
    console.log(" :: END updateDeviceInfo :: ");
    return body;
  } catch (err) {
    console.log("ERROR :: ", err);
    return err;
  }
};

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
      action: participantToken,
      
      // Tells the application which call to join
      url: participantToken,
      
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
    }

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
    const result = await pinpoint.sendMessages(params);
    console.log("RESULT :: ", result);

    if (data.MessageResponse.Result[deviceToken].DeliveryStatus === 'SUCCESSFUL') {
      return { message: "success" };
    } else {
      return { message: "failure" };
    }

  } catch(err) {
    console.log("Failed:", err);
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
    
    console.log(" :: CALLER initiateCall :: ");
    console.log(callerInfo);
    console.log(" :: CALLEE initiateCall :: ");
    console.log(calleeInfo);

    // Check for existing session
    if (callerInfo.participantId !== null) {
      // Caller is in a session
      //    TODO: Drop existing session, or cancel this initiation?
      console.log(" :: CALLER BUSY initiateCall :: ");
      return { message: "ERROR :: Caller already in a session!" };
    }

    if (calleeInfo.participantId !== null) {
      // Callee is in a session
      //    TODO: Drop existing session, or cancel this initiation?
      console.log(" :: CALLEE BUSY initiateCall :: ");
      return { message: "ERROR :: Callee already in a session!" };
    }

    // Create a new session and add the participants to it
    let sessionId = await getSessionId(uuidv1());
    let [callerPId, callerToken] = await createParticipant(callerId); // TODO: Just use the clientId?
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

    calleeInfo.participantId = calleePId.id;
    calleeInfo.participantToken = calleeToken;

    await updateDeviceInfo(callerInfo.id, callerInfo.participantId, callerInfo.participantToken);
    await updateDeviceInfo(calleeInfo.id, calleeInfo.participantId, calleeInfo.participantToken);

    // Notify Callee to join the session
    await SendMessage(calleeInfo.notifyType,calleeInfo.deviceToken,"Someone","Special",calleeInfo.participantToken);


    // Now that we have added them to the session, we can send back the token they need to join
    console.log(" :: END initiateCall :: ");
    return { token: callerInfo.participantToken };
  } catch (error) {
    console.log("Failed:", error);
    return { message: "ERROR :: Failed to set up participant", error: error };
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
