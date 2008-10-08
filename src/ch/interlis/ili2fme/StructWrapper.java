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

/** Wrapper around a struct element, used to make a struct element aware of its parent.
 * @author ce
 * @version $Revision: 1.0 $ $Date: 05.04.2005 $
 */
public class StructWrapper {
	private int parentId;
	private String parentType;
	private IomObject struct;
	private int structi;
	private String parentAttr;
	public StructWrapper(int parentId1,String parentType1,String parentAttr1,IomObject struct1,int structi1){
		parentId=parentId1;
		parentType=parentType1;
		parentAttr=parentAttr1;
		struct=struct1;
		structi=structi1;
	}
	/** the id of the parent object.
	 */
	public int getParentId() {
		return parentId;
	}
	/** the type of the parent object.
	 */
	public String getParentType() {
		return parentType;
	}
	/** the name of the parent attribute.
	 */
	public String getParentAttr() {
		return parentAttr;
	}
	/** the wrapped struct element.
	 */
	public IomObject getStruct() {
		return struct;
	}
	/** the index of the struct element in the parent attribute.
	 */
	public int getStructi() {
		return structi;
	}

}
