package turntaking;

import org.ib.data.GenericData;
import org.ib.data.TypeIdentification;

@TypeIdentification(typeID=30)
public class UserSignal implements GenericData {
    private long _id;
    private double _sigValue;
    private String _name;

    public UserSignal() {
    }
    
    public UserSignal(long id,double sigValue,String name){
    	_id=id;
    	_sigValue=sigValue;
    	_name=name;
    }
    
	@Override
	public long getId() {
		return _id;
	}

	@Override
	public void setId(long id) {
		_id=id;
	}
	
	public double getSigValue(){
		return _sigValue;
	}
	
	public void setSigValue(double sigValue){
		_sigValue=sigValue;
	}
	
	public String getName(){
		return _name;
	}
	
	public void setName(String name){
		_name=name;
	}

}
