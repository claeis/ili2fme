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

import ch.interlis.iom.IomObject;
import COM.safe.fmeobjects.*;

/** Utility to convert from FME to INTERLIS IOM geometry types.
 * @author ceis
 */
public class Fme2iox {
	// utility, no instances
	private Fme2iox(){}
	/** Converts from a Point to a INTERLIS COORD.
	 * @param value FME Point.
	 * @return INTERLIS COORD structure
	 */
	static public  IomObject FME2coord(IFMEPoint value) 
	throws DataException
	{
		IomObject ret=new ch.interlis.iom_j.Iom_jObject("COORD",null);
		ret.setattrvalue("C1", Double.toString(value.getX()));
		ret.setattrvalue("C2", Double.toString(value.getY()));
		if(value.is3D()){
			ret.setattrvalue("C3", Double.toString(value.getZ()));
		}
		return ret;
	}
	/** Converts from a FME Path to a INTERLIS POLYLINE.
	 * @param value FME Path 
	 * @return INTERLIS POLYLINE structure
	 */
	static public  IomObject FME2polyline(IFMESession session,IFMEPath value) 
		throws DataException
	{
		IomObject ret=new ch.interlis.iom_j.Iom_jObject("POLYLINE",null);
		IomObject sequence=new ch.interlis.iom_j.Iom_jObject("SEGMENTS",null);
		ret.addattrobj("sequence",sequence);
		int segc=value.numSegments();
		for(int segi=0;segi<segc;segi++){
			IFMESegment seg=null;
			seg=value.getSegmentAt(segi);
			boolean is3D=value.is3D();
			addSegment(session,sequence, seg, is3D);
		}
		return ret;
	}
	private static void addSegment(IFMESession session,IomObject sequence, IFMESegment seg, boolean is3D) 
		throws DataException 
	{
		IFMEPoint coord=null;
		IFMEGeometryTools tools=session.geometryTools();
		try{
			coord=tools.createPoint();
			if(seg instanceof IFMELine){
				IFMELine line=(IFMELine)seg;
				int coordc=line.numPoints();
				// if not first segement then skip first point
				int coordi=sequence.getattrvaluecount("segment")>0?1:0;
				for(;coordi<coordc;coordi++){
					try {
						line.getPointAt(coordi, coord);
					} catch (FMEException ex) {
						throw new DataException(ex);
					}
					sequence.addattrobj("segment", FME2coord(coord));
				}
			}else if(seg instanceof IFMEArc){
				IFMEArc arc=(IFMEArc)seg;
				// if first segement
				if(sequence.getattrvaluecount("segment")==0){
					// add start point
					try {
						arc.getStartPoint(coord);
					} catch (FMEException ex) {
						throw new DataException(ex);
					}
					IomObject iomCoord=new ch.interlis.iom_j.Iom_jObject("COORD",null);
					iomCoord.setattrvalue("C1", Double.toString(coord.getX()));
					iomCoord.setattrvalue("C2", Double.toString(coord.getY()));
					if(is3D){
						iomCoord.setattrvalue("C3", Double.toString(coord.getZ()));
					}
					sequence.addattrobj("segment", iomCoord);
				}
				try {
					arc.getEndPoint(coord);
				} catch (FMEException ex) {
					throw new DataException(ex);
				}
				IomObject iomArc=new ch.interlis.iom_j.Iom_jObject("ARC",null);
				iomArc.setattrvalue("C1", Double.toString(coord.getX()));
				iomArc.setattrvalue("C2", Double.toString(coord.getY()));
				if(is3D){
					iomArc.setattrvalue("C3", Double.toString(coord.getZ()));
				}
				try {
					arc.getMidPoint(coord);
				} catch (FMEException ex) {
					throw new DataException(ex);
				}
				iomArc.setattrvalue("A1", Double.toString(coord.getX()));
				iomArc.setattrvalue("A2", Double.toString(coord.getY()));
				sequence.addattrobj("segment", iomArc);
			}else{
				throw new IllegalArgumentException("unexpected IFMESegment "+seg.getClass().getName());
			}
		}finally{
			if(coord!=null){
				coord.dispose();
				coord=null;
			}
		}
	}
	/** Converts from a FME Curve to a INTERLIS POLYLINE.
	 * @param value FME Curve 
	 * @return INTERLIS POLYLINE structure
	 */
	static public  IomObject FME2polyline(IFMESession session,IFMECurve value) 
	throws DataException 
	{
		if(value instanceof IFMEPath){
			return FME2polyline(session,(IFMEPath) value); 
		}else if(value instanceof IFMESegment){
			IomObject ret=new ch.interlis.iom_j.Iom_jObject("POLYLINE",null);
			IomObject sequence=new ch.interlis.iom_j.Iom_jObject("SEGMENTS",null);
			ret.addattrobj("sequence",sequence);
			IFMESegment seg=(IFMESegment)value;
			boolean is3D=value.is3D();
			addSegment(session,sequence, seg, is3D);
			return ret;
		}else{
			throw new IllegalArgumentException("unexpected IFMECurve "+value.getClass().getName());
		}
	}
	/** Converts from a FME Donut to a INTERLIS SURFACE.
	 * @param value FME Donut
	 * @return INTERLIS SURFACE structure
	 */
	static public  IomObject FME2surface(IFMESession session,IFMEDonut value) 
	throws DataException 
	{
		IomObject ret=new ch.interlis.iom_j.Iom_jObject("MULTISURFACE",null);
		IomObject surface=new ch.interlis.iom_j.Iom_jObject("SURFACE",null);
		ret.addattrobj("surface",surface);

		// shell
		IFMECurve shell=null;
		shell=value.getOuterBoundaryAsCurve();
		IomObject boundary=new ch.interlis.iom_j.Iom_jObject("BOUNDARY",null);
		surface.addattrobj("boundary",boundary);
		boundary.addattrobj("polyline", FME2polyline(session,shell));
		
		// holes
		int holec=value.numInnerBoundaries();
		for(int holei=0;holei<holec;holei++){
			IFMECurve hole=null;
			hole=value.getInnerBoundaryAsCurveAt(holei);
			boundary=new ch.interlis.iom_j.Iom_jObject("BOUNDARY",null);
			surface.addattrobj("boundary",boundary);
			boundary.addattrobj("polyline", FME2polyline(session,hole));
		}
		return ret;
	}
	/** Converts from a FME Area to a INTERLIS SURFACE.
	 * @param value FME Area
	 * @return INTERLIS SURFACE structure
	 */
	static public  IomObject FME2surface(IFMESession session,IFMEArea value) 
	throws DataException 
	{
		if(value instanceof IFMEDonut){
			return FME2surface(session,(IFMEDonut) value);
		}else if(value instanceof IFMESimpleArea){
			IomObject ret=new ch.interlis.iom_j.Iom_jObject("MULTISURFACE",null);
			IomObject surface=new ch.interlis.iom_j.Iom_jObject("SURFACE",null);
			ret.addattrobj("surface",surface);
			IFMECurve shell=null;
			shell=((IFMESimpleArea)value).getBoundaryAsCurve();
			IomObject boundary=new ch.interlis.iom_j.Iom_jObject("BOUNDARY",null);
			surface.addattrobj("boundary",boundary);
			boundary.addattrobj("polyline", FME2polyline(session,shell));
			return ret;
		}else{
			throw new IllegalArgumentException("unexpected IFMEArea "+value.getClass().getName());
		}
	}

}
