package turntaking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.SourceComponent;
import org.ib.component.model.ComponentConfig;

@ConfigureParams(outputChannels = {"pitch.data"},
outputDataTypes = {UserSignal.class})
public class PitchPerceptor extends SourceComponent{
	
	private static final String LISTEN_PITCH = "portPitch";
    private static final String HOST = "host";
    private static final String MIN_PITCH="minPitch";
    private static final String MAX_PITCH="maxPitch";
	private int _listenPitchPort;
	private ServerSocket _servSockPitch;
	private PitchListener _pl;
	private double _maxPitch;
	private double _minPitch;
	
	protected PitchPerceptor(String outboundPort, ComponentConfig config) throws IOException {
		super(outboundPort, config);
		_servSockPitch = null;
		_maxPitch=0.0;
		_minPitch=0.0;
	}

	@Override
	public void definePublishedData() {
		addOutboundTypeChecker("pitch.data", UserSignal.class);		
	}

	@Override
	protected void setupComponent(ComponentConfig config) {
		_listenPitchPort=Integer.parseInt(config.getProperty(LISTEN_PITCH));
		_maxPitch=Double.parseDouble(config.getProperty(MAX_PITCH));
		_minPitch=Double.parseDouble(config.getProperty(MIN_PITCH));
		config.getProperty(HOST);	
		try {
			_servSockPitch = new ServerSocket(_listenPitchPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
		_pl = new PitchListener();
		_pl.start();
	}

	private class PitchListener extends Thread{
		public void run(){
			try {
				double pitch=0.0;
				double pitchNorm=0.0;
				Socket client=_servSockPitch.accept();
				BufferedReader outReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String line;
                try {
                    while ((line = outReader.readLine()) != null) {
                        try{
                        	pitch=Double.parseDouble(line);
                        	pitchNorm=Math.max(0,Math.min((pitch-_minPitch)/(_maxPitch-_minPitch),1));
                        	publishData("pitch.data", new UserSignal(0, pitchNorm,"pitch"));
//                        	System.out.println("published::"+pitch);
                        }catch(NumberFormatException e){
	                    	e.printStackTrace();
                    	}catch(IllegalArgumentException e){
	                    	e.printStackTrace();
	                    }
                    }
                    outReader.close();
                    client.close();
                } catch (IOException e) {
                    //-- ignore for now
                }
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
