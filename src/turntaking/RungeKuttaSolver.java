package turntaking;

public class RungeKuttaSolver extends OrdinarySolver{	
	public RungeKuttaSolver(EulerFunc func) {
		super(func);
	}

	@Override
	public double[] solve(double step, double prevStep,double[] prevValue,double[] params) {
		EulerFunc func = super.getFunc();
		if(func!=null && prevValue!=null){
			double[] k1=func.f(step, prevStep, prevValue, params);
			double[] k2=func.f(step-(step-prevStep)/2, prevStep, new double[]{prevValue[0]+k1[0]*(step-prevStep)/2,prevValue[1]+k1[1]*(step-prevStep)/2}, params);
			double[] k3=func.f(step-(step-prevStep)/2, prevStep, new double[]{prevValue[0]+k2[0]*(step-prevStep)/2,prevValue[1]+k2[1]*(step-prevStep)/2}, params);
			double[] k4=func.f(step-(step-prevStep)/2, prevStep, new double[]{prevValue[0]+k3[0]*(step-prevStep),prevValue[1]+k3[1]*(step-prevStep)}, params);
			
			double yDot[]=new double[2];
			yDot[0]=(k1[0]+2*k2[0]+2*k3[0]+k4[0])/6;
			yDot[1]=(k1[1]+2*k2[1]+2*k3[1]+k4[1])/6;
			
			return new double[]{prevValue[0]+yDot[0]*(step-prevStep),prevValue[1]+yDot[1]*(step-prevStep)};
		}
		else{
			return null;
		}
	}

}
