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
		IFMEGeometryTools tools=session.geometryTools();
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
	private static void addArc(IFMESession session,IFMEPath path,IomObject value)
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
			IFMEGeometryTools tools=session.geometryTools();
			IFMEPoint startPoint=null;
			IFMEPoint arcPoint=null;
			IFMEPoint endPoint=null;
			IFMEArc arc=null;
			try{
				startPoint=tools.createPoint();
				path.getEndPoint(startPoint);
				arcPoint=tools.createPointXY(arcPt_re, arcPt_ho);
				endPoint=tools.createPointXY(pt2_re, pt2_ho);
				
				arc=tools.createArcBy3Points(startPoint, arcPoint, endPoint);
				//added by jko
				
				if (!(Double.valueOf(arc.getSweepAngle()).isNaN() || Math.abs(arc.getSweepAngle()) < 0.0001)){
					if (startPoint.equals(arcPoint) || endPoint.equals(arcPoint)){
						EhiLogger.logAdaption("Ignored arc because arcpoint identical to start or end point" +
								"("+arcPoint.getX()+","+arcPoint.getY()+")");
							path.appendCurve(tools.convertToLine(arc));
					}else{
						
							path.appendCurve(arc);
						
							}	
				}else{
					EhiLogger.logAdaption("Changed straight arc to straight on arc point " +
							"("+arcPoint.getX()+","+arcPoint.getY()+")");
							//add straight
							path.appendCurve(tools.convertToLine(arc));
				}
			}catch(FMEException ex){
				throw new DataException("failed to add Arc",ex);
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
		IFMEGeometryTools tools=session.geometryTools();
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
			for(int segmenti=0;segmenti<sequence.getattrvaluecount("segment");segmenti++){
				IomObject segment=sequence.getattrobj("segment",segmenti);
				//EhiLogger.debug("segmenttag "+segment.getobjecttag());
				if(segment.getobjecttag().equals("COORD")){
					// COORD
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
				}else if(segment.getobjecttag().equals("ARC")){
					// ARC
					
					addArc(session,ret,segment);
				}else{
					// custum line form
					throw new DataException("custom line form not supported");
					//out.startElement(segment->getTag(),0,0);
					//writeAttrs(out,segment);
					//out.endElement(/*segment*/);
				}

			}
			if(clipped){
				//out.endElement(/*CLIPPED*/);
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
	public static IFMEDonut surface2FME(IFMESession session,IomObject obj) //SurfaceOrAreaType type)
	throws DataException
	{
		if(obj==null){
			return null;
		}
		IFMEGeometryTools tools=session.geometryTools();
		IFMEDonut ret=null;
		//IFMEFeatureVector bndries=session.createFeatureVector();
		boolean clipped=obj.getobjectconsistency()==IomConstants.IOM_INCOMPLETE;
		if(clipped){
			throw new DataException("clipped surface not supported");
		}
		for(int surfacei=0;surfacei<obj.getattrvaluecount("surface");surfacei++){
			if(clipped){
				//out.startElement("CLIPPED",0,0);
			}else{
				// an unclipped surface should have only one surface element
				if(surfacei>0){
					throw new DataException("unclipped surface with multi 'surface' elements");
				}
			}
			IomObject surface=obj.getattrobj("surface",surfacei);
			int boundaryc=surface.getattrvaluecount("boundary");
			for(int boundaryi=0;boundaryi<boundaryc;boundaryi++){
				IomObject boundary=surface.getattrobj("boundary",boundaryi);
				//IFMEFeature fmeLine=session.createFeature();
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
								fmeLine=null;
							}
						}
					}
					if(boundaryi==0){
						ret=tools.createDonutByCurve(fmeBoundary);
					}else{
						ret.addInnerBoundaryCurve(fmeBoundary);
					}
				}finally{
					if(fmeBoundary!=null){
						fmeBoundary.dispose();
						fmeBoundary=null;
					}
				}
			}
			if(clipped){
				//out.endElement(/*CLIPPED*/);
			}
		}
		return ret;
	}
	
	private static double dist(IFMEPoint pt1, IFMEPoint pt2){
		return Math.sqrt(
				Math.pow(pt2.getY()-pt1.getY(), 2)
				+
				Math.pow(pt2.getY()-pt1.getY(), 2)
				);
		
	}

}
