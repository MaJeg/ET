package turntaking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.SourceComponent;
import org.ib.component.model.ComponentConfig;

@ConfigureParams(outputChannels = {"volume.data"},
outputDataTypes = {UserSignal.class})
public class VolumePerceptor extends SourceComponent {
	
	private static final String LISTEN_VOLUME = "portVolume";
    private static final String MIN_VOLUME="minVolume";
    private static final String MAX_VOLUME="maxVolume";
	private int _listenVolumePort;
	private ServerSocket _servSockVolume;
	private VolumeListener _vl;
	private double _maxVolume;
	private double _minVolume;

	protected VolumePerceptor(String outboundPort, ComponentConfig config) {
		super(outboundPort, config);
		_servSockVolume = null;
	}

	@Override
	public void definePublishedData() {
		addOutboundTypeChecker("volume.data", UserSignal.class);
	}

	@Override
	protected void setupComponent(ComponentConfig arg0) {
		
		//System.out.println("SetupComponentVolume");
		_listenVolumePort=Integer.parseInt(config.getProperty(LISTEN_VOLUME));
		try {
			_servSockVolume = new ServerSocket(_listenVolumePort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_maxVolume=Double.parseDouble(config.getProperty(MAX_VOLUME));
		_minVolume=Double.parseDouble(config.getProperty(MIN_VOLUME));
		_vl = new VolumeListener();
		_vl.start();
	}
	
	private class VolumeListener extends Thread{
		public void run(){
			try {
				Socket client=_servSockVolume.accept();
				BufferedReader outReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
				double volume=0.0;
				double volumeNorm=0.0;
                String line;
                try {
                    while ((line = outReader.readLine()) != null) {
                        try{
                        	volume=Double.parseDouble(line);
                        	//System.out.println("Volume Max::"+_maxVolume);
                        	//System.out.println("Volume Min::"+_minVolume);
                        	//System.out.println("Volume::"+volume);
                        	volumeNorm=Math.max(0,Math.min((volume-_minVolume)/(_maxVolume-_minVolume),1));
                        	//System.out.println("Volume Norm::"+volumeNorm);
                        	publishData("volume.data", new UserSignal(1,volumeNorm,"volume"));
                        }catch(IllegalArgumentException arg){
                        	arg.printStackTrace();
                        }
                    }
                    outReader.close();
                    client.close();
                } catch (IOException e) {
                    //-- ignore for now
                }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
