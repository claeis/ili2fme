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

import ch.interlis.ili2c.metamodel.*;
import java.util.List;

/** Wrapper around a Viewable or a SurfaceOrArea attribute to give it an FME name 
 * and make it aware of all attributes of all specializations.
 * @author ce
 * @version $Revision: 1.0 $ $Date: 13.04.2005 $
 */
public class ViewableWrapper {
	private boolean isHelper_=false;
	public ViewableWrapper(String fmeFeatureType,Viewable viewable){
		this.fmeFeatureType=fmeFeatureType;
		this.viewable=viewable;
		isHelper_=false;
	}
	/** create a wrapper around a helper table
	 */
	public ViewableWrapper(String fmeFeatureType){
		this.fmeFeatureType=fmeFeatureType;
		isHelper_=true;
	}
	private String fmeFeatureType=null;
	/** the viewable that this wrapper wraps. May be null, if it wraps a SurfaceOrArea attribute.
	 */
	private Viewable viewable=null;
	/** the attributes and roles that this viewable and all it specializations have.
	 * list<Viewable.TransferElement>
	 */
	private List attrv=new java.util.ArrayList();
	/** the geometry of this viewable
	 */
	private AttributeDef geomAttr=null;
	/** the attributes and roles that this viewable and all it specializations have.
	 * @return list<ViewableTransferElement>
	 */
	public List getAttrv() {
		return attrv;
	}
	public void setAttrv(List list) {
		attrv = list;
	}
	public java.util.Iterator getAttrIterator() {
		return attrv.iterator();
	}


	/** gets the viewable that this wrapper wraps.
	 */
	public Viewable getViewable() {
		return viewable;
	}

	/** gets the geometry attribute of this viewable used for the FME feature type.
	 */
	public AttributeDef getGeomAttr4FME() {
		return geomAttr;
	}

	public void setGeomAttr4FME(AttributeDef def) {
		geomAttr = def;
	}

	public String getFmeFeatureType() {
		return fmeFeatureType;
	}
	public void setFmeFeatureType(String string) {
		fmeFeatureType = string;
	}
	public boolean isHelper()
	{
		return this.isHelper_;
	}
}
