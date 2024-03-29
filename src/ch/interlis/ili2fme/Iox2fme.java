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

import ch.interlis.iom.IomConstants;
import ch.interlis.iom.IomObject;
import ch.interlis.iom_j.xtf.Ili2cUtility;
import COM.safe.fmeobjects.*;
import ch.ehi.basics.logging.EhiLogger;

/** Utility to convert from INTERLIS to FME geometry types.
 * @author ceis
 */
public class Iox2fme {
	private static boolean firstLineattrib=true;
	// utility, no instances
	private Iox2fme(){}
	/** Converts a COORD to a FME Point.
	 * @param session FME session
	 * @param value INTERLIS COORD structure.
	 * @return FMEPoint.
	 * @throws DataException
	 */
	public static IFMEPoint coord2FME(IFMESession session,IomObject value) 
	throws DataException 
	{
		if(value==null){
			return null;
		}
		String c1=value.getattrvalue("C1");
		String c2=value.getattrvalue("C2");
		String c3=value.getattrvalue("C3");
		double xCoord;
		try{
			xCoord = Double.parseDouble(c1);
		}catch(Exception ex){
			throw new DataException("failed to read C1 <"+c1+">",ex);
		}
		double yCoord;
		try{
			yCoord = Double.parseDouble(c2);
		}catch(Exception ex){
			throw new DataException("failed to read C2 <"+c2+">",ex);
		}
		IFMEPoint coord=null;
		IFMEGeometryTools tools=session.getGeometryTools();
		if(c3==null){
			coord=tools.createPointXY(xCoord, yCoord);
		}else{
			double zCoord;
			try{
				zCoord = Double.parseDouble(c3);
			}catch(Exception ex){
				throw new DataException("failed to read C3 <"+c3+">",ex);
			}
			coord=tools.createPointXYZ(xCoord, yCoord,zCoord);
		}
		return coord;
	}
	private static void addArc(IFMESession session,IFMEPath path,IomObject startPt,IomObject value)
	throws DataException
	{
		if(value!=null){
			String c1=value.getattrvalue("C1");
			String c2=value.getattrvalue("C2");
			String c3=value.getattrvalue("C3");
			String a1=value.getattrvalue("A1");
			String a2=value.getattrvalue("A2");
			double pt2_re;
			try{
				pt2_re = Double.parseDouble(c1);
			}catch(Exception ex){
				throw new DataException("failed to read C1 <"+c1+">",ex);
			}
			double pt2_ho;
			try{
				pt2_ho = Double.parseDouble(c2);
			}catch(Exception ex){
				throw new DataException("failed to read C2 <"+c2+">",ex);
			}
			double arcPt_re;
			try{
				arcPt_re = Double.parseDouble(a1);
			}catch(Exception ex){
				throw new DataException("failed to read A1 <"+a1+">",ex);
			}
			double arcPt_ho;
			try{
				arcPt_ho = Double.parseDouble(a2);
			}catch(Exception ex){
				throw new DataException("failed to read A2 <"+a2+">",ex);
			}
			IFMEGeometryTools tools=session.getGeometryTools();
			IFMEPoint startPoint=null;
			IFMEPoint arcPoint=null;
			IFMEPoint endPoint=null;
			IFMEArc arc=null;
			try{
				startPoint=coord2FME(session, startPt);
				arcPoint=tools.createPointXY(arcPt_re, arcPt_ho);
				endPoint=tools.createPointXY(pt2_re, pt2_ho);
				
				arc=tools.createArcBy3Points(startPoint, arcPoint, endPoint);
				//added by jko
				
				if (!(Double.valueOf(arc.getSweepAngle()).isNaN() || Math.abs(arc.getSweepAngle()) < 0.0001)){
					if (startPoint.equals(arcPoint) || endPoint.equals(arcPoint)){
						EhiLogger.logAdaption("Ignored arc because arcpoint identical to start or end point" +
								"("+arcPoint.getX()+","+arcPoint.getY()+")");
						IFMELine line=null;
						try{
							line=tools.convertToLine(arc);
							path.appendCurve(line);
						}finally{
							if(line!=null){
								line.dispose();
								line=null;
							}
						}
					}else{
						path.appendCurve(arc);
					}	
				}else{
					EhiLogger.logAdaption("Changed straight arc to straight on arc point " +
							"("+arcPoint.getX()+","+arcPoint.getY()+")");
							//add straight
					IFMELine line=null;
					try{
						line=tools.convertToLine(arc);
						path.appendCurve(line);
					}finally{
						if(line!=null){
							line.dispose();
							line=null;
						}
					}
				}
			}finally{
				if(startPoint!=null){
					startPoint.dispose();
					startPoint=null;
				}
				if(arcPoint!=null){
					arcPoint.dispose();
					arcPoint=null;
				}
				if(endPoint!=null){
					endPoint.dispose();
					endPoint=null;
				}
				if(arc!=null){
					arc.dispose();
					arc=null;
				}
			}
		}
	}
	/** Converts a POLYLINE to a FME Path.
	 * @param session FME session
	 * @param polylineObj INTERLIS POLYLINE structure
	 * @param isSurfaceOrArea true if called as part of a SURFACE conversion.
	 * @return FME Path
	 * @throws DataException
	 */
	public static IFMEPath polyline2FME(IFMESession session,IomObject polylineObj,boolean isSurfaceOrArea)
	throws DataException
	{
		if(polylineObj==null){
			return null;
		}
		IFMEGeometryTools tools=session.getGeometryTools();
		IFMEPath ret=tools.createPath();
		// is POLYLINE?
		if(isSurfaceOrArea){
			IomObject lineattr=polylineObj.getattrobj("lineattr",0);
			if(lineattr!=null){
				//writeAttrs(out,lineattr);
				if(firstLineattrib){
					EhiLogger.logAdaption("XTF lineattributes not yet supported");							
					firstLineattrib=false;
				}
			}
		}
		boolean clipped=polylineObj.getobjectconsistency()==IomConstants.IOM_INCOMPLETE;
		if(clipped){
			throw new DataException("clipped polyline not supported");
		}
		IomObject arcStartPt=null;
		for(int sequencei=0;sequencei<polylineObj.getattrvaluecount("sequence");sequencei++){
			if(clipped){
				//out.startElement(tags::get_CLIPPED(),0,0);
			}else{
				// an unclipped polyline should have only one sequence element
				if(sequencei>0){
					throw new DataException("unclipped polyline with multi 'sequence' elements");
				}
			}
			IomObject sequence=polylineObj.getattrobj("sequence",sequencei);
			int segmentc=sequence.getattrvaluecount("segment");
			for(int segmenti=0;segmenti<segmentc;segmenti++){
				IomObject segment=sequence.getattrobj("segment",segmenti);
				//EhiLogger.debug("segmenttag "+segment.getobjecttag());
				if(segment.getobjecttag().equals("COORD")){
					// COORD
					// first point and then an arc?
					if(sequencei==0 && segmenti==0 &&  segmentc>1 && !sequence.getattrobj("segment",segmenti+1).getobjecttag().equals("COORD")){
						// skip point now (add it as start of arc)
					}else{
						IFMEPoint point=null;
						try{
							point=coord2FME(session,segment);
							ret.extendToPoint(point);
						}finally{
							if(point!=null){
								point.dispose();
								point=null;
							}
						}
					}
				}else if(segment.getobjecttag().equals("ARC")){
					// ARC
					addArc(session,ret,arcStartPt,segment);
				}else{
					// custum line form
					throw new DataException("custom line form not supported");
					//out.startElement(segment->getTag(),0,0);
					//writeAttrs(out,segment);
					//out.endElement(/*segment*/);
				}
				arcStartPt=segment;
			}
			if(clipped){
				//out.endElement(/*CLIPPED*/);
			}
		}
		return ret;
	}

