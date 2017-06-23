package turntaking;

public abstract class OrdinarySolver {
	private EulerFunc _func;
	
	protected OrdinarySolver(EulerFunc func){
		if(func!=null){
			_func=func;
		}
	}
	
	public EulerFunc getFunc(){
		return _func;
	}
	
	public void setFunc(EulerFunc func){
		_func=func;
	}
	
	public abstract double[] solve(double step, double prevStep,double[] prevValue,double... params);
}
