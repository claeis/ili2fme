package ch.interlis.ili2fme;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;
import COM.safe.fmeobjects.IFMELogFile;
import COM.safe.fmeobjects.IFMEPoint;
import COM.safe.fmeobjects.IFMESession;

public class DebugUtil {
	private static boolean patchingDone=true;
	public static void patchPoint(IFMELogFile logFile)
	{
		if(!patchingDone){
			ClassPool pool = ClassPool.getDefault();
			{
				CtClass cc=null;
				try {
					cc = pool.get("COM.safe.fmeobjects.FMEPoint");
					CtConstructor c1 = cc.getConstructor("(LCOM/safe/fmeobjects/FMESession;J)V");
					c1.insertBefore("{ ch.ehi.fme.Main.createPoint($0); }");
					CtMethod m = cc.getDeclaredMethod("dispose");
					m.insertBefore("{ ch.ehi.fme.Main.disposePoint($0); }");
					Class clazz = cc.toClass();
					logFile.logMessageString("FMEPoint patched",IFMELogFile.FME_INFORM);
				} catch (NotFoundException e) {
					logFile.logMessageString(e.toString(),IFMELogFile.FME_ERROR);
				} catch (CannotCompileException e) {
					logFile.logMessageString(e.toString(),IFMELogFile.FME_ERROR);
				}
			}
			patchingDone=true;
		}
		
	}
	private static java.util.HashMap points=new java.util.HashMap();
	public static void createPoint(IFMEPoint p)
	{
		points.put(p, null);
	}
	public static void disposePoint(IFMEPoint p)
	{
		points.remove(p);
	}
	public static void dumpPoints(IFMELogFile logFile)
	{
		java.util.Iterator it=points.keySet().iterator();
		while(it.hasNext()){
			IFMEPoint p=(IFMEPoint)it.next();
			logFile.logMessageString(p.toString(),IFMELogFile.FME_INFORM);
		}
	}
	public static void dumpObjects(IFMESession session1,IFMELogFile logFile)
	{
		logFile.logMessageString("dumpObjects()...",IFMELogFile.FME_INFORM);
		COM.safe.fmeobjects.FMESession session=(COM.safe.fmeobjects.FMESession)session1;
		if(session.dialogMap_.size()>0)dumpMap(logFile,session.dialogMap_);
		if(session.factoryPipelineMap_.size()>0)dumpMap(logFile,session.factoryPipelineMap_);
		if(session.featureMap_.size()>0)dumpMap(logFile,session.featureMap_);
		if(session.featureVectorMap_.size()>0)dumpMap(logFile,session.featureVectorMap_);
		if(session.featureVectorOnDiskMap_.size()>0)dumpMap(logFile,session.featureVectorOnDiskMap_);
		if(session.readerMap_.size()>0)dumpMap(logFile,session.readerMap_);
		if(session.rectangleMap_.size()>0)dumpMap(logFile,session.rectangleMap_);
		if(session.rectangleVectorMap_.size()>0)dumpMap(logFile,session.rectangleVectorMap_);
		if(session.reprojectorMap_.size()>0)dumpMap(logFile,session.reprojectorMap_);
		if(session.spatialIndexMap_.size()>0)dumpMap(logFile,session.spatialIndexMap_);
		if(session.stringArrayMap_.size()>0)dumpMap(logFile,session.stringArrayMap_);
		if(session.triangle3DListMap_.size()>0)dumpMap(logFile,session.triangle3DListMap_);
		if(session.triangle3DMap_.size()>0)dumpMap(logFile,session.triangle3DMap_);
		if(session.workspaceRunnerMap_.size()>0)dumpMap(logFile,session.workspaceRunnerMap_);
		if(session.writerMap_.size()>0)dumpMap(logFile,session.writerMap_);
		COM.safe.fmeobjects.FMEGeometryTools tools=(COM.safe.fmeobjects.FMEGeometryTools)session.geometryTools();
		java.lang.reflect.Field fieldv[]=tools.getClass().getFields();
		for(int fieldi=0;fieldi<fieldv.length;fieldi++){
			java.lang.reflect.Field field=fieldv[fieldi];
			if(field.getName().endsWith("Map_")){
				try {
					Object val=field.get(tools);
					if(val instanceof java.util.Map){
						java.util.Map map=(java.util.Map)val;
						if(map.size()>0){
							dumpMap(logFile,map);
						}
					}
				} catch (IllegalArgumentException e) {
					logFile.logMessageString(e.toString(),IFMELogFile.FME_ERROR);
				} catch (IllegalAccessException e) {
					logFile.logMessageString(e.toString(),IFMELogFile.FME_ERROR);
				}
			}
		}
	}
	public static void dumpMap(IFMELogFile logFile,java.util.Map map)
	{
		java.util.Iterator it=map.keySet().iterator();
		while(it.hasNext()){
			Object p=it.next();
			logFile.logMessageString(p.getClass().getName()+"@"+p.hashCode(),IFMELogFile.FME_INFORM);
		}
	}

}
