/* This file is part of the ili2fme project.
 * For more information, please see <http://www.eisenhutinformatik.ch/interlis/ili2fme/>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package ch.interlis.ili2fme;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;

import COM.safe.fme.pluginbuilder.IFMEWriter;
import COM.safe.fmeobjects.IFMEFeature;
import COM.safe.fmeobjects.IFMEFeatureVector;
import COM.safe.fme.pluginbuilder.IFMEMappingFile;
import COM.safe.fmeobjects.IFMEFactoryPipeline;
import COM.safe.fmeobjects.IFMEGeometry;
import COM.safe.fmeobjects.IFMENull;
import COM.safe.fmeobjects.IFMEPoint;
import COM.safe.fmeobjects.IFMECurve;
import COM.safe.fmeobjects.IFMEArea;
import COM.safe.fmeobjects.IFMESimpleArea;
import COM.safe.fmeobjects.IFMEDonut;
import COM.safe.fmeobjects.IFMEPolygon;
import COM.safe.fmeobjects.IFMESession;
import COM.safe.fmeobjects.FMEException;
import COM.safe.fmeobjects.IFMELogFile;
import COM.safe.fmeobjects.IFMEStringArray;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.basics.tools.StringUtility;
import ch.ehi.fme.*;

import ch.interlis.ili2c.metamodel.*;

import ch.interlis.iom.*;
import ch.interlis.iox.*;
import ch.interlis.iox_j.jts.Jts2iox;
import ch.interlis.iox_j.jts.Iox2jtsException;

/** INTERLIS implementation of an FME-Writer.
 * @author ce
 * @version $Revision: 1.0 $ $Date: 17.05.2005 $
 */