	private static IFMEArea surface2FMEUnchecked(IFMESession session, IomObject surface)
	throws DataException
	{
		IFMEArea ret = null;
		IFMEGeometryTools tools=session.getGeometryTools();

		int boundaryc=surface.getattrvaluecount("boundary");
		for(int boundaryi=0;boundaryi<boundaryc;boundaryi++){
			IomObject boundary=surface.getattrobj("boundary",boundaryi);
			IFMEPath fmeBoundary=null;
			try{
				fmeBoundary=tools.createPath();
				for(int polylinei=0;polylinei<boundary.getattrvaluecount("polyline");polylinei++){
					IomObject polyline=boundary.getattrobj("polyline",polylinei);
					IFMEPath fmeLine=null;
					try{
						fmeLine=polyline2FME(session,polyline,true);
						fmeBoundary.appendCurve(fmeLine);
					}finally{
						if(fmeLine!=null){
							fmeLine.dispose();
						}
					}
				}
				if(boundaryi==0){
					if(boundaryc==1){
						ret=tools.createPolygonByCurve(fmeBoundary);
					}else{
						ret=tools.createDonutByCurve(fmeBoundary);
					}
				}else{
					((IFMEDonut) ret).addInnerBoundaryCurve(fmeBoundary);
				}
			}finally{
				if(fmeBoundary!=null){
					fmeBoundary.dispose();
				}
			}
		}
		return ret;
	}

