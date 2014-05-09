package ch.interlis.ili2fme;


import java.util.ArrayList;
import java.util.Iterator;

import ch.ehi.fme.Main;
import ch.interlis.ili2c.metamodel.Viewable;
import COM.safe.fmeobjects.FMEException;
import COM.safe.fmeobjects.IFMEFeature;
import COM.safe.fmeobjects.IFMESession;

public class GeomAttrIterator {

	private ArrayList<String> geomAttrs=null;
	private Iterator<String> geomAttri=null;
	private IFMEFeature featOri=null;
	private int mode=0;
	public GeomAttrIterator(IFMESession session,IFMEFeature feat,ArrayList<String> geomAttrsCollector,int mode) throws FMEException{
		
		this.mode=mode;
		geomAttrs=geomAttrsCollector;
		geomAttri=geomAttrs.iterator();
		featOri=session.createFeature();
		feat.clone(featOri);
		//featOri.performFunction("@Log()");
	}
	public void dispose()
	{
		if(featOri!=null){
			featOri.dispose();
			featOri=null;
		}
	}
	
	public boolean hasNext() {
		return geomAttri.hasNext();
	}

	public void next(IFMEFeature dup) throws FMEException {
		featOri.clone(dup);
		String geomAttr=geomAttri.next();
		String func=null;
		if(mode==GeometryEncoding.FME_HEXBIN){
			func="@Geometry(FROM_ATTRIBUTE_BINARY_HEX,"+geomAttr+")";
		}else if(mode==GeometryEncoding.FME_BIN){
			func="@Geometry(FROM_ATTRIBUTE_BINARY,"+geomAttr+")";
		}else if(mode==GeometryEncoding.OGC_HEXBIN){
			// some special
			func="@OGCGeometry(from_attribute,wkbhex,"+geomAttr+",1.1)";
		}else{
			// mode==FME_XML
			func="@Geometry(FROM_ATTRIBUTE,"+geomAttr+")";
		}
		dup.performFunction(func);
		dup.setStringAttribute(Main.XTF_GEOMATTR, geomAttr);
	}
	
}
