package turntaking;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.agent.slang.data.audio.PlayerEvent;
import org.agent.slang.out.bml.marc.socket.MarcSocket;
import org.agent.slang.out.bml.marc.socket.MarcSocket.DataListener;
import org.agent.slang.out.bml.marc.socket.TCPSocket;
import org.agent.slang.out.bml.marc.socket.UDPSocket;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.LivingComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.StringData;
import org.ib.logger.Logger;

import turntaking.OrdinarySolver;


@ConfigureParams(inputDataTypes = {FloatData.class,StringData.class})
public class SignalsDecider extends LivingComponent implements DataListener {
	private static final String GAZE="gaze";
	private static final String BROWS="brows";
	private static final String PROP_SOCKET_TYPE = "MARCSocketType";
    private static final String PROP_SOCKET_HOST = "MARCHostname";
    private static final String PROP_SOCKET_IN_PORT = "MARCInPort";
    private static final String PROP_SOCKET_OUT_PORT = "MARCOutPort";
	private double _currentPitch;
	private double _currentVolume;
	private double _currentGaze;
	private double _currentBrows;
	private double _previousTime;
	private double _currentTime;
	private String _currUtterance;
	private volatile int _currentIndex;
	private double _prevStep;
	private volatile double _gamma;
	private volatile double _mot;
	private Map<String,OrdinarySolver> _solversSp;
	private Map<String,OrdinarySolver> _solversLis;
	private Map<String,double[]> _prevValues;
	private volatile AgentRole _currentRole;
	private DatagramSocket _sendSocket;
	private DatagramSocket _recvSocket;
	private static final String audioPlayerData = "audioPlayer.data";
	private final Queue<String> bmlExecutionQueue = new LinkedList<String>();
	private Long lastExecutionTimestamp = null;
	private final static long EXECUTION_TIMEOUT = 5 * 60 * 1000;
	private String _trackId;
	private String _bmlId;
	private volatile int _cnt;
	private volatile boolean _isLaunched;
	private int _inPort;
	private int _outPort;
	private String _hostname;
	private ReceiveBMLMessageThread _recvThread;
	public boolean _isEnded;
	private FileWriter _signalLogger;
	private boolean _receivedGamma;
	
