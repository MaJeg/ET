package turntaking;

import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.LivingComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.LanguageUtils;
import org.ib.data.StringData;

@ConfigureParams(inputDataTypes={StringData.class},
		outputChannels = {"motivation","changeRole"},
outputDataTypes = {FloatData.class,StringData.class})
public class EmoTurn extends LivingComponent {
	private static final String SCENARIO="scenario";
	private String _actionTendency;
	private double _motivation;
	private String _currentRole;
	private String _scenario;
	private double _initTime;
	
	public EmoTurn(String outboundPort, ComponentConfig config) {
		super(outboundPort, config);
		_initTime=TimeUtils.getCurrentTime()/1000.0;
	}

	@Override
	public void definePublishedData() {
		addOutboundTypeChecker("motivation", FloatData.class);
		addOutboundTypeChecker("changeRole",StringData.class);
	}

	@Override
	public void defineReceivedData() {
		addInboundTypeChecker(StringData.class);
	}

	@Override
	public boolean act() {
		updateMotivation();
		System.out.println("Motivation::"+_motivation);
		System.out.println("Current role::"+_currentRole);
		return true;
	}

	@Override
	protected void handleData(GenericData data) {
		if(data instanceof StringData){
			StringData changeRole = (StringData)data;
			_currentRole=changeRole.getData();
			System.out.println("Received change role::"+_currentRole);
		} 
	}

	private void sendMotivation() {
		System.out.println("Publish motivation::"+_motivation);
		publishData("motivation", new FloatData(0,_motivation,"motivation"));
	}
	
	private void sendChangeRole(){
		publishData("changeRole", new StringData(0,_currentRole,LanguageUtils.IDX_NONE));
	}

	private void updateMotivation() {
		double mact=0.0;
		double mu=0.0;
		double oldMot=_motivation;
		double currentTime=TimeUtils.getCurrentTime()/1000.0;
		if(_actionTendency.equals("excited")){
			if(_currentRole.equals("listener")){
				mact=0.5;
			} else if(_currentRole.equals("speaker")){
				mact=-0.5;
			}
		} else if(_actionTendency.equals("inhibited")){
			if(_currentRole.equals("listener")){
				mact=-0.5;
			} else if(_currentRole.equals("speaker")){
				mact=0.5;
			}
		}
		System.out.println(mact);
		mu=updateMU(currentTime);
		_motivation=mact+mu;
		if(oldMot!=_motivation){
			sendMotivation();
			sendChangeRole();
		}
	}
	
	private double updateMU(double currentTime){
		double mu=0.0;
		if(_currentRole.equals("listener")){
			// We simulate the fact that, when the agent is listener,
			// it has nothing to say at the beginning then 
			if((currentTime-_initTime)>2.0){
				mu=0.5;
			}else{
				mu=-0.5;
			}
		}else {
			// When the agent is speaker, it always has something to say
			mu=-0.5;
		}
		System.out.println(mu);
		return mu;
	}

	@Override
	protected void setupComponent(ComponentConfig arg0){
		if(config.hasProperty(SCENARIO)){
			_scenario=config.getProperty(SCENARIO);
			configScenario();
		}
	}

	private void configScenario() {
		if(_scenario.equals("scenario1")){
			_currentRole="listener";
			_actionTendency="excited";
		} else if(_scenario.equals("scenario2")){
			_currentRole="listener";
			_actionTendency="inhibited";
		} else if(_scenario.equals("scenario3")){
			_currentRole="speaker";
			_actionTendency="excited";
		} else if(_scenario.equals("scenario4")){
			_currentRole="speaker";
			_actionTendency="inhibited";
		}
		updateMotivation();
	}

}