	/** Converts a SURFACE to a FME Donut.
	 * @param session FME session
	 * @param obj INTERLIS SURFACE structure
	 * @return FME Donut
	 * @throws DataException
	 */
	public static IFMEArea surface2FME(IFMESession session,IomObject obj) //SurfaceOrAreaType type)
	throws DataException
	{
		if(obj==null || obj.getattrvaluecount("surface") == 0){
			return null;
		}

		boolean clipped=obj.getobjectconsistency()==IomConstants.IOM_INCOMPLETE;
		if(clipped){
			throw new DataException("clipped surface not supported");
		}else if (obj.getattrvaluecount("surface") > 1) {
			throw new DataException("unclipped surface with multi 'surface' elements");
		}

		IomObject surface = obj.getattrobj("surface", 0);
		return surface2FMEUnchecked(session, surface);
	}

	public static IFMEGeometry geom2FME(IFMESession session,IomObject obj)
	throws DataException
	{
		if(obj==null){
			return null;
		}
		String type=obj.getobjecttag();
		if(type.equals("COORD")){
			return coord2FME(session,obj);
		}else if(type.equals("POLYLINE")){
			return polyline2FME(session,obj,false);
		}else if(type.equals("MULTISURFACE")){
			return surface2FME(session,obj);
		}
		throw new DataException("unexpected type "+type);
	}

	public static IFMEMultiCurve multipolyline2FME(IFMESession session, IomObject value)
	throws  DataException
	{
		IFMEGeometryTools tools=session.getGeometryTools();
		IFMEMultiCurve multiCurve = tools.createMultiCurve();

		for (int i = 0; i < value.getattrvaluecount("polyline"); i++) {
			IomObject polyline = value.getattrobj("polyline", i);
			IFMEPath curve = polyline2FME(session, polyline, false);
			multiCurve.appendPart(curve);
		}

		return  multiCurve;
	}

	public static IFMEMultiArea multisurface2FME(IFMESession session, IomObject value)
	throws DataException
	{
		boolean clipped=value.getobjectconsistency()==IomConstants.IOM_INCOMPLETE;
		if(clipped){
			throw new DataException("clipped surface not supported");
		}

		IFMEGeometryTools tools=session.getGeometryTools();
		IFMEMultiArea multiArea = tools.createMultiArea();

		for (int i = 0; i < value.getattrvaluecount("surface"); i++) {
			IomObject surface = value.getattrobj("surface", i);
			IFMEArea area = surface2FMEUnchecked(session, surface);
			multiArea.appendPart(area);
		}

		return  multiArea;
	}

	public static IFMEMultiPoint multicoord2FME(IFMESession session, IomObject value)
	throws DataException
	{
		IFMEGeometryTools tools=session.getGeometryTools();
		IFMEMultiPoint multiPoint = tools.createMultiPoint();

		for (int i = 0; i < value.getattrvaluecount("coord"); i++) {
			IomObject coord = value.getattrobj("coord", i);
			IFMEPoint point = coord2FME(session, coord);
			multiPoint.appendPart(point);
		}

		return multiPoint;
	}
}
