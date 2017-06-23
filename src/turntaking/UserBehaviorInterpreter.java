package turntaking;

import java.util.Locale;

import org.agent.slang.out.bml.marc.socket.MarcSocket;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.LivingComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.LanguageUtils;
import org.ib.data.StringData;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import ch.epfl.lis.sde.Sde;
import ch.epfl.lis.sde.solvers.EulerMaruyama;


@ConfigureParams(outputChannels = {"gamma.data","changeRole.event"},
outputDataTypes = {FloatData.class,StringData.class},
inputDataTypes = {UserSignal.class})
public class UserBehaviorInterpreter extends LivingComponent {
	private static final String POS_THRESHOLD = "posThreshold";
    private static final String NEG_THRESHOLD = "negThreshold";
    private static final String DEFAULT_GAMMA="defaultGamma";
    private static final String INIT_ROLE="initRole";
	private volatile AgentRole _agentRole;
	private double _accValue;
	private EulerMaruyama _em;
	private volatile double _pitch;
	private volatile double _volume;
	private double _currentTime;
	private double _previousTime;
	private volatile double _gamma;
	private volatile double _posThreshold;
	private volatile double _negThreshold;
	
	public UserBehaviorInterpreter(String outboundPort, ComponentConfig config) {
		super(outboundPort, config);
		_em=new EulerMaruyama();
		_accValue=0.0;
		_gamma=0.0;
		_previousTime=TimeUtils.getCurrentTime()/1000.0;
		_em.setSystem(new Sde(){
			@Override
			public void getDriftAndDiffusion(double t, DoubleMatrix1D xIn, DoubleMatrix1D F, DoubleMatrix2D G)
					throws Exception {
				F.set(0, _accValue);
				G.set(0, 0, 0.0);
			}
			});
		_em.initialize();
		_em.setX(new DenseDoubleMatrix1D(1).assign(0.0));
		_agentRole=AgentRole.SPEAKER;
		_negThreshold=0.0;
		_posThreshold=0.0;
	}

	@Override
	public void definePublishedData() {
		addOutboundTypeChecker("gamma.data", FloatData.class);
		addOutboundTypeChecker("changeRole.event", StringData.class);
	}

	@Override
	public void defineReceivedData() {
		addInboundTypeChecker(UserSignal.class);
	}

	@Override
	public boolean act() {
		double accVolume= 0.0;
		double accPitch= 0.0;
		
		if(_agentRole==AgentRole.LISTENER){
			accPitch= 0.5-4.0*_pitch;
			accVolume=0.5-4.0*_volume;
		}else if(_agentRole==AgentRole.SPEAKER){
			accPitch=-0.5+1.5*_pitch;
			accVolume=-0.5+1.5*_volume;
		}
		
		_accValue=accVolume+accPitch;
		_currentTime=TimeUtils.getCurrentTime()/1000.0;
		
		// On change le pas de temps pour que cela corresponde au
		// delta par rapport à la dernière fois où le module a 
		// été exécuté
		_em.setH(_currentTime-_previousTime);
		
		// Modifier données d'entrée
		try {
			_em.step();
		} catch (Exception e) {
			e.printStackTrace();
		}
		_previousTime=_currentTime;
		DoubleMatrix1D xOut = _em.getX();
		double res = 0.0;
		if(xOut!=null){
			res=xOut.get(0);
		} else {
			//System.out.println("The stochastic solver was unable to find the next confidence value");
		}
		_gamma=res;
		
//		System.out.println("gamma::"+_gamma);
		//System.out.println(_agentRole);
		
		if(_gamma>_posThreshold){
			if(_agentRole==AgentRole.LISTENER){
				_agentRole=AgentRole.SPEAKER;
			} else {
				_agentRole=AgentRole.LISTENER;
			}
			_gamma=0.0;
			_em.setX(new DenseDoubleMatrix1D(1).assign(_gamma));
			publishData("changeRole.event", new StringData(0,_agentRole.toString(),LanguageUtils.getLanguageCodeByLocale(Locale.US)));
		} else if(_gamma<_negThreshold) {
			_gamma=0.0;
			_em.setX(new DenseDoubleMatrix1D(1).assign(_gamma));
		}
		System.out.println("GAMMA AFTER::"+_gamma);
		publishData("gamma.data",new FloatData(0,_gamma,"gamma"));
		return true;
	}

	@Override
	protected void handleData(GenericData data) {
		//System.out.println("handleData");
		if (data instanceof UserSignal) {
			UserSignal uSig =(UserSignal) data;
			String name="";
			name=uSig.getName();
			//System.out.println("Name::"+name);
			if(name!=null){
				if(name.equals("pitch")){
					_pitch=uSig.getSigValue();
					//System.out.println("Pitch::"+_pitch);
				} else if(name.equals("volume")){
					_volume=uSig.getSigValue();
					//System.out.println("Volume::"+_volume);
				}
				
			}
        }
	}

	@Override
	protected void setupComponent(ComponentConfig arg0) {
		String agentRoleStr=config.getProperty(INIT_ROLE);
		
		if(agentRoleStr!=null && agentRoleStr.toLowerCase().equals("speaker")){
			_agentRole=AgentRole.SPEAKER;
		} else if(agentRoleStr!=null && agentRoleStr.toLowerCase().equals("listener")){
			_agentRole=AgentRole.LISTENER;
		}
		publishData("changeRole.event", new StringData(0,_agentRole.toString(),LanguageUtils.getLanguageCodeByLocale(Locale.US)));
		_posThreshold=Double.parseDouble(config.getProperty(POS_THRESHOLD));
		_negThreshold=Double.parseDouble(config.getProperty(NEG_THRESHOLD));
		_gamma=Double.parseDouble(config.getProperty(DEFAULT_GAMMA));
		
		//System.out.println("After Config::\n\n");
		//System.out.println(_posThreshold);
		//System.out.println(_negThreshold);
		//System.out.println(_agentRole);
		//System.out.println(_gamma);
	}

	/**
	 * @return the _volume
	 */
	public double getVolume() {
		return _volume;
	}

	/**
	 * @param _volume the _volume to set
	 */
	public void setVolume(double _volume) {
		this._volume = _volume;
	}

}
