package org.mobicents.arquillian.container;

import jain.protocol.ip.mgcp.CreateProviderException;
import jain.protocol.ip.mgcp.JainMgcpCommandEvent;
import jain.protocol.ip.mgcp.JainMgcpEvent;
import jain.protocol.ip.mgcp.JainMgcpResponseEvent;
import jain.protocol.ip.mgcp.message.CreateConnection;
import jain.protocol.ip.mgcp.message.CreateConnectionResponse;
import jain.protocol.ip.mgcp.message.DeleteConnection;
import jain.protocol.ip.mgcp.message.DeleteConnectionResponse;
import jain.protocol.ip.mgcp.message.NotificationRequest;
import jain.protocol.ip.mgcp.message.NotificationRequestResponse;
import jain.protocol.ip.mgcp.message.Notify;
import jain.protocol.ip.mgcp.message.NotifyResponse;
import jain.protocol.ip.mgcp.message.parms.CallIdentifier;
import jain.protocol.ip.mgcp.message.parms.ConnectionDescriptor;
import jain.protocol.ip.mgcp.message.parms.ConnectionIdentifier;
import jain.protocol.ip.mgcp.message.parms.ConnectionMode;
import jain.protocol.ip.mgcp.message.parms.EndpointIdentifier;
import jain.protocol.ip.mgcp.message.parms.EventName;
import jain.protocol.ip.mgcp.message.parms.NotificationRequestParms;
import jain.protocol.ip.mgcp.message.parms.NotifiedEntity;
import jain.protocol.ip.mgcp.message.parms.RequestIdentifier;
import jain.protocol.ip.mgcp.message.parms.RequestedAction;
import jain.protocol.ip.mgcp.message.parms.RequestedEvent;
import jain.protocol.ip.mgcp.message.parms.ReturnCode;
import jain.protocol.ip.mgcp.pkg.MgcpEvent;
import jain.protocol.ip.mgcp.pkg.PackageName;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.mobicents.protocols.mgcp.stack.JainMgcpExtendedListener;
import org.mobicents.protocols.mgcp.stack.JainMgcpStackImpl;
import org.mobicents.protocols.mgcp.stack.JainMgcpStackProviderImpl;
import org.mobicents.protocols.mgcp.stack.MgcpResponseType;

/**
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 */

public class MgcpCallAgent implements JainMgcpExtendedListener {

	private Logger logger = Logger.getLogger(MgcpCallAgent.class);

	private static final int CA_PORT = 2724;
	protected static final String LOCAL_ADDRESS = "127.0.0.1";
	private static final int MG_PORT = 2427;

	private JainMgcpStackProviderImpl caProvider;
	private int mgStack = MG_PORT;
	private InetAddress localAddress = InetAddress.getByName(LOCAL_ADDRESS);
	private int localPort = CA_PORT;

	protected boolean sentCCR, receivedCCResponse, sentNotificationRequest,
	receiveNotificationRequestResponse, receivedNotification,
	sentNotificatioAnswer, sentDLCX, receivedDLCXA;

	protected EndpointIdentifier specificEndpointId = null;
	protected ConnectionIdentifier specificConnectionId = null;

	protected JainMgcpStackImpl caStack = null;
	protected InetAddress caIPAddress = null;

	public MgcpCallAgent() throws UnknownHostException, CreateProviderException {
		caIPAddress = InetAddress.getByName(LOCAL_ADDRESS);
		caStack = new JainMgcpStackImpl(caIPAddress, CA_PORT);
		caProvider = (JainMgcpStackProviderImpl) caStack.createProvider();

	}

	public void sendCRCX() {

		try {
			caProvider.addJainMgcpListener(this);

			CallIdentifier callID = caProvider.getUniqueCallIdentifier();

			EndpointIdentifier endpointID = new EndpointIdentifier(
					"mobicents/ivr/$", "127.0.0.1:" + mgStack);

			CreateConnection createConnection = new CreateConnection(this,
					callID, endpointID, ConnectionMode.SendRecv);

			String sdpData = "v=0\r\n"
					+ "o=4855 13760799956958020 13760799956958020"
					+ " IN IP4  127.0.0.1\r\n" + "s=mysession session\r\n"
					+ "p=+46 8 52018010\r\n" + "c=IN IP4  127.0.0.1\r\n"
					+ "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"
					+ "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
					+ "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";

			createConnection
			.setRemoteConnectionDescriptor(new ConnectionDescriptor(
					sdpData));

			createConnection.setTransactionHandle(caProvider
					.getUniqueTransactionHandler());

			System.err.println(" - "+localAddress+":"+localPort+" SENDING CRCX");

			caProvider.sendMgcpEvents(new JainMgcpEvent[] { createConnection });

			logger.debug(" CreateConnection command sent for TxId "
					+ createConnection.getTransactionHandle() + " and CallId "
					+ callID);
			sentCCR = true;
		} catch (Exception e) {
			e.printStackTrace();
//			SimpleFlowTest.fail("Unexpected error: " + e);
		}
	}

