package turntaking;

import org.ib.data.GenericData;
import org.ib.data.TypeIdentification;

@TypeIdentification(typeID=31)
public class FloatData implements GenericData {

	private long _id;
	private double _value;
	private String _name;
	
	public FloatData(){
		_value=0.0;
		_name="Unknown";
		_id=0;
	}
	
	public FloatData(long id, double value, String name) {
		super();
		_id = id;
		_value = value;
		_name = name;
	}

	@Override
	public long getId() {
		return _id;
	}

	@Override
	public void setId(long arg0) {
		_id=arg0;
	}

	/**
	 * @return the value
	 */
	public double getValue() {
		return _value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(double value) {
		_value = value;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return _name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		_name = name;
	}

	
	
}
