package turntaking;

import turntaking.AttrFunc;
import turntaking.EulerFunc;

public class GeneEqu implements EulerFunc{

	private double _damping;
	private double _stiffness;
	private AttrFunc _func;
	
	public GeneEqu(double damping, double stiffness,AttrFunc af){
		_damping=damping;
		_stiffness=stiffness;
		_func=af;
	}
	
	public AttrFunc getFunc(){
		return _func;
	}
	
	public void setFunc(AttrFunc func){
		if(func!=null){
			_func=func;
		}
	}
	
	/**
	 * Calcul de l'équation dy/dt = f(t,y,...)
	 */
	@Override
	public double[] f(double step, double prevStep, double prevValue[], double params[]) {
		double yDotPrec=prevValue[0];
		double yPrec=prevValue[1];
		double yDDot = 0;
		double attrValue=0;
		if(_func!=null){
			attrValue=_func.getAttractor(params);
//			System.out.println("attrValue::"+attrValue);
//			System.out.println("Step::"+step);
//			System.out.println("Delta::"+(step-prevStep));
		}
		
		yDDot=-_damping*yDotPrec -_stiffness*(yPrec-attrValue);
		
		return new double[]{yDDot,yDotPrec};
	}
	
}
