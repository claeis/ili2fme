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

import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;

import ch.interlis.ili2c.metamodel.*;
import ch.ehi.basics.logging.EhiLogger;


/**
 * @author ce
 * @version $Revision: 1.0 $ $Date: 27.07.2006 $
 */
public class ModelUtility {
	private ModelUtility(){};
	/** @return map<String fmeFeatureTypeName, ViewableWrapper wrapper>
	 */
	public static HashMap getItfTransferViewables(ch.interlis.ili2c.metamodel.TransferDescription td)
	{
		HashMap ret = new HashMap();
		Iterator modeli = td.iterator();
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
						Iterator iter = topic.getViewables().iterator();
						while (iter.hasNext()) {
							Object obj = iter.next();
							if (obj instanceof Viewable) {
								Viewable v = (Viewable) obj;
								if(isPureRefAssoc(v)){
									continue;
								}
								//log.logMessageString("getTransferViewables() leave <"+v+">",IFMELogFile.FME_INFORM);
								String className = v.getScopedName(null);
								ViewableWrapper viewableWrapper =
									new ViewableWrapper(className, v);
								java.util.List attrv =
										ch.interlis.iom_j.itf.ModelUtilities.getIli1AttrList(
										(AbstractClassDef) v);
								viewableWrapper.setAttrv(attrv);
								ret.put(
									viewableWrapper.getFmeFeatureType(),
									viewableWrapper);
								// set geom attr in wrapper
								Iterator attri = v.getAttributes();
								while (attri.hasNext()) {
									Object attrObj = attri.next();
									if (attrObj instanceof AttributeDef) {
										AttributeDef attr =
											(AttributeDef) attrObj;
										Type type =
											Type.findReal(attr.getDomain());
										if (type instanceof PolylineType 
											|| type instanceof SurfaceOrAreaType 
											|| type instanceof CoordType
											){
												viewableWrapper.setGeomAttr4FME(attr);
												break;
										}
									}
								}
								// add helper tables of surface and area attributes
								attri = v.getAttributes();
								while (attri.hasNext()) {
									Object attrObj = attri.next();
									if (attrObj instanceof AttributeDef) {
										AttributeDef attr =
											(AttributeDef) attrObj;
										Type type =
											Type.findReal(attr.getDomain());
										if (type
											instanceof SurfaceOrAreaType) {
											String name =
												v.getContainer().getScopedName(
													null)
													+ "."
													+ v.getName()
													+ "_"
													+ attr.getName();
											ViewableWrapper wrapper =
												new ViewableWrapper(name);
											wrapper.setGeomAttr4FME(attr);
											ArrayList helper_attrv=new ArrayList();
											helper_attrv.add(new ViewableTransferElement(attr));
											wrapper.setAttrv(helper_attrv);
											ret.put(
												wrapper.getFmeFeatureType(),
												wrapper);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return ret;
	}
	/** @return map<String fmeFeatureTypeName, ViewableWrapper wrapper>
	 */
	public static HashMap getXrfTransferViewables(ch.interlis.ili2c.metamodel.TransferDescription td)
	{
		HashMap ret = new HashMap();
		Iterator modeli = td.iterator();
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
						Iterator iter = topic.getViewables().iterator();
						while (iter.hasNext()) {
							Object obj = iter.next();
							if (obj instanceof Viewable) {
								Viewable v = (Viewable) obj;
								if(isPureRefAssoc(v)){
									continue;
								}
								//log.logMessageString("getTransferViewables() leave <"+v+">",IFMELogFile.FME_INFORM);
								String className = v.getScopedName(null);
								ViewableWrapper viewableWrapper =
									new ViewableWrapper(className, v);
								java.util.List attrv =
										ch.interlis.iom_j.itf.ModelUtilities.getIli1AttrList(
										(AbstractClassDef) v);
								viewableWrapper.setAttrv(attrv);
								ret.put(
									viewableWrapper.getFmeFeatureType(),
									viewableWrapper);
								// set geom attr in wrapper
								Iterator attri = v.getAttributes();
								while (attri.hasNext()) {
									Object attrObj = attri.next();
									if (attrObj instanceof AttributeDef) {
										AttributeDef attr =
											(AttributeDef) attrObj;
										Type type =
											Type.findReal(attr.getDomain());
										if (type instanceof PolylineType 
											|| type instanceof SurfaceOrAreaType 
											|| type instanceof CoordType
											){
												viewableWrapper.setGeomAttr4FME(attr);
												break;
										}
									}
								}
								// add helper tables of surface and area attributes
								attri = v.getAttributes();
								while (attri.hasNext()) {
									Object attrObj = attri.next();
									if (attrObj instanceof AttributeDef) {
										AttributeDef attr =
											(AttributeDef) attrObj;
										Type type =
											Type.findReal(attr.getDomain());
										if (type
											instanceof AreaType) {
											String name =
												v.getContainer().getScopedName(
													null)
													+ "."
													+ v.getName()
													+ "_"
													+ attr.getName();
											ViewableWrapper wrapper =
												new ViewableWrapper(name);
											wrapper.setGeomAttr4FME(attr);
											ArrayList helper_attrv=new ArrayList();
											helper_attrv.add(new ViewableTransferElement(attr));
											wrapper.setAttrv(helper_attrv);
											ret.put(
												wrapper.getFmeFeatureType(),
												wrapper);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return ret;
	}
	/** @return map<String fmeFeatureTypeName, ViewableWrapper wrapper>
	 */
	public static HashMap getXtfTransferViewables(ch.interlis.ili2c.metamodel.TransferDescription td,int inheritanceMappingStrategy)
	{
		HashSet leaveclassv=new HashSet();
		// for all models
		Iterator modeli = td.iterator ();
		while (modeli.hasNext ())
		{
		  Object mObj = modeli.next ();
		  if (mObj instanceof Model){
			Model model=(Model)mObj;
			//EhiLogger.debug("model <"+model+">");
			Iterator topici=model.iterator();
			while(topici.hasNext()){
				Object tObj=topici.next();
				if (tObj instanceof Topic){
				  Topic topic=(Topic)tObj;
				  //EhiLogger.debug("topic <"+topic+">");
				  Iterator iter = topic.getViewables().iterator();
				  while (iter.hasNext())
				  {
					Object obj = iter.next();
					if(obj instanceof Viewable){
						Viewable v=(Viewable)obj;
						if(isDerivedAssoc(v) 
								|| isPureRefAssoc(v) 
								|| isTransientView(v)){
							continue;
						}
						//EhiLogger.debug("leaveclass <"+v+">");
						leaveclassv.add(v);
					}
				  }
				}else if(tObj instanceof Viewable){
					  Viewable v=(Viewable)tObj;
						if(isDerivedAssoc(v) 
								|| isPureRefAssoc(v) 
								|| isTransientView(v)){
							continue;
						}
					  //EhiLogger.debug("leaveclass <"+v+">");
					  leaveclassv.add(v);
				}
			}
		  }
		}
		// find base classes and add possible extensions
		Iterator vi=leaveclassv.iterator();
		HashMap basev=new HashMap(); // map<Viewable root,HashSet<Viewable extensions>> 
		while(vi.hasNext()){
			Viewable v=(Viewable)vi.next();
			//EhiLogger.debug("leaveclass <"+v+">");
			// is it a CLASS defined in model INTERLIS? 
			if((v instanceof Table) && ((Table)v).isIdentifiable() && v.getContainerOrSame(Model.class)==td.INTERLIS){
				// skip it; use in any case sub-class strategy
				continue;
			}
			Viewable root=null;
			if(inheritanceMappingStrategy==InheritanceMapping.SUBCLASS){
				// is v a STRUCTURE?
				if(isStructure(v)){
					// use in any case a super-class strategy
					root=getRoot(v);
				}else if(isEmbeddedAssocWithAttrs(v)){
					// use in any case a super-class strategy
					root=getRoot(v);
				}else{
					// CLASS or ASSOCIATION
					if(v.isAbstract()){
						continue;
					}
					root=null;
				}
			}else{
				root=getRoot(v);
			}
			//EhiLogger.debug("  root <"+root+">");
			if(root==null){
				if(!basev.containsKey(v)){
					basev.put(v,new HashSet());
				}
			}else{
				HashSet extv;
				if(basev.containsKey(root)){
					extv=(HashSet)basev.get(root);
				}else{
					extv=new HashSet();
					basev.put(root,extv);
				}
				while(v!=root){
					extv.add(v);
					v=(Viewable)v.getExtending();
				}
			}
		}
		// build list of attributes
		vi=basev.keySet().iterator();
		HashMap ret=new HashMap();
		while(vi.hasNext()){
			Viewable v=(Viewable)vi.next();
			//EhiLogger.debug("baseclass <"+v+">");
			ArrayList attrv=new ArrayList();
			mergeAttrs(attrv,v,true);
			Iterator exti=((HashSet)basev.get(v)).iterator();
			while(exti.hasNext()){
				Viewable ext=(Viewable)exti.next();
				//EhiLogger.debug("  ext <"+ext+">");
				mergeAttrs(attrv,ext,false);
			}
			ViewableWrapper wrapper=new ViewableWrapper(v.getScopedName(null),v);
			wrapper.setAttrv(attrv);
			boolean isEncodedAsStruct=isStructure(v) || isEmbeddedAssocWithAttrs(v);
			for(int i=0;i<attrv.size();i++){
				ViewableTransferElement attro=(ViewableTransferElement)attrv.get(i);
				if(attro.obj instanceof AttributeDef){
					AttributeDef attr=(AttributeDef)attro.obj;
					Type type=attr.getDomainResolvingAliases();
					if (type instanceof PolylineType 
						|| type instanceof SurfaceOrAreaType
						|| type instanceof CoordType){
						if((type instanceof CoordType) && ((CoordType)type).getDimensions().length==1){
							// encode 1d coord as fme attribute and not as fme-geom
						}else if(!isEncodedAsStruct){
							wrapper.setGeomAttr4FME(attr);
							break;
						}
					}
				}
			}
			ret.put(wrapper.getFmeFeatureType(),wrapper);
			exti=((HashSet)basev.get(v)).iterator();
			while(exti.hasNext()){
				Viewable ext=(Viewable)exti.next();
				//EhiLogger.debug("  ext <"+ext+">");
				ret.put(ext.getScopedName(null),wrapper);
			}
		}
		// addSecondGeomAttrs(ret,v);
		return ret;
	}
	private static boolean isStructure(Viewable v) {
		if((v instanceof Table) && !((Table)v).isIdentifiable()){
			return true;
		}
		return false;
	}
	private static boolean isTransientView(Viewable v) {
		if (!(v instanceof View)){
			return false;
		}
		Topic topic=(Topic)v.getContainer (Topic.class);
		if(topic==null){
			return true;
		}
		if(topic.isViewTopic()){ // TODO && !((View)v).isTransient()){
			return false;
		}
		return true;
	}
	public static boolean isPureRefAssoc(Viewable v) {
		if(!(v instanceof AssociationDef)){
			return false;
		}
		AssociationDef assoc=(AssociationDef)v;
		// embedded and no attributes/embedded links?
		if(assoc.isLightweight() && 
			!assoc.getAttributes().hasNext()
			&& !assoc.getLightweightAssociations().iterator().hasNext()
			) {
			return true;
		}
		return false;
	}
	public static boolean isEmbeddedAssocWithAttrs(Viewable v) {
		if(!(v instanceof AssociationDef)){
			return false;
		}
		AssociationDef assoc=(AssociationDef)v;
		// embedded and attributes/embedded links?
		if(assoc.isLightweight() && 
			(assoc.getAttributes().hasNext()
			|| assoc.getLightweightAssociations().iterator().hasNext())
			) {
			return true;
		}
		return false;
	}
	private static boolean isDerivedAssoc(Viewable v) {
		if(!(v instanceof AssociationDef)){
			return false;
		}
		AssociationDef assoc=(AssociationDef)v;
		if(assoc.getDerivedFrom()!=null){
			return true;
		}
		return false;
	}
	public static Viewable getRoot(Viewable v)
	{
		Viewable root=(Viewable)v.getRootExtending();
		// a class extended from a structure?
		if(isStructure(root) && !isStructure(v)){
			// find root class
			root=v;
			Viewable nextbase=(Viewable)v.getExtending();
			while(!isStructure(nextbase)){
				root=nextbase;
				nextbase=(Viewable)root.getExtending();
			}
		}
		// is root a class and defined in model INTERLIS?
		if((root instanceof Table) && ((Table)root).isIdentifiable() && root.getContainerOrSame(Model.class) instanceof PredefinedModel){
			if((v instanceof Table) && ((Table)v).isIdentifiable() && v.getContainerOrSame(Model.class) instanceof PredefinedModel){
				// skip it
				return v;
			}
			// use base as root that is defined outside model INTERLIS
			root=v;
			Viewable nextbase=(Viewable)v.getExtending();
			while(!(nextbase.getContainerOrSame(Model.class) instanceof PredefinedModel)){
				root=nextbase;
				nextbase=(Viewable)root.getExtending();
			}
		}
		return root;
	}
	/**
	 * @param attrv list<ViewableTransferElement>
	 * @param v
	 * @param isRoot
	 */
	private static void mergeAttrs(ArrayList attrv,Viewable v,boolean isRoot){
		Iterator iter = null;
		if(isRoot){
			iter=v.getAttributesAndRoles2();
		}else{
			iter=v.getDefinedAttributesAndRoles2();
		}
		while (iter.hasNext()) {
			ViewableTransferElement obj = (ViewableTransferElement)iter.next();
			if (obj.obj instanceof AttributeDef) {
				attrv.add(obj);
			}
			if(obj.obj instanceof RoleDef){
				RoleDef role = (RoleDef) obj.obj;
				// not an embedded role and roledef not defined in a lightweight association?
				if (!obj.embedded && !((AssociationDef)v).isLightweight()){
					attrv.add(obj);
				}
				// a role of an embedded association?
				if(obj.embedded){
					AssociationDef roleOwner = (AssociationDef) role.getContainer();
					if(roleOwner.getDerivedFrom()==null){
						attrv.add(obj);
					}
				}
			}
		}
	}
	
}
