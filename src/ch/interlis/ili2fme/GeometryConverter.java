package ch.interlis.ili2fme;

import COM.safe.fmeobjects.FMEException;
import COM.safe.fmeobjects.IFMEArea;
import COM.safe.fmeobjects.IFMECurve;
import COM.safe.fmeobjects.IFMEFactoryPipeline;
import COM.safe.fmeobjects.IFMEFeature;
import COM.safe.fmeobjects.IFMENull;
import COM.safe.fmeobjects.IFMEPath;
import COM.safe.fmeobjects.IFMEGeometry;
import COM.safe.fmeobjects.IFMEDonut;
import COM.safe.fmeobjects.IFMEPoint;
import COM.safe.fmeobjects.IFMESession;
import COM.safe.fmeobjects.IFMEGeometryTools;
import ch.interlis.iom.IomObject;
import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.fme.Main;


public class GeometryConverter {
	private int mode=0;
	private IFMESession session=null;
	private IFMEFeature f=null;
	private IFMEFactoryPipeline pipe=null;
	public GeometryConverter(IFMESession session,int converterMode){
		this.session=session;
		f=session.createFeature();
		mode=converterMode;
	}
	private final String DUMMY_TYPE="Dummy";
	private final String DUMMY_ATTR="attr";
	private void initPipe() 
	throws FMEException 
	{
		if(pipe==null){
			pipe=session.createFactoryPipeline("GeometryConverter", null);
			String func=null;
			if(mode==GeometryEncoding.FME_HEXBIN){
				func="@Geometry(FROM_ATTRIBUTE_BINARY_HEX,"+DUMMY_ATTR+")";
			}else if(mode==GeometryEncoding.FME_BIN){
				func="@Geometry(FROM_ATTRIBUTE_BINARY,"+DUMMY_ATTR+")";
			}else{
				// mode==FME_XML
				func="@Geometry(FROM_ATTRIBUTE,"+DUMMY_ATTR+")";
			}
			String factory="FACTORY_DEF * TeeFactory"
				+" INPUT FEATURE_TYPE "+DUMMY_TYPE
				+" OUTPUT FEATURE_TYPE "+DUMMY_TYPE
				+" "+func;
			pipe.addFactory(factory, " ");
		}
	}
	public void dispose()
	{
		if(f!=null){
			f.dispose();
			f=null;
		}
		if(pipe!=null){
			pipe.dispose();
			pipe=null;
		}
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
	private IFMEGeometry getGeometry(IFMEFeature src, String srcAttr) 
	throws DataException 
	{
		try {
			initPipe();
			f.setFeatureType(DUMMY_TYPE);
			if(mode==GeometryEncoding.FME_BIN){
				f.setBinaryAttribute(DUMMY_ATTR, src.getBinaryAttribute(srcAttr));
			}else{
				String val=src.getStringAttribute(srcAttr);
				f.setStringAttribute(DUMMY_ATTR, val);
			}
			pipe.processFeature(f);
			boolean gotOne=pipe.getOutputFeature(f);
			if(!gotOne){
				throw new DataException("failed to get feature back from pipeline");
			}
		} catch (FMEException ex) {
			throw new DataException(ex);
		}
		return f.getGeometry();
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
	 public void FME2polyline(IomObject target,String targetAttr,IFMEFeature src,String srcAttr)
		throws DataException 
	 {
		 IFMEGeometry fmeGeom=null;
			try{
				fmeGeom=getGeometry(src, srcAttr);
				if(fmeGeom instanceof IFMECurve){
					IomObject polyline=Fme2iox.FME2polyline(session,(IFMECurve)fmeGeom);
					target.addattrobj(targetAttr,polyline);
				}else if(fmeGeom instanceof IFMENull){
					// skip it
				}else{
					throw new DataException("unexpected geometry type "+fmeGeom.getClass().getName());
				}
			}finally{
				if(fmeGeom!=null){
					fmeGeom.dispose();
					fmeGeom=null;
				}
			}
	 }
	 public void FME2surface(IomObject target,String targetAttr,IFMEFeature src,String srcAttr)
		throws DataException 
	 {
			IFMEGeometry fmeGeom=null;
			try{
				fmeGeom=getGeometry(src,srcAttr);
				if(fmeGeom instanceof IFMEArea){
					IomObject surface=Fme2iox.FME2surface(session,(IFMEArea)fmeGeom);
					target.addattrobj(targetAttr,surface);
				}else if(fmeGeom instanceof IFMENull){
					// skip it
				}else{
					throw new DataException("unexpected geometry type "+fmeGeom.getClass().getName());
				}
			}finally{
				if(fmeGeom!=null){
					fmeGeom.dispose();
					fmeGeom=null;
				}
			}
	 }
	 public void FME2coord(IomObject target,String targetAttr,IFMEFeature src,String srcAttr)
		throws DataException 
	 {
			IFMEGeometry fmeGeom=null;
			try{
				fmeGeom=getGeometry(src,srcAttr);
				if(fmeGeom instanceof IFMEPoint){
					IomObject coord=Fme2iox.FME2coord((IFMEPoint)fmeGeom);
					target.addattrobj(targetAttr,coord);
				}else if(fmeGeom instanceof IFMENull){
					// skip it
				}else{
					throw new DataException("unexpected geometry type "+fmeGeom.getClass().getName());
				}
			}finally{
				if(fmeGeom!=null){
					//fmeGeom.dispose();
					fmeGeom=null;
				}
			}
	 }
}
