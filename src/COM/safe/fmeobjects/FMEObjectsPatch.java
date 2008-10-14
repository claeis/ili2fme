package COM.safe.fmeobjects;

public class FMEObjectsPatch {
	private FMEObjectsPatch(){};
	static public void setOwnsNative(IFMESegment seg,boolean ownsNative) 
	{ 
		if(seg instanceof FMEArc){
			((FMEArc)seg).setOwnsNative(ownsNative); 
		}else if(seg instanceof FMELine){
			((FMELine)seg).setOwnsNative(ownsNative); 
		}else{
			throw new IllegalArgumentException();
		}
	}
}