	@Override
	public void processMgcpCommandEvent(JainMgcpCommandEvent command) {
		if (command instanceof Notify) {
			receivedNotification = true;
			System.err.println(" - "+localAddress+":"+localPort+" RECEIVE NOTIFY");
			NotifyResponse response = new NotifyResponse(command.getSource(),
					ReturnCode.Transaction_Executed_Normally);
			response.setTransactionHandle(command.getTransactionHandle());
			caProvider.sendMgcpEvents(new JainMgcpEvent[] { response });
			sentNotificatioAnswer = true;

			DeleteConnection deleteConnection = new DeleteConnection(this,
					this.specificEndpointId);

			deleteConnection.setConnectionIdentifier(this.specificConnectionId);

			deleteConnection.setTransactionHandle(caProvider
					.getUniqueTransactionHandler());
			
			//Lets add NotificationParms
			NotificationRequestParms parms=new NotificationRequestParms(new RequestIdentifier("1"));
			deleteConnection.setNotificationRequestParms(parms);
			
			System.err.println(" - "+localAddress+":"+localPort+" SEND DLCX");
			caProvider.sendMgcpEvents(new JainMgcpEvent[] { deleteConnection });
			sentDLCX = true;

		}
	}

	@Override
	public void processMgcpResponseEvent(JainMgcpResponseEvent response) {
		MgcpResponseType type = MgcpResponseType
				.getResponseTypeFromCode(response.getReturnCode().getValue());

		if (response instanceof CreateConnectionResponse) {
			receivedCCResponse = true;
			System.err.println(" - "+localAddress+":"+localPort+" RECEIVE CRCXResponse");
			switch (type) {
			case SuccessResponse:
				// Tx executed properly
				CreateConnectionResponse event = (CreateConnectionResponse) response;
				ConnectionIdentifier connectionIdentifier = event
						.getConnectionIdentifier();
				this.specificEndpointId = event.getSpecificEndpointIdentifier();
				NotificationRequest notificationRequest = new NotificationRequest(
						this, specificEndpointId, this.caProvider
								.getUniqueRequestIdentifier());

				this.specificConnectionId=connectionIdentifier;
				this.specificEndpointId=event.getSpecificEndpointIdentifier();
				EventName[] signalRequests = { new EventName(
						PackageName.Announcement, MgcpEvent.ann
								.withParm("http://tests.ip:8080/test.wav"),
						connectionIdentifier) };
				notificationRequest.setSignalRequests(signalRequests);

				RequestedAction[] actions = new RequestedAction[] { RequestedAction.NotifyImmediately };

				RequestedEvent[] requestedEvents = {
						new RequestedEvent(new EventName(PackageName.Dtmf,
								MgcpEvent.dtmf0, connectionIdentifier), actions),
						new RequestedEvent(new EventName(
								PackageName.Announcement, MgcpEvent.of,
								connectionIdentifier), actions) };

				notificationRequest.setRequestedEvents(requestedEvents);
				notificationRequest.setTransactionHandle(caProvider
						.getUniqueTransactionHandler());

				NotifiedEntity notifiedEntity = new NotifiedEntity(
						this.localAddress.toString(), localAddress.toString(),
						this.localPort);
				notificationRequest.setNotifiedEntity(notifiedEntity);

				System.err.println(" - "+localAddress+":"+localPort+" SEND NR");
				caProvider
						.sendMgcpEvents(new JainMgcpEvent[] { notificationRequest });
				sentNotificationRequest = true;
				break;
			case ProvisionalResponse:
				break;
			default:
//				SimpleFlowTest.fail("Bad message: " + response);
			}
		} else if (response instanceof NotificationRequestResponse) {
			receiveNotificationRequestResponse = true;
			System.err.println(" - "+localAddress+":"+localPort+" Receive NRResponse");
			switch (type) {
			case SuccessResponse:

				break;
			case ProvisionalResponse:
				break;
			default:
//				SimpleFlowTest.fail("Bad message: " + response);
			}
		} else if (response instanceof DeleteConnectionResponse) {
			receivedDLCXA = true;
			switch (type) {
			case SuccessResponse:

				break;
			case ProvisionalResponse:
				break;
			default:
//				SimpleFlowTest.fail("Bad message: " + response);
			}
		}
	}

	@Override
	public void transactionRxTimedOut(JainMgcpCommandEvent command) {
		System.err.println("Transaction Rx timed out on = " + localAddress + ":"
				+ localPort);
	}

	@Override
	public void transactionTxTimedOut(JainMgcpCommandEvent command) {
		System.err.println("Transaction Tx timed out on = " + localAddress + ":"
				+ localPort);
	}

	@Override
	public void transactionEnded(int handle) {
		System.err.println("Transaction ended out on = " + localAddress + ":"
				+ localPort);
	}

}
