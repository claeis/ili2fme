package ch.interlis.ili2fme;

import COM.safe.fmeobjects.FMEException;
import COM.safe.fmeobjects.IFMEFeature;
import COM.safe.fmeobjects.IFMEPath;
import COM.safe.fmeobjects.IFMEGeometry;
import COM.safe.fmeobjects.IFMEDonut;
import COM.safe.fmeobjects.IFMEPoint;
import COM.safe.fmeobjects.IFMESession;
import ch.interlis.iom.IomObject;


public class GeometryConverter {
	private int mode=0;
	private IFMESession session=null;
	private IFMEFeature f=null;
	public GeometryConverter(IFMESession session,int converterMode){
		this.session=session;
		f=session.createFeature();
		mode=converterMode;
	}
	private void setGeometry(IFMEFeature ret, String attrName, IFMEGeometry fmeLine) 
	throws DataException 
	{
		final String ATTR="attr";
		f.setGeometry(fmeLine);
		String func=null;
		if(mode==GeometryEncoding.FME_HEXBIN){
			func="@Geometry(TO_ATTRIBUTE_BINARY_HEX,"+ATTR+")";
		}else if(mode==GeometryEncoding.FME_BIN){
			func="@Geometry(TO_ATTRIBUTE_BINARY,"+ATTR+")";
		}else{
			// mode==FME_XML
			func="@Geometry(TO_ATTRIBUTE,"+ATTR+")";
		}
		 try {
			f.performFunction(func);
		} catch (FMEException ex) {
			throw new DataException(ex);
		}
		try {
			if(mode==GeometryEncoding.FME_BIN){
				 ret.setBinaryAttribute(attrName, f.getBinaryAttribute(ATTR));
			}else{
				 ret.setStringAttribute(attrName, f.getStringAttribute(ATTR));
			}
		} catch (FMEException ex) {
			throw new DataException(ex);
		}
	}
	 public void coord2FME(IFMEFeature ret,String attrName,IomObject value)
		throws DataException 
	 {
		 IFMEPoint point=null;
		 try{
			 point=Iox2fme.coord2FME(session,value);
			 setGeometry(ret,attrName,point);
		 }finally{
			 if(point!=null){
				 point.dispose();
				 point=null;
			 }
		 }
	 }
	 public void polyline2FME(IFMEFeature ret,String attrName,IomObject value)
		throws DataException 
	 {
		 IFMEPath fmeLine=null;
		 try{
			 fmeLine=Iox2fme.polyline2FME(session,value,false);
			 setGeometry(ret, attrName, fmeLine);
		 }finally{
			 if(fmeLine!=null){
				 fmeLine.dispose();
				 fmeLine=null;
			 }
		 }
	 }
	 public void surface2FME(IFMEFeature ret,String attrName,IomObject value)
		throws DataException 
	 {
		 IFMEDonut fmeSurface=null;
		 try{
			 fmeSurface=Iox2fme.surface2FME(session,value);
			 setGeometry(ret, attrName, fmeSurface);
		 }finally{
			 if(fmeSurface!=null){
				 fmeSurface.dispose();
				 fmeSurface=null;
			 }
		 }
	 }
}