	public SignalsDecider(String outboundPort, ComponentConfig config){
		super(outboundPort, config);
		_currUtterance="128";
		_currentIndex=1;
		_currentRole=AgentRole.LISTENER;
		_mot=1.0; 
		_prevStep=TimeUtils.getCurrentTime()/1000.0;
		_solversSp=new HashMap<String,OrdinarySolver>();
		_solversLis=new HashMap<String,OrdinarySolver>();
		_prevValues=new HashMap<String,double[]>();
		_isEnded=false;
		try {
			System.setErr(new PrintStream("C:\\Users\\jegou\\Documents\\modele_tdep\\TT_NARECASlang\\signals\\errors.txt"));
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		}
		try {
			_signalLogger = new FileWriter(new File("C:\\Users\\jegou\\Documents\\modele_tdep\\TT_NARECASlang\\signals\\signals_sc1.csv"));
			_signalLogger.write("volume,pitch,gaze,eyebrows\n");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			_sendSocket=new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		_trackId="Track_0";
		_bmlId="bml_item_";
		_isLaunched=false;
		_receivedGamma=false;
	}
	
	public double getCurrentPitch() {
		return _currentPitch;
	}

	public void setCurrentPitch(double currentPitch) {
		_currentPitch = currentPitch;
	}

	public double getCurrentVolume() {
		return _currentVolume;
	}

	public void setCurrentVolume(double currentVolume) {
		_currentVolume = currentVolume;
	}

	public double getCurrentGaze() {
		return _currentGaze;
	}

	public void setCurrentGaze(double currentGaze) {
		_currentGaze = currentGaze;
	}

	public double getCurrentBrows() {
		return _currentBrows;
	}

	public void setCurrentBrows(double currentBrows) {
		_currentBrows = currentBrows;
	}

	public double getPreviousTime() {
		return _previousTime;
	}

	public void setPreviousTime(double previousTime) {
		_previousTime = previousTime;
	}

	public double getCurrentTime() {
		return _currentTime;
	}

	public void setCurrentTime(double currentTime) {
		_currentTime = currentTime;
	}

	public String getCurrUtterance() {
		return _currUtterance;
	}

	public void setCurrUtterance(String currUtterance) {
		_currUtterance = currUtterance;
	}

	@Override
	public void defineReceivedData() {
		addInboundTypeChecker(FloatData.class);
		addInboundTypeChecker(StringData.class);
	}

	public String getCurrentChunk(String utterance){
//		ClassLoader loader = SignalsDecider.class.getClassLoader();
//		URL currentChunkUrl=loader.getResource("/resource/chunks/"+_currUtterance+"_"+_currentIndex+".wav");
//		String currentChunkStr="";
//		if(currentChunkUrl!=null){
//			currentChunkStr=currentChunkUrl.toString();
//		}else{
//			System.err.println("No chunk found");
//		}
		String currentChunkStr=null;
		currentChunkStr="C:\\Users\\jegou\\Documents\\modele_tdep\\TT_NARECASlang\\resource\\chunks\\";
		currentChunkStr=currentChunkStr+_currUtterance+"_"+_currentIndex+".wav";
//		System.out.println("Next chunk::"+currentChunkStr);
		return currentChunkStr;
	}
	
	@Override
	public boolean act() {
		// Pour éviter de lancer le module avec que l'UserBehaviorInterpreter soit intialisé
		if(_receivedGamma){
			// 1 : Implémenter les équations de contrôle des signaux
			Map<String,Double> values=new HashMap<String,Double>();
			for(String id: _solversSp.keySet()){
				double[] res=null;
				try {
					res = execute(id);
				} catch (UndefinedResultException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				_prevValues.put(id, res);
				values.put(id, res[1]);
	
				// 2 : Utiliser les valeurs de sortie pour contrôler les signaux de l'agent
				System.out.println("GenerateMsg::Time::"+TimeUtils.getCurrentTime()/1000.0+"id::"+id+"::"+res[1]+"gamma:"+_gamma+"mot:"+_mot);
				if(!id.equals("volume") || (res[1]>0.2 && !_isLaunched) || (res[1]<0.2 && _isLaunched)){
					String msg=generateBMLMsg(id,res[1]);
					scheduleForExecutionMessage(msg);
				}
			}
			try {
				_signalLogger.write(values.get("volume")+","+values.get("pitch")+","+values.get("gaze")+","+values.get("brows")+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Gamma act::"+_gamma);
			System.out.println("Current Role::"+_currentRole);
			System.out.println("Motivation::"+_mot);
		}
		return true;
	}
	
	public double[] execute(String id) throws Exception{
		// Calcul de la valeur de dérivée de l'équation
		double step = TimeUtils.getCurrentTime()/1000.0;
		double[] res=null;
		OrdinarySolver solver=null;
		if(_currentRole.equals(AgentRole.SPEAKER)){
			solver=_solversSp.get(id);
		} else{
			solver=_solversLis.get(id);
		}
		
		double[] currValue=_prevValues.get(id);
		if(solver!=null){
//			System.out.println("Compute:"+id+" gamma:"+_gamma+" mot:"+_mot);
			currValue=solver.solve(step, _prevStep, currValue, _mot,_gamma);
		}else{
			throw new Exception("Solver Undefined");
		}
		if(currValue!=null && currValue.length==2){
			res=currValue;
		} else {
			throw new UndefinedResultException("The solver was unable to compute the next value.");
		}
		_prevStep=step;
		return res;
	}

	@Override
	protected void handleData(GenericData datum) {
//		System.out.println("handleData::"+datum.toString());
		if(datum instanceof StringData){
//			// Change role data
			String currentRole=((StringData) datum).getData();
			if(currentRole.toLowerCase().equals("speaker")){
				_currentRole=AgentRole.SPEAKER;
			} else{
				_currentRole=AgentRole.LISTENER;
			}
			System.out.println("Received changeRole::"+currentRole);
			
		} else if(datum instanceof FloatData){
			FloatData floatDat=(FloatData)datum;
			if(floatDat.getName().equals("gamma")){
				_gamma=floatDat.getValue();
				_receivedGamma=true;
//				System.out.println("Received Gamma::"+_gamma);
			} else if(floatDat.getName().equals("motivation")){
				_mot=floatDat.getValue();
				System.out.println("Received motivation::"+_mot);
			}
		}
	}
	
	private String generateBMLMsg(String id, double value){
		String msg=null;
		if(id.equals("brows")){
			msg = "<bml id=\""+_trackId+"\">\n"+
					"<marc:fork id=\""+_trackId+"_fork_1\">\n"+
		            "<face id=\""+_bmlId+_cnt++ +"\" type=\"FACS\" side=\"BOTH\" amount=\""+value+"\" au=\""+value+"\" marc:interpolate=\"0.2\" marc:interpolation_type=\"linear\" /> \n"+
					"</marc:fork>\n"+
		            "</bml>\n";
		} else if(id.equals("gaze")){
			msg="<bml id=\""+_trackId+"\">\n"+
			"<marc:fork id=\""+_trackId+"_fork_1\">\n"+
	        "<gaze id=\""+_bmlId+_cnt++ +"\" target=\"start\" direction=\"DOWN\" angle=\""+value+"\" /> \n"+		
			"</marc:fork>\n"+
	        "</bml>\n";
		} else if(id.equals("volume")){
			// Soit on coupe soit on relance le dernier chunk BML
			if(value>0.2){
				msg="<bml id=\""+_trackId+"\">\n"+
				"<marc:fork id=\""+_trackId+"_fork_1\">\n"+
				"<speech"+ "\n"+
				"id=\"wav" +"\"\n"+
				"marc:volume=\""+value+"\"\n"+
				"marc:articulate=\"1.0\"\n";
				msg=msg+"marc:file=\""+getCurrentChunk(_currUtterance)+"\"\n/>";
				msg=msg+"</marc:fork>\n</bml>\n";
				_isLaunched=true;
			} else if(value<=0.2){
				msg="<bml id=\""+_trackId+"\">\n"+
			      "<marc:fork id=\""+_trackId+"_fork_1\">\n"+
			      "<marc:speech_stop id=\"wav\" />\n"+ 
			      "</marc:fork>\n"+
			      "</bml>";
				_isLaunched=false;
			}
//			System.out.println("Volume Msg::"+msg);
		}
		return msg;
	}

	@Override
	protected void setupComponent(ComponentConfig config) {
	
        if (config.hasProperty(PROP_SOCKET_HOST)) {
            _hostname = config.getProperty(PROP_SOCKET_HOST);
        }

        if (config.hasProperty(PROP_SOCKET_IN_PORT)) {
            try {
            	_inPort=Integer.parseInt(config.getProperty(PROP_SOCKET_IN_PORT));
            } catch (NumberFormatException e) {
                Logger.log(this, Logger.CRITICAL, "Invalid inPort provided", e);
            }
        }
        
        try {
        	_outPort=Integer.parseInt(config.getProperty(PROP_SOCKET_OUT_PORT));
        	_recvSocket=new DatagramSocket(_outPort);
        	_recvThread=new ReceiveBMLMessageThread();
//        	System.out.println("Starting thread");
        	_recvThread.start();
        } catch (NumberFormatException e) {
            Logger.log(this, Logger.CRITICAL, "Invalid outPort provided", e);
        } catch (SocketException e) {
			e.printStackTrace();
		}

		boolean isGaze=Boolean.parseBoolean(config.getProperty(GAZE));
		if(isGaze){
			// Instancier le solver et ajouter à la liste des solvers
			BifurcGazeSpeaker blGaze = new BifurcGazeSpeaker();
			EulerFunc fGaze = new GeneEqu(4.0, 20.0,blGaze);
			OrdinarySolver osGaze = new RungeKuttaSolver(fGaze);
			BifurcGazeListener blGazeLis = new BifurcGazeListener();
			EulerFunc fGazeLis = new GeneEqu(4.0, 20.0,blGazeLis);
			OrdinarySolver osGazeLis = new RungeKuttaSolver(fGazeLis);
			_solversSp.put("gaze", osGaze);
			_solversLis.put("gaze", osGazeLis);
			_prevValues.put("gaze", new double[]{0.0,0.5});
		}
		
		boolean isBrows=Boolean.parseBoolean(config.getProperty(BROWS));
		if(isBrows){
			// Instancier la variation de sourcils et ajouter à la liste des solvers
			
			BifurcBrowsSpeaker blFace = new BifurcBrowsSpeaker();
			EulerFunc fFace = new GeneEqu(1.0, 5.0,blFace);
			OrdinarySolver osFace = new RungeKuttaSolver(fFace);
			
			BifurcBrowsListener blFaceLis = new BifurcBrowsListener();
			EulerFunc fFaceLis = new GeneEqu(4.0, 20.0,blFaceLis);
			OrdinarySolver osFaceLis = new RungeKuttaSolver(fFaceLis);
			_solversSp.put("brows", osFace);
			_solversLis.put("brows", osFaceLis);
			_prevValues.put("brows", new double[]{0.0,0.0});
		}
		
		
		// Instancier le solver de prosodie et ajouter à la liste
		BifurcSpeaker bl = new BifurcSpeaker();
		BifurcIntonSpeaker blSpeak = new BifurcIntonSpeaker();
		EulerFunc f = new GeneEqu(1.0, 40.0,bl);
		EulerFunc fPit = new GeneEqu(2.0, 10.0,blSpeak);
		OrdinarySolver os = new RungeKuttaSolver(f);
		OrdinarySolver osPit = new RungeKuttaSolver(fPit);
		
		BifurcListener blLis = new BifurcListener();
		BifurcIntonListener blPitLis = new BifurcIntonListener();
		EulerFunc fLis = new GeneEqu(1.0, 40.0,blLis);
		EulerFunc fPitLis = new GeneEqu(2.0, 10.0,blPitLis);
		OrdinarySolver osLis = new RungeKuttaSolver(fLis);
		OrdinarySolver osPitLis = new RungeKuttaSolver(fPitLis);

		_solversSp.put("volume", os);
		_solversSp.put("pitch", osPit);

		if(_currentRole.equals(AgentRole.SPEAKER)){
			_prevValues.put("volume", new double[]{0.0,0.5});
			_prevValues.put("pitch", new double[]{0.0,0.5});
		} else if(_currentRole.equals(AgentRole.LISTENER)){
			_prevValues.put("volume", new double[]{0.0,0.0});
			_prevValues.put("pitch", new double[]{0.0,0.0});
		}
		
		_solversLis.put("volume", osLis);
		_solversLis.put("pitch", osPitLis);
	}
	

	@Override
	public void definePublishedData() {
	}

	@Override
	public void dataReceived(String message) {
//        System.out.println("From MARC: " + message);
        if (message.toLowerCase().contains("wav:start")) {
//            publishData(audioPlayerData, new PlayerEvent(0, PlayerEvent.EVENT_START));
//            System.out.println("Sending player start message !");
        } else if (message.toLowerCase().contains("wav:end")) {
        	try{                    
	    		Writer output;
	    		output = new BufferedWriter(new FileWriter("CommandsLogFile.txt", true));
	    		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	    		Date date = new Date();
	    		output.append("<bmlEndTime>"+dateFormat.format(date)+"</bmlEndTime>");
	    		output.close();
	    		// Si lancé cela veut dire que l'on a finit de prononcer le chunk, on passe au chunk suivant
	    		if(_isLaunched){
//	    			_currentIndex++;
//	    			System.out.println("Planning next utterance");
//	    			String msg=generateBMLMsg("volume",0.5);
//	    			System.out.println("Schedule for execution::"+msg);
//	    			scheduleForExecutionMessage(msg);
	    		}
            }
            catch (IOException e)
	    	{
	    		System.out.println("\nError: File writer crashed!\n");
	    		Logger.log(this, Logger.CRITICAL, "Error in MARC BML Translation Component log file management!", e); 
	    	}
            publishData(audioPlayerData, new PlayerEvent(0, PlayerEvent.EVENT_STOP));
//            System.out.println("Sending player stop message !");
            stopAndScheduleNext();
        }
	}
	
	private void stopAndScheduleNext() {
        synchronized (bmlExecutionQueue) {
            lastExecutionTimestamp = null;
            scheduleNextBML();
        }
    }
	
	private void scheduleForExecutionMessage(String message) {
        synchronized (bmlExecutionQueue) {
//        	System.out.println("Schedule Message::"+message);
            bmlExecutionQueue.offer(message);
            scheduleNextBML();
        }
    }
	
	private void scheduleNextBML() {
        synchronized (bmlExecutionQueue) {
//            if (lastExecutionTimestamp == null || System.currentTimeMillis() - lastExecutionTimestamp > EXECUTION_TIMEOUT) {
                String message = bmlExecutionQueue.poll();
                if (message != null) {
                    if (_sendSocket != null) {
//                        Logger.log(this, Logger.INFORM, "MARC Sending message: " + message);
                        
                        try{                    
	                        Writer output;
	    		    		output = new BufferedWriter(new FileWriter("CommandsLogFile.txt", true));
	    		    		
	    		    		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	    		    		Date date = new Date();
	    		    		output.append("<bmlStartTime>"+dateFormat.format(date)+"</bmlStartTime>"+message);
	    		    		output.close();
	    		    		System.out.println("Sending Message::"+message+" port::"+_inPort);
	    		    		DatagramPacket dp = new DatagramPacket(message.getBytes(),message.getBytes().length,InetAddress.getLocalHost(),_inPort);
	                        _sendSocket.send(dp);	                        
	                        lastExecutionTimestamp = System.currentTimeMillis();
                        }
                        catch (IOException e)
        		    	{
        		    		System.out.println("\nError: File writer crashed!\n");
        		    		Logger.log(this, Logger.CRITICAL, "Error in MARC BML Translation Component log file management!", e); 
        		    	}
                    } else {
                        Logger.log(this, Logger.INFORM, "Marc Sending message failed: " + message);
                    }
                } else {
                    lastExecutionTimestamp = null;
                }
//            }
        }
    }
	
	private class ReceiveBMLMessageThread extends Thread{

		public void run(){
			if (_recvSocket != null) {
	            
	            byte[] buffer = new byte[2048];
	            while (!_isEnded) {
	                buffer[0] = 0;
	                DatagramPacket packet=null;
					try {
						packet = new DatagramPacket(buffer, buffer.length,InetAddress.getByName("127.0.0.1"),_outPort);
					} catch (UnknownHostException e1) {
						e1.printStackTrace();
					}
	                try {
//	                	System.out.println("Waiting message");
	                    _recvSocket.receive(packet);
//	                    System.out.println("Received Message::"+packet.getLength());
	                } catch (IOException e) {
	                	e.printStackTrace();
	                }
	                TreatDataThread tdt = new TreatDataThread(buffer);
	                tdt.start();
	            }
	        }
//			System.out.println("End thread");
		}
	}
	
	
	private class TreatDataThread extends Thread {
		private byte[] buffer;
		
		public TreatDataThread(byte[] buffer){
			this.buffer=buffer;
		}
		
		public void run(){
			StringBuilder line = new StringBuilder();
			String newLine = new String(buffer);
//            System.out.println(newLine);
            dataReceived(newLine);
		}
	}
}
