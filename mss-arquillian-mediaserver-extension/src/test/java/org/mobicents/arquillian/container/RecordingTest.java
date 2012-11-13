package org.mobicents.arquillian.container;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mobicents.arquillian.mediaserver.api.EmbeddedMediaserver;
import org.mobicents.arquillian.mss.mediaserver.extension.EmbeddedMediaserverImpl;
import org.mobicents.media.ComponentType;
import org.mobicents.media.core.endpoints.impl.IvrEndpoint;
import org.mobicents.media.server.impl.resource.dtmf.DetectorImpl;
import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.ConnectionMode;
import org.mobicents.media.server.spi.ConnectionType;
import org.mobicents.media.server.spi.MediaType;
import org.mobicents.media.server.spi.dtmf.DtmfDetector;
import org.mobicents.media.server.spi.dtmf.DtmfDetectorListener;
import org.mobicents.media.server.spi.dtmf.DtmfEvent;
import org.mobicents.media.server.spi.player.Player;
import org.mobicents.media.server.spi.recorder.Recorder;
import org.mobicents.media.server.utils.Text;

/**
 * @author yulian oifa
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 */

public class RecordingTest implements DtmfDetectorListener {
	private EmbeddedMediaserver mediaserver;
	private IvrEndpoint user, ivr;

	private List<String> tones = new ArrayList<String>();
	
	@Before
	public void setUp() throws Exception {
		mediaserver = new EmbeddedMediaserverImpl();
		mediaserver.startServer();
		ivr = new IvrEndpoint("/mobicents/ivr/$");
		mediaserver.installEndpoint(ivr);
		user = new IvrEndpoint("/mobicents/ivr/$");
		mediaserver.installEndpoint(user);
	}

	@After
	public void tearDown() {
		mediaserver.stopServer();
	}

	@Test
	public void testRecording() throws Exception {
		long s = System.nanoTime();

		//create user connection
		Connection userConnection = user.createConnection(ConnectionType.RTP,false);        
		Text sd2 = new Text(userConnection.getDescriptor());
		userConnection.setMode(ConnectionMode.INACTIVE);
		Thread.sleep(50);

		//create server connection
		Connection ivrConnection = ivr.createConnection(ConnectionType.RTP,false);        
		Text sd1 = new Text(ivrConnection.getDescriptor());

		ivrConnection.setOtherParty(sd2);
		ivrConnection.setMode(ConnectionMode.SEND_RECV);
		Thread.sleep(50);

		//modify client
		userConnection.setOtherParty(sd1);
		userConnection.setMode(ConnectionMode.SEND_RECV);
		Thread.sleep(50);
		
		Recorder recorder = (Recorder) ivr.getResource(MediaType.AUDIO, ComponentType.RECORDER);
//		recorder.setRecordFile("file://"+this.getClass().getClassLoader().getResource("test-recording.wav").getPath(), false);
		recorder.setRecordFile("file://./src/test/resources/test-recording.wav", false);
		recorder.activate();
		
		DetectorImpl dtmfDetector = (DetectorImpl) ivr.getResource(MediaType.AUDIO, ComponentType.DTMF_DETECTOR);
		dtmfDetector.addListener(this);
		dtmfDetector.activate();
		
		Player player = (Player) user.getResource(MediaType.AUDIO, ComponentType.PLAYER);        
		player.setURL("file://"+this.getClass().getClassLoader().getResource("dtmfs-1-9.wav").getPath());
		player.activate();

		Thread.sleep(10000);

		assertTrue(recorder.getBytesReceived()>0);
		assertTrue(player.getBytesTransmitted()>0);
		
		for (String tone : tones) {
			System.out.println("Tone detected: "+tone);
		}
		
		player.deactivate();
		recorder.deactivate();
		
		user.deleteConnection(userConnection);
		ivr.deleteConnection(ivrConnection);
	}

	@Override
	public void process(DtmfEvent event) {
		tones.add(event.getTone());
	}

}