public class Ili2Writer implements IFMEWriter {
	private IFMEMappingFile mappingFile=null;
	private IFMESession session=null;
	private String writerKeyword=null;
	private String writerTypename=null;
	private String xtfFile=null;
	private IoxWriter ioxWriter=null;
	private java.io.OutputStream outputFile=null; // output stream to ioxWriter
	private ch.interlis.ili2c.metamodel.TransferDescription iliTd=null;
	private HashMap basketv=null; // set<String basketId,StartBasketEvent>
	private HashMap featurebufferv=new HashMap(); // set<String basketId,IFMEFeatureVectorOnDisk>
	private HashMap fmeFeatureTypev=null; // map<String fmeFeatureTypeName, ViewableWrapper wrapper>
	private HashMap tag2class=null; // map<String iliQName,Viewable|AttributeDef modelele>
	private int formatMode=0;
	private static final int MODE_XTF=1;
	private static final int MODE_ITF=2;
	private int  inheritanceMapping=InheritanceMapping.SUPERCLASS;
	private boolean useLineTableFeatures=true;
	private boolean doRichGeometry=true;
	private boolean scanXtfBaskets=true;
	private boolean autoXtfBaskets=true;
	private ArrayList modeldirv=null;
	private HashSet modelsFromFME=null;
	private int maxTid=1; // only used if formatMode==MODE_ITF
	public Ili2Writer(IFMESession session1,IFMEMappingFile mappingFile1,String writerTypename,String keyword,IFMELogFile log){
		mappingFile=mappingFile1;
		writerKeyword=keyword;
		session=session1;
		this.writerTypename=writerTypename;
		listener=Main.setupLogging(log);
	}
	// Open up the writer and pass it the destination directory to which files will
	// be written (1st parameter) and a string array which may contain additional
	// dataset parameters (2nd parameter).
	public void open(ArrayList args) throws Exception {
		try{
			myopen(args);
		}catch(Exception ex)
		{
			EhiLogger.logError(ex);
			throw ex;
		}
	}
	private void myopen(ArrayList args) throws Exception {
		if(args.size()==0){
			throw new IllegalArgumentException("args.size()==0");
		}
		maxTid=1;
		//EhiLogger.getInstance().setTraceFilter(false);
		// setup logging of trace messages
		if(true){
			for(int i=0;i<args.size();i++){
				String arg=(String)args.get(i);
				if(arg.equals(Main.TRACEMSGS)){
					i++;
					EhiLogger.getInstance().setTraceFilter(!FmeUtility.isTrue((String)args.get(i)));
					break;
				}else{
					// skip this argument
				}
			}
			Iterator elei=mappingFile.elements();
			while(elei.hasNext()){
				Object eleo=elei.next();
				if(eleo instanceof ArrayList){
					ArrayList ele=(ArrayList)eleo;
					String val=(String)ele.get(0);
					if(val.equals(writerKeyword+"_"+Main.TRACEMSGS)){
						EhiLogger.getInstance().setTraceFilter(!FmeUtility.isTrue((String)ele.get(1)));
						break;
					}
				}
			}
		}
		String models=null;
		String modeldir=null;
		for(int i=0;i<args.size();i++){
			String arg=(String)args.get(i);
			EhiLogger.traceState("arg["+Integer.toString(i)+"] "+arg);
			if(arg.equals(Main.MODELS)){
				i++;
				models=(String)args.get(i);
			}else if(arg.equals(Main.MODEL_DIR)){
				i++;
				modeldir=(String)args.get(i);
			}else if(arg.equals(Main.INHERITANCE_MAPPING)){
				i++;
				inheritanceMapping=InheritanceMapping.valueOf((String)args.get(i));
			}else if(arg.equals(Main.USE_LINETABLES)){
				i++;
				useLineTableFeatures=FmeUtility.isTrue((String)args.get(i));
			}else{
				// skip this argument
			}
		}
		Iterator elei=mappingFile.elements();
		while(elei.hasNext()){
			Object eleo=elei.next();
			EhiLogger.traceState("element "+eleo.getClass()+","+eleo);	
			if(eleo instanceof ArrayList){
				ArrayList ele=(ArrayList)eleo;
				String val=(String)ele.get(0);
				if(val.equals(writerKeyword+"_"+Main.MODELS)){
					models=(String)ele.get(1);	
				}else if(val.equals(writerKeyword+"_"+Main.MODEL_DIR)){
					modeldir=(String)ele.get(1);	
				}else if(val.equals(writerKeyword+"_"+Main.USE_LINETABLES)){
					useLineTableFeatures=FmeUtility.isTrue((String)ele.get(1));
				}else if(val.equals(writerKeyword+"_"+Main.INHERITANCE_MAPPING)){
					inheritanceMapping=InheritanceMapping.valueOf((String)ele.get(1));
				}
			}
		}
		if(models==null){
			throw new IllegalArgumentException("model name not specified; set FME-Parameter Models");
		}
		EhiLogger.logState("useLineTables <"+useLineTableFeatures+">");
		EhiLogger.logState("inheritanceMapping <"+InheritanceMapping.toString(inheritanceMapping)+">");
		EhiLogger.traceState("models <"+models+">");
		
		xtfFile=(String)args.get(0);
		if(xtfFile.length()>=2 && xtfFile.charAt(0)=='/' && xtfFile.charAt(1)=='/'){
			StringBuffer x=new StringBuffer(xtfFile);
			x.setCharAt(0,'\\');
			x.setCharAt(1,'\\');
			xtfFile=x.toString();
		}
		EhiLogger.logState("xtfFile <"+xtfFile+">");
		String xtfExt=ch.ehi.basics.view.GenericFileFilter.getFileExtension(new java.io.File(xtfFile)).toLowerCase();
		if(xtfExt.equals("itf")){
			formatMode=MODE_ITF;
		}else{
			formatMode=MODE_XTF;
		}

		if(modeldir==null){
			modeldir=new java.io.File(session.fmeHome(),"plugins/interlis2/ili22models").getAbsolutePath();
			modeldir=new java.io.File(session.fmeHome(),"plugins/interlis2/ilimodels").getAbsolutePath()+";"+modeldir;
			modeldir=new java.io.File(xtfFile).getParentFile().getAbsolutePath()+";"+modeldir;
		}else{
			int startPos=modeldir.indexOf(Main.XTFDIR_PLACEHOLDER);
			if(startPos>-1){
				StringBuffer buf=new StringBuffer(modeldir);
				buf.replace(startPos,startPos+Main.XTFDIR_PLACEHOLDER.length(),new java.io.File(xtfFile).getParentFile().getAbsolutePath());
				modeldir=buf.toString();
			}
		}
		EhiLogger.traceState("modeldir <"+modeldir+">");

		modeldirv=new ArrayList(java.util.Arrays.asList(modeldir.split(";")));
		ArrayList iliModelv=null;
		modelsFromFME=new HashSet();
		if(models.equals(Main.DATA_PLACEHOLDER)){
			// TODO skip throw new IllegalArgumentException("model name not specified; set FME-Parameter Models");
		}else{
			// parse string
			iliModelv=new ArrayList(java.util.Arrays.asList(models.split(";")));
			// compile models
			setupModel(iliModelv, modeldirv);
		}

		// setup output stream
		outputFile=null;
		try{
			java.io.File outfile=new java.io.File(xtfFile);
			java.io.File outdir=outfile.getParentFile();
			if(!outdir.exists()){
				if(!outdir.mkdirs()){
					throw new java.io.IOException("failed to create directory "+outdir.getAbsolutePath());
				}
			}
			outputFile=new java.io.FileOutputStream(outfile);
		}catch(java.io.IOException ex){
		  throw ex;
		}

		// ASSERT: ready to write data

	}
	private void setupModel(ArrayList iliModelv, ArrayList modeldirv) 
	{
		iliTd=ch.interlis.ili2c.Main.compileIliModels(iliModelv,modeldirv,null);
		if(iliTd==null){
			// compiler failed
			throw new IllegalArgumentException("INTERLIS compiler failed");
		}
		if(formatMode==MODE_XTF){
			fmeFeatureTypev=ModelUtility.getXtfTransferViewables(iliTd,inheritanceMapping);
			tag2class=ch.interlis.ili2c.generator.XSDGenerator.getTagMap(iliTd);
		}else{
			fmeFeatureTypev=ModelUtility.getItfTransferViewables(iliTd);
			tag2class=ch.interlis.iom_j.itf.ModelUtilities.getTagMap(iliTd);
		}
	}
	// Terminate the writer mid-stream.  Any special actions to shut
	// down a writer not finished writing data should be taken in this
	// method.  For most writers, nothing will be done.
	public void abort() throws Exception {
		try {
			myabort();
		} catch (Exception ex) {
			EhiLogger.logError(ex);
			throw ex;
		}
	}
	private void myabort() throws Exception {
		cleanup();
	}
	// Close the writer after it has completed writing out all the features.
	public void close() throws Exception {
		try {
			myclose();
		} catch (Exception ex) {
			EhiLogger.logError(ex);
			throw ex;
		}
	}
	private void myclose() throws Exception {
		if(outputFile!=null){
			if(iliTd==null && modelsFromFME.isEmpty()){
				// no data and should use model name from data
				// shortcut:
				// - don't compile models
				// - write nothing
			}else{
				// data arrived in the writer
				// use model name from data?
				if(iliTd==null){
					// use model name from data
					ArrayList iliModelv=new ArrayList();
					iliModelv.addAll(modelsFromFME);
					setupModel(iliModelv, modeldirv);
				}
				if(formatMode==MODE_XTF){
					ioxWriter=new ch.interlis.iom_j.xtf.XtfWriter(outputFile,iliTd);
				}else{
					ioxWriter=new ch.interlis.iom_j.itf.ItfWriter(outputFile,iliTd);
				}
				ioxWriter.write(new ch.interlis.iox_j.StartTransferEvent("ili2fme-"+Main.getVersion(),null,null));
				if(formatMode==MODE_XTF){
					// write each basket (feature collection)
					writeXtfBuffers();
				}else{
					writeItfBuffers();
				}
				ioxWriter.write(new ch.interlis.iox_j.EndTransferEvent());
				ioxWriter.flush();
				ioxWriter.close();
				ioxWriter=null;
			}
		}
		if(listener!=null && listener.hasSeenErrors()){
			throw new Exception("INTERLIS 2 writer failed");
		}
		cleanup();
	}
	private void writeXtfBuffers() throws IoxException,Exception {
		if(basketv!=null){
			Iterator basketi=basketv.keySet().iterator();
			while(basketi.hasNext()){
				String basketId=(String)basketi.next();
				ch.interlis.iox_j.StartBasketEvent basketEvent=(ch.interlis.iox_j.StartBasketEvent)basketv.get(basketId);
				if(autoXtfBaskets){
					// auto generate BID
					basketEvent.setBid(Integer.toString(++maxTid));
				}
				EhiLogger.logState(basketEvent.getType()+" "+basketEvent.getBid()+"...");
				ioxWriter.write(basketEvent);
				COM.safe.fmeobjects.IFMEFeatureVectorOnDisk featurev;
				writeBasket(basketId);
				ioxWriter.write(new ch.interlis.iox_j.EndBasketEvent());
			}
		}
	}
	private void writeBasket(String bufferKey) throws Exception, IoxException {
		//EhiLogger.debug("bufferKey "+bufferKey);
		COM.safe.fmeobjects.IFMEFeatureVectorOnDisk featurev=getFeatureBuffer(bufferKey);
		int featurec=featurev.entries();
		// has basket features?
		if(featurec>0){
			for(int featurei=0;featurei<featurec;featurei++){
				IFMEFeature feature=featurev.getAt(featurei);
				IomObject iomObj=mapFeature(feature,null,null,null);
				if(iomObj==null){
					throw new IllegalStateException("iomObj==null with feature "+feature.toString());
				}
				ioxWriter.write(new ch.interlis.iox_j.ObjectEvent(iomObj));
				feature.dispose();
			}
		}
		// do not dispose featurev here; because it is used again when writing the line table of SURFACEs to ITF
		// dispose it in cleanup()
	}
	private void writeItfLineTableArea(String mainTableName,String lineTableName) throws Exception, IoxException {
		//EhiLogger.debug("bufferKey "+bufferKey);
		COM.safe.fmeobjects.IFMEFeatureVectorOnDisk featurev=getFeatureBuffer(mainTableName);
		int featurec=featurev.entries();
		// has basket features?
		if(featurec>0){
			
			// setup pipeline
			IFMEFactoryPipeline lineTableBuilder=null;
			lineTableBuilder=session.createFactoryPipeline(mainTableName,null);
			String factory=null;
			
			factory="FACTORY_DEF * IntersectionFactory "
				+" INPUT FEATURE_TYPE *"
				+" IGNORE_NODE_HEIGHTS yes"
				+" OUTPUT SEGMENT  FEATURE_TYPE "+lineTableName
				;
			EhiLogger.traceState("factory "+factory);
			lineTableBuilder.addFactory(factory," ");
			
			// feed all features to pipeline
			for(int featurei=0;featurei<featurec;featurei++){
				IFMEFeature feature=featurev.getAt(featurei);
				lineTableBuilder.processFeature(feature);
				feature.dispose();
			}
			
			// process pipeline
			lineTableBuilder.allDone();
			
			// read all features from pipeline			
			ViewableWrapper wrapper=(ViewableWrapper)fmeFeatureTypev.get(lineTableName);
			AttributeDef attr=wrapper.getGeomAttr4FME();
			String attrName=attr.getName();
			Type type = attr.getDomainResolvingAliases();
			IFMEFeature feature=session.createFeature();
			while(lineTableBuilder.getOutputFeature(feature)){
				// process feature
				IomObject iomObj=new ch.interlis.iom_j.Iom_jObject(lineTableName,Integer.toString(++maxTid));	
				mapItfPolylineValueOfLineTable(feature, iomObj, wrapper, type, attrName);
				// add line attributes
				//SurfaceOrAreaType surfaceType=(SurfaceOrAreaType)type;
				//addItfLineAttributes(iomObj, feature, wrapper, surfaceType);
				ioxWriter.write(new ch.interlis.iox_j.ObjectEvent(iomObj));
			}
			feature.dispose();
			
			// cleanup
			lineTableBuilder.dispose();
		}
		
	}
	private void writeItfLineTableSurface(String mainTableName,String lineTableName) throws Exception, IoxException {
		//EhiLogger.debug("bufferKey "+bufferKey);
		COM.safe.fmeobjects.IFMEFeatureVectorOnDisk featurev=getFeatureBuffer(mainTableName);
		int featurec=featurev.entries();
		// has basket features?
		if(featurec>0){
			
			ViewableWrapper wrapper=(ViewableWrapper)fmeFeatureTypev.get(lineTableName);
			AttributeDef attr=wrapper.getGeomAttr4FME();
			String attrName=attr.getName();
			Type type = attr.getDomainResolvingAliases();
			
			for(int featurei=0;featurei<featurec;featurei++){
				IFMEFeature feature=featurev.getAt(featurei);
				
				// process feature
				
				boolean is3D=3==((CoordType)((SurfaceOrAreaType)type).getControlPointDomain().getType()).getDimensions().length;
				if(doRichGeometry){
					IFMEGeometry fmeGeom=null;
					fmeGeom=feature.getGeometry();
					if(fmeGeom instanceof IFMEDonut){
						IFMEDonut fmeDonut=(IFMEDonut)fmeGeom;
						// shell
						{
							IomObject iomObj=new ch.interlis.iom_j.Iom_jObject(lineTableName,Integer.toString(++maxTid));	

							//add ref to main table
							String fkName=wrapper.getGeomAttr4FME().getContainer().getName();
							IomObject structvalue=iomObj.addattrobj(fkName,"REF");
							structvalue.setobjectrefoid(getStringAttribute(feature,Main.XTF_ID));
							
							IFMECurve shell=null;
							shell=fmeDonut.getOuterBoundaryAsCurve();
							IomObject polyline=Fme2iox.FME2polyline(session,shell);
							iomObj.addattrobj(attrName,polyline);
							
							// add line attributes
							//SurfaceOrAreaType surfaceType=(SurfaceOrAreaType)type;
							//addItfLineAttributes(iomObj, shell, wrapper, surfaceType);
							ioxWriter.write(new ch.interlis.iox_j.ObjectEvent(iomObj));
						}
						
						// holes
						int holec=fmeDonut.numInnerBoundaries();
						for(int holei=0;holei<holec;holei++){
							IomObject iomObj=new ch.interlis.iom_j.Iom_jObject(lineTableName,Integer.toString(++maxTid));	

							//add ref to main table
							String fkName=wrapper.getGeomAttr4FME().getContainer().getName();
							IomObject structvalue=iomObj.addattrobj(fkName,"REF");
							structvalue.setobjectrefoid(getStringAttribute(feature,Main.XTF_ID));
							
							IFMECurve hole=null;
							hole=fmeDonut.getInnerBoundaryAsCurveAt(holei);
							IomObject polyline=Fme2iox.FME2polyline(session,hole);
							iomObj.addattrobj(attrName,polyline);
							
							// add line attributes
							//SurfaceOrAreaType surfaceType=(SurfaceOrAreaType)type;
							//addItfLineAttributes(iomObj, hole, wrapper, surfaceType);
							ioxWriter.write(new ch.interlis.iox_j.ObjectEvent(iomObj));
						}
					}else if(fmeGeom instanceof IFMESimpleArea){
						IomObject iomObj=new ch.interlis.iom_j.Iom_jObject(lineTableName,Integer.toString(++maxTid));	

						//add ref to main table
						String fkName=wrapper.getGeomAttr4FME().getContainer().getName();
						IomObject structvalue=iomObj.addattrobj(fkName,"REF");
						structvalue.setobjectrefoid(getStringAttribute(feature,Main.XTF_ID));
						IomObject polyline=Fme2iox.FME2polyline(session,((IFMESimpleArea)fmeGeom).getBoundaryAsCurve());
						iomObj.addattrobj(attrName,polyline);
						
						// add line attributes
						//SurfaceOrAreaType surfaceType=(SurfaceOrAreaType)type;
						//addItfLineAttributes(iomObj, fmeGeom, wrapper, surfaceType);
						ioxWriter.write(new ch.interlis.iox_j.ObjectEvent(iomObj));
					}else if(fmeGeom instanceof IFMENull){
						// skip it
					}else{
						feature.performFunction("@Log()");
						throw new DataException("unexpected geometry type "+fmeGeom.getClass().getName());
					}
				}else{
					throw new java.lang.IllegalStateException("use RichGeometry");
					//obj.getDonutParts(bndries);
					// write parts as polyline
				}
				feature.dispose();
			}
						
		}
		
	}
	private void addItfLineAttributes(IomObject iomObj, IFMEFeature feature, ViewableWrapper wrapper, SurfaceOrAreaType surfaceType) throws Exception, DataException, FMEException, Iox2jtsException {
		Table lineAttrTable=surfaceType.getLineAttributeStructure();
		if(lineAttrTable!=null){
		    Iterator attri = lineAttrTable.getAttributes ();
		    while(attri.hasNext()){
		    	AttributeDef lineattr=(AttributeDef)attri.next();
				mapAttributeValue(feature, null, iomObj, wrapper, null, lineattr);
		    }
		}
	}
	private void writeItfBuffers() throws IoxException,Exception
	{
		Iterator modeli = iliTd.iterator();
		int topicNr=0;
		while (modeli.hasNext()) {
			Object mObj = modeli.next();
			if (mObj instanceof Model) {
				Model model = (Model) mObj;
				if (model instanceof TypeModel) {
					continue;
				}
				if (model instanceof PredefinedModel) {
					continue;
				}
				Iterator topici = model.iterator();
				while (topici.hasNext()) {
					Object tObj = topici.next();
					if (tObj instanceof Topic) {
						Topic topic = (Topic) tObj;
						// StartBasket
						topicNr++;
						StartBasketEvent basketEvent=new ch.interlis.iox_j.StartBasketEvent(topic.getScopedName(null),Integer.toString(topicNr));
						ioxWriter.write(basketEvent);
						Iterator iter = topic.getViewables().iterator();
						while (iter.hasNext()) {
							Object obj = iter.next();
							if (obj instanceof Viewable) {
								Viewable v = (Viewable) obj;
								if((v instanceof Table) && !((Table)v).isIdentifiable()){
									// STRUCTURE
									continue;
								}
								if(ModelUtility.isPureRefAssoc(v)){
									continue;
								}
								Iterator attri =null;
								String className = v.getScopedName(null);
								if(useLineTableFeatures){
									// add helper tables of area attributes
									attri = v.getAttributes();
									while (attri.hasNext()) {
										Object attrObj = attri.next();
										if (attrObj instanceof AttributeDef) {
											AttributeDef attr = (AttributeDef) attrObj;
											Type type = Type.findReal(attr.getDomain());
											if (type instanceof AreaType) {
												String helperTableName =
													v.getContainer().getScopedName(
														null)
														+ "."
														+ v.getName()
														+ "_"
														+ attr.getName();
												// area helper table
												EhiLogger.logState(helperTableName+"...");
												writeBasket(helperTableName);
											}
										}
									}
									
									// main table
									EhiLogger.logState(className+"...");
									writeBasket(className);

									// add helper tables of surface attributes
									attri = v.getAttributes();
									while (attri.hasNext()) {
										Object attrObj = attri.next();
										if (attrObj instanceof AttributeDef) {
											AttributeDef attr = (AttributeDef) attrObj;
											Type type = Type.findReal(attr.getDomain());
											if (type instanceof SurfaceType) {
												String helperTableName =
													v.getContainer().getScopedName(
														null)
														+ "."
														+ v.getName()
														+ "_"
														+ attr.getName();
												// surface helper table
												EhiLogger.logState(helperTableName+"...");
												writeBasket(helperTableName);
											}
										}
									}
								}else{
									ViewableWrapper wrapper=(ViewableWrapper)fmeFeatureTypev.get(className);
									
									if(wrapper.getGeomAttr4FME()!=null && wrapper.getGeomAttr4FME().getDomainResolvingAliases() instanceof AreaType){
										// build line table from polygons/donuts
										String lineTableName =
											v.getContainer().getScopedName(
												null)
												+ "."
												+ v.getName()
												+ "_"
												+ wrapper.getGeomAttr4FME().getName();
										EhiLogger.logState(lineTableName+"...");
										writeItfLineTableArea(className,lineTableName);
									}
									
									// main table
									EhiLogger.logState(className+"...");
									writeBasket(className);
									
									if(wrapper.getGeomAttr4FME()!=null && wrapper.getGeomAttr4FME().getDomainResolvingAliases() instanceof SurfaceType){
										// build line table from polygons/donuts
										String lineTableName =
											v.getContainer().getScopedName(
												null)
												+ "."
												+ v.getName()
												+ "_"
												+ wrapper.getGeomAttr4FME().getName();
										writeItfLineTableSurface(className,lineTableName);
									}
								}
								
							}
						}
						// EndBasket
						ioxWriter.write(new ch.interlis.iox_j.EndBasketEvent());
					}
				}
			}
		}
	}
	public int id() {
		// All Java writers return 0.
		return 0;
	}
	// Write the next feature to a file.
	public void write(IFMEFeature obj) 
	throws Exception 
	{
		try{
			// special feature type with basket metadata
			if(obj.getFeatureType().equals(Main.XTF_BASKETS)){
				//EhiLogger.debug("topic <"+getStringAttribute(obj,Main.XTF_TOPIC)+">, id <"+getStringAttribute(obj,Main.XTF_ID)+">");
				mapBasket(obj);
			}else{
				//EhiLogger.debug("class <"+getStringAttribute(obj,Main.XTF_CLASS)+">, id <"+getStringAttribute(obj,Main.XTF_ID)+">");
				//mapFeature(obj,null,null,null);
				bufferFeature(obj);
			}
		}catch(Exception ex){
			EhiLogger.logError(ex);
			throw ex;
		}
	}
	private void bufferFeature(IFMEFeature obj)
	throws Exception 
	{
		String bufferKey=null;
		String fmeRecInfo=obj.getFeatureType();
		String tag=null;
		if(obj.attributeExists(Main.XTF_CLASS)){
			tag=getStringAttribute(obj,Main.XTF_CLASS);
		}
		if(tag==null || tag.length()==0){
			tag=obj.getFeatureType();
		}
		if(!tag.equals(Main.XTF_DELETEOBJECT)){
			String iliModel=tag.substring(0,tag.indexOf('.'));
			if(!modelsFromFME.contains(iliModel)){
				modelsFromFME.add(iliModel);
			}
			// keep track of max TID
			if(obj.attributeExists(Main.XTF_ID)){
				String tid=obj.getStringAttribute(Main.XTF_ID);
				if(tid!=null && tid.length()>0){
					try {
						int tidInt=Integer.parseInt(tid);
						if(tidInt>maxTid){
							maxTid=tidInt;
						}
					} catch (NumberFormatException e) {
						// ignore it
					}
				}
			}
		}
		if(formatMode==MODE_XTF){
			if(scanXtfBaskets){
				scanXtfBaskets=false;
				if(obj.attributeExists(Main.XTF_BASKET) || tag.equals(Main.XTF_DELETEOBJECT)){
					autoXtfBaskets=false;
				}else{
					autoXtfBaskets=true;
				}
			}else{
				if(autoXtfBaskets){
					if(obj.attributeExists(Main.XTF_BASKET)){
						String err=fmeRecInfo+": no "+Main.XTF_BASKET+" attribute in auto XTF baskes mode allowed";
						throw new ConfigException(err);
					}
				}
			}
			if(autoXtfBaskets){
				// use qualified topic name as buffer name
				bufferKey=tag.substring(0,tag.lastIndexOf('.'));
				if(basketv==null){
					basketv=new HashMap();
				}
				String topic=bufferKey;
				String basketId=bufferKey;
				if(!basketv.containsKey(basketId)){
					ch.interlis.iox_j.StartBasketEvent basket=new ch.interlis.iox_j.StartBasketEvent(topic,basketId);
					basketv.put(basketId,basket);
				}
			}else{
				if(!obj.attributeExists(Main.XTF_BASKET)){
					String err=fmeRecInfo+": missing mandatory attribute "+Main.XTF_BASKET;
					EhiLogger.logError(err);
					throw new Exception(err);
				}
				// use basket-id as buffername
				bufferKey=obj.getStringAttribute(Main.XTF_BASKET);
				if(bufferKey==null || bufferKey.length()==0){
					String err=fmeRecInfo+": missing mandatory attribute "+Main.XTF_BASKET;
					EhiLogger.logError(err);
					throw new Exception(err);
				}
			}
		}else{
			// use qualified ili1TableName as buffername
			bufferKey=tag;
			//EhiLogger.debug("bufferKey "+bufferKey);
		}
		COM.safe.fmeobjects.IFMEFeatureVectorOnDisk featurev=getFeatureBuffer(bufferKey);
		featurev.append(obj);
	}
	private void mapBasket(IFMEFeature obj)
	throws Exception 
	{
		if(formatMode==MODE_XTF){
			if(scanXtfBaskets){
				// stop scanning for XTF_BASKETS/xtf_basket
				scanXtfBaskets=false;
				// disable auto XTF basket mode
				autoXtfBaskets=false;
			}else{
				if(autoXtfBaskets){
					throw new ConfigException("no XTF_BASKETS feature in auto XTF baskes mode allowed");
				}
			}
			if(basketv==null){
				basketv=new HashMap();
			}
			String topic=obj.getStringAttribute(Main.XTF_TOPIC);
			String basketId=obj.getStringAttribute(Main.XTF_ID);
			if(basketv.containsKey(basketId)){
				EhiLogger.logAdaption("dupliacte basket id "+basketId+"; ignored");
			}else{
				ch.interlis.iox_j.StartBasketEvent basket=new ch.interlis.iox_j.StartBasketEvent(topic,basketId);
				String startState=null;
				if(obj.attributeExists(Main.XTF_STARTSTATE)){
					startState=ch.ehi.basics.tools.StringUtility.purge(obj.getStringAttribute(Main.XTF_STARTSTATE));
				}
				String endState=null;
				if(obj.attributeExists(Main.XTF_ENDSTATE)){
					endState=ch.ehi.basics.tools.StringUtility.purge(obj.getStringAttribute(Main.XTF_ENDSTATE));
				}
				basket.setKind(IomConstants.IOM_FULL);
				if(endState!=null){
					basket.setEndstate(endState);
					if(startState==null){
						basket.setKind(IomConstants.IOM_INITIAL);
					}else{
						basket.setStartstate(startState);
						basket.setKind(IomConstants.IOM_UPDATE);
					}
				}
				if(obj.attributeExists(Main.XTF_CONSISTENCY)){
					int consistency=FmeUtility.mapFme2IoxConsistency(obj.getStringAttribute(Main.XTF_CONSISTENCY));
					basket.setConsistency(consistency);
				}
				basketv.put(basketId,basket);
			}
		}
	}
	private String getStringAttribute(IFMEFeature obj,String attr)
	throws COM.safe.fmeobjects.FMEException
	{
		//EhiLogger.debug(attr);
		return obj.getStringAttribute(attr);
	}
	private IomObject mapFeature(IFMEFeature obj,String fmeListAttrPrefix,IomObject iliStructParent,String iliStructAttrName)
	throws Exception 
	{
		boolean isStructEle=fmeListAttrPrefix!=null;
		String attrPrefix="";
		String fmeFeatureType=obj.getFeatureType();
		String fmeRecInfo=fmeFeatureType;
		if(fmeFeatureType.equals(Main.XTF_DELETEOBJECT)){
			if(!obj.attributeExists(Main.XTF_ID)){
				String err=fmeRecInfo+": missing mandatory attribute "+Main.XTF_ID;
				EhiLogger.logError(err);
				throw new Exception(err);
			}
			String tid=getStringAttribute(obj,Main.XTF_ID);
			if(tid==null || tid.length()==0){
				String err=fmeRecInfo+": missing mandatory attribute "+Main.XTF_ID;
				EhiLogger.logError(err);
				throw new Exception(err);
			}
			//EhiLogger.debug("tag "+tag+", tid "+tid);
			IomObject iomObj=new ch.interlis.iom_j.Iom_jObject(Main.DELETE_TAG,tid);	
			return iomObj;
		}
		if(isStructEle){
			fmeRecInfo = fmeRecInfo+" "+fmeListAttrPrefix;
			attrPrefix=fmeListAttrPrefix+".";
		}
		String tag=null;
		if(isStructEle){
			if(!obj.attributeExists(attrPrefix+Main.XTF_CLASS)){
				String err=fmeRecInfo+": missing mandatory attribute "+attrPrefix+Main.XTF_CLASS;
				EhiLogger.logError(err);
				throw new Exception(err);
			}
			tag=getStringAttribute(obj,attrPrefix+Main.XTF_CLASS);
			if(tag==null || tag.length()==0){
				String err=fmeRecInfo+": missing mandatory attribute "+attrPrefix+Main.XTF_CLASS;
				EhiLogger.logError(err);
				throw new Exception(err);
			}
		}else{
			if(obj.attributeExists(attrPrefix+Main.XTF_CLASS)){
				tag=getStringAttribute(obj,attrPrefix+Main.XTF_CLASS);
			}
			if(tag==null || tag.length()==0){
				tag=fmeFeatureType;
			}
		}
		IomObject iomObj=null;
		if(!isStructEle){
			if(!obj.attributeExists(Main.XTF_ID)){
				String err=fmeRecInfo+": missing mandatory attribute "+Main.XTF_ID;
				EhiLogger.logError(err);
				throw new Exception(err);
			}
			String tid=getStringAttribute(obj,Main.XTF_ID);
			if(tid==null || tid.length()==0){
				String err=fmeRecInfo+": missing mandatory attribute "+Main.XTF_ID;
				EhiLogger.logError(err);
				throw new Exception(err);
			}
			//EhiLogger.debug("tag "+tag+", tid "+tid);
			iomObj=new ch.interlis.iom_j.Iom_jObject(tag,tid);	
		}else{
			iomObj=iliStructParent.addattrobj(iliStructAttrName,tag);	
		}
		ViewableWrapper wrapper=(ViewableWrapper)fmeFeatureTypev.get(tag);
		if(wrapper==null){
			String err=fmeRecInfo+": unknown interlis class <"+tag+">";
			EhiLogger.logError(err);
			throw new Exception(err);
			
		}
		if(formatMode==MODE_ITF){
			// if SURFACE helper table
			if(wrapper.isHelper() && wrapper.getGeomAttr4FME().getDomainResolvingAliases() instanceof SurfaceType){
				//add ref to main table
				String fkName=wrapper.getGeomAttr4FME().getContainer().getName();
				IomObject structvalue=iomObj.addattrobj(fkName,"REF");
				structvalue.setobjectrefoid(getStringAttribute(obj,fkName));
			}
		}
		if(formatMode==MODE_XTF){
			if(obj.attributeExists(Main.XTF_CONSISTENCY)){
				int consistency=FmeUtility.mapFme2IoxConsistency(getStringAttribute(obj, Main.XTF_CONSISTENCY));
				iomObj.setobjectconsistency(consistency);
			}
			if(obj.attributeExists(Main.XTF_OPERATION)){
				int operation=FmeUtility.mapFme2IoxOperation(getStringAttribute(obj, Main.XTF_OPERATION));
				iomObj.setobjectoperation(operation);
			}
		}
		
		Iterator iter;
		if(formatMode==MODE_ITF){
			iter = wrapper.getAttrIterator();
		}else{
			Viewable aclass=(Viewable)tag2class.get(tag);
			iter = aclass.getAttributesAndRoles2();
		}

		String geomattr=null;
		if(!isStructEle){
			if(obj.attributeExists(Main.XTF_GEOMATTR)){
				geomattr=StringUtility.purge(getStringAttribute(obj,Main.XTF_GEOMATTR));
			}
			if(geomattr==null){
				AttributeDef attr=wrapper.getGeomAttr4FME();
				if(attr!=null){
					geomattr=attr.getName();
				}
			}
			//EhiLogger.debug("geomattr <"+geomattr+">");
		}
		while (iter.hasNext()) {
			ViewableTransferElement prop = (ViewableTransferElement)iter.next();
			if (prop.obj instanceof AttributeDef) {
				AttributeDef attr = (AttributeDef) prop.obj;
				mapAttributeValue(obj, attrPrefix, iomObj, wrapper, geomattr, attr);
			}
			if(prop.obj instanceof RoleDef){
				RoleDef role = (RoleDef) prop.obj;
				String roleName=role.getName();
				// a role of an embedded association?
				if(prop.embedded){
					AssociationDef roleOwner = (AssociationDef) role.getContainer();
					if(roleOwner.getDerivedFrom()==null){
						// not just a link?
						if (roleOwner.getAttributes().hasNext()
							|| roleOwner.getLightweightAssociations().iterator().hasNext()) {
							 // TODO add association attributes
						}
						if(obj.attributeExists(roleName)){
							String refoid=getStringAttribute(obj,roleName);
							IomObject structvalue=iomObj.addattrobj(roleName,"REF");
							 structvalue.setobjectrefoid(refoid);
							 if(role.isOrdered()){
								long orderPos=obj.getIntAttribute(roleName+"."+Main.ORDERPOS);
								structvalue.setobjectreforderpos(orderPos);
							 }
						}
					}
				}else{
					String refoid=getStringAttribute(obj,roleName);
					IomObject structvalue=iomObj.addattrobj(roleName,"REF");
					 structvalue.setobjectrefoid(refoid);
					 if(role.isOrdered()){
						long orderPos=obj.getIntAttribute(roleName+"."+Main.ORDERPOS);
						structvalue.setobjectreforderpos(orderPos);
					 }
				}
			}
		}
		return iomObj;
			
	}
	private void mapAttributeValue(IFMEFeature obj, String attrPrefix, IomObject iomObj, ViewableWrapper wrapper, String geomattr, AttributeDef attr) 
		throws Exception, DataException, FMEException, Iox2jtsException 
	{
		Type type = attr.getDomainResolvingAliases();
		 String attrName=attr.getName();
		 if(attrPrefix==null){
			 attrPrefix="";
		 }
		if (type instanceof CompositionType){
			int elei=0;
			String prefix=attrPrefix+attrName+"{"+Integer.toString(elei)+"}";
			while(obj.attributeExists(prefix+"."+Main.XTF_CLASS)){
				// struct element
				//EhiLogger.debug("struct "+prefix);
				mapFeature(obj,prefix,iomObj,attrName);						
				elei++;
				prefix=attrPrefix+attrName+"{"+Integer.toString(elei)+"}";
			}
		}else if (type instanceof PolylineType){
			if(geomattr!=null && attrName.equals(geomattr)){
				boolean is3D=3==((CoordType)((PolylineType)type).getControlPointDomain().getType()).getDimensions().length;
				if(doRichGeometry){
					IFMEGeometry fmeGeom=null;
					try{
						fmeGeom=obj.getGeometry();
						if(fmeGeom instanceof IFMECurve){
							IomObject polyline=Fme2iox.FME2polyline(session,(IFMECurve)fmeGeom);
							iomObj.addattrobj(attrName,polyline);
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
				}else{
					mapClassicPolyline(obj, iomObj, attrName, is3D);
				}
			}else{
				if(obj.attributeExists(attrPrefix+attrName)){
					String value=getStringAttribute(obj,attrPrefix+attrName);
					if(value!=null && value.length()>0){
						iomObj.addattrobj(attrName, Jts2iox.hexwkb2polyline(value));
					}
				}else{
				}
			}
		 }else if(type instanceof SurfaceOrAreaType){
		 	if(formatMode==MODE_XTF){
				if(geomattr!=null && attrName.equals(geomattr)){
					if(doRichGeometry){
						IFMEGeometry fmeGeom=null;
						try{
							fmeGeom=obj.getGeometry();
							if(fmeGeom instanceof IFMEArea){
								IomObject surface=Fme2iox.FME2surface(session,(IFMEArea)fmeGeom);
								iomObj.addattrobj(attrName,surface);
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
					}else{
						mapClassicSurface(obj, iomObj, type, attrName);
					}
				}else{
					if(obj.attributeExists(attrPrefix+attrName)){
						String value=getStringAttribute(obj,attrPrefix+attrName);
						if(value!=null && value.length()>0){
							iomObj.addattrobj(attrName, Jts2iox.hexwkb2surface(value));
						}
					}else{
					}
					
				}
		 	}else{
		 		// formatMode==MODE_ITF
				// if helper table
				if(wrapper.isHelper()){
					mapItfPolylineValueOfLineTable(obj, iomObj, wrapper, type, attrName);
					// add line attributes
					SurfaceOrAreaType surfaceType=(SurfaceOrAreaType)type;
					addItfLineAttributes(iomObj, obj, wrapper, surfaceType);
				}else{
					// main table
					if(type instanceof AreaType){
						if(geomattr!=null && attrName.equals(geomattr)){
							if(doRichGeometry){
								IFMEGeometry fmeGeom=null;
								try{
									fmeGeom=obj.getGeometry();
									if(fmeGeom instanceof IFMEPoint){
										IomObject coord=Fme2iox.FME2coord((IFMEPoint)fmeGeom);
										iomObj.addattrobj(attrName,coord);
									}else if(fmeGeom instanceof IFMENull){
										// skip it
									}else if(fmeGeom instanceof IFMEArea){
										double valuev[]=obj.generatePointInPolygon(true);
										IomObject coord=iomObj.addattrobj(attrName,"COORD");
										coord.setattrvalue("C1",Double.toString(valuev[0]));
										coord.setattrvalue("C2",Double.toString(valuev[1]));						
									}else{
										throw new DataException("unexpected geometry type "+fmeGeom.getClass().getName());
									}
								}finally{
									if(fmeGeom!=null){
										fmeGeom.dispose();
										fmeGeom=null;
									}
								}
							}else{
								double valuev[]=obj.get3DCoordinate(0);
								IomObject coord=iomObj.addattrobj(attrName,"COORD");
								coord.setattrvalue("C1",Double.toString(valuev[0]));
								coord.setattrvalue("C2",Double.toString(valuev[1]));						
							}
						}
					}
				}
		 	}
		 }else if(type instanceof CoordType){
			if(geomattr!=null && attrName.equals(geomattr)){
				if(doRichGeometry){
					IFMEGeometry fmeGeom=null;
					try{
						fmeGeom=obj.getGeometry();
						if(fmeGeom instanceof IFMEPoint){
							IomObject coord=Fme2iox.FME2coord((IFMEPoint)fmeGeom);
							iomObj.addattrobj(attrName,coord);
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
				}else{
					double valuev[]=obj.get3DCoordinate(0);
					IomObject coord=iomObj.addattrobj(attrName,"COORD");
					coord.setattrvalue("C1",Double.toString(valuev[0]));
					coord.setattrvalue("C2",Double.toString(valuev[1]));
					if(((CoordType)type).getDimensions().length==3){
						coord.setattrvalue("C3",Double.toString(valuev[2]));
					}
				}
				
			}else{
				if(obj.attributeExists(attrPrefix+attrName)){
					String value=getStringAttribute(obj,attrPrefix+attrName);
					if(value!=null && value.length()>0){
						iomObj.addattrobj(attrName, Jts2iox.hexwkb2coord(value));
					}
				}
			}
		}else{
			if(obj.attributeExists(attrPrefix+attrName)){
				String value=getStringAttribute(obj,attrPrefix+attrName);
				if(value!=null && value.length()>0){
					iomObj.setattrvalue(attrName, value);
				}
			}
		}
	}
	private void mapItfPolylineValueOfLineTable(IFMEFeature obj, IomObject iomObj, ViewableWrapper wrapper, Type type, String attrName) throws DataException, FMEException, Exception, Iox2jtsException {
		boolean is3D=3==((CoordType)((SurfaceOrAreaType)type).getControlPointDomain().getType()).getDimensions().length;
		if(doRichGeometry){
			IFMEGeometry fmeGeom=null;
			fmeGeom=obj.getGeometry();
			if(fmeGeom instanceof IFMECurve){
				IomObject polyline=Fme2iox.FME2polyline(session,(IFMECurve)fmeGeom);
				iomObj.addattrobj(attrName,polyline);
			}else if(fmeGeom instanceof IFMESimpleArea){
				IomObject polyline=Fme2iox.FME2polyline(session,((IFMESimpleArea)fmeGeom).getBoundaryAsCurve());
				iomObj.addattrobj(attrName,polyline);
			}else{
				obj.performFunction("@Log()");
				throw new DataException("unexpected geometry type "+fmeGeom.getClass().getName());
			}
			fmeGeom.dispose();
		}else{
			mapClassicPolyline(obj, iomObj, attrName, is3D);
		}
	}
	private void mapClassicPolyline(
		IFMEFeature obj,
		IomObject iomObj,
		String attrName,
		boolean is3D)
		throws FMEException {
		IomObject polylineValue=iomObj.addattrobj(attrName,"POLYLINE");
		// unclipped polyline, add one sequence
		IomObject sequence=polylineValue.addattrobj("sequence","SEGMENTS");
		int segc=obj.numCoords();
		for(int segi=0;segi<segc;segi++){
			// add control point
			double valuev[]=obj.get3DCoordinate(segi);
			IomObject coordValue=sequence.addattrobj("segment","COORD");
			coordValue.setattrvalue("C1",Double.toString(valuev[0]));
			coordValue.setattrvalue("C2",Double.toString(valuev[1]));
			if(is3D){
				coordValue.setattrvalue("C3",Double.toString(valuev[2]));
			}
			//coordValue.setattrvalue("A1",Double.toString(ordv[i]));
			//coordValue.setattrvalue("A2",Double.toString(ordv[i+1]));
			//if(is3D){
			//	// no A3 in XTF!
			//}
		}
	}
	private void mapClassicSurface(
		IFMEFeature obj,
		IomObject iomObj,
		Type type,
		String attrName)
		throws FMEException {
		boolean is3D=3==((CoordType)((SurfaceOrAreaType)type).getControlPointDomain().getType()).getDimensions().length;
		//fmeLog.logFeature(obj,IFMELogFile.FME_INFORM,-1);
		IFMEFeatureVector bndries=session.createFeatureVector();
		obj.getDonutParts(bndries);
		IomObject multisurface=iomObj.addattrobj(attrName,"MULTISURFACE");
		IomObject surface=multisurface.addattrobj("surface","SURFACE");
		int bndc=bndries.entries();
		//EhiLogger.debug("number of boundaries "+Integer.toString(bndc));
		for(int bndi=0;bndi<bndc;bndi++){
			IFMEFeature line=bndries.getAt(bndi);
			IomObject boundary=surface.addattrobj("boundary","BOUNDARY");
			IomObject polylineValue=boundary.addattrobj("polyline","POLYLINE");
			IomObject sequence=polylineValue.addattrobj("sequence","SEGMENTS");
			int segc=line.numCoords();
			for(int segi=0;segi<segc;segi++){
				// add control point
				double valuev[]=line.get3DCoordinate(segi);
				IomObject coordValue=sequence.addattrobj("segment","COORD");
				coordValue.setattrvalue("C1",Double.toString(valuev[0]));
				coordValue.setattrvalue("C2",Double.toString(valuev[1]));
				if(is3D){
					coordValue.setattrvalue("C3",Double.toString(valuev[2]));
				}
				//coordValue.setattrvalue("A1",Double.toString(ordv[i]));
				//coordValue.setattrvalue("A2",Double.toString(ordv[i+1]));
				//if(is3D){
				//	// no A3 in XTF!
				//}
			}
		}
	}
	/** get the feature buffer associated with the given key.
	 * @param bufferKey basketId or qualifiedIli1TableName
	 * @return feature buffer
	 */
	private COM.safe.fmeobjects.IFMEFeatureVectorOnDisk getFeatureBuffer(String bufferKey)
	{
		//EhiLogger.debug("bufferKey "+bufferKey);

		if(featurebufferv.containsKey(bufferKey)){
			return (COM.safe.fmeobjects.IFMEFeatureVectorOnDisk)featurebufferv.get(bufferKey);
		}
		COM.safe.fmeobjects.IFMEFeatureVectorOnDisk ret=session.createFeatureVectorOnDisk(1000);
		featurebufferv.put(bufferKey,ret);
		return ret;
	}

	public boolean multiFileWriter() {
		// don't create a new object for every classdef
		return false;
	}
	public void startTransaction() throws Exception {
		//For formats or systems which do not have the notion of a transaction then nothing needs to be done.
	}
	public void commitTransaction() throws Exception {
		//For formats or systems which do not have the notion of a transaction then nothing needs to be done.
	}
	public void rollbackTransaction() throws Exception {
		//For formats or systems which do not have the notion of a transaction then nothing needs to be done.
	}
	private FmeLogListener listener=null;
	private void cleanup(){
		// free buffers
		if(featurebufferv!=null){
			Iterator bufi=featurebufferv.values().iterator();
			while(bufi.hasNext()){
				COM.safe.fmeobjects.IFMEFeatureVectorOnDisk buf=(COM.safe.fmeobjects.IFMEFeatureVectorOnDisk)bufi.next();
				buf.dispose();
				buf=null;
			}
			featurebufferv=null;
		}

		// close writer
		if(ioxWriter!=null){
			try{
				ioxWriter.close();
			}catch(IoxException ex){
				EhiLogger.logError(ex);
			}
			ioxWriter=null;
		}
		
		// close file
		if(outputFile!=null){
			try {
				outputFile.close();
			}catch(java.io.IOException ex) {
				EhiLogger.logError(ex);
			}
			outputFile=null;
		}
		if(listener!=null){
			Main.endLogging();
			listener=null;
		}
	}
}