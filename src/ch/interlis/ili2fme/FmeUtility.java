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

public class FmeUtility {
	private FmeUtility(){}
	
	static public boolean isTrue(String str)
	{
		return "TRUE".equalsIgnoreCase(str) || "YES".equalsIgnoreCase(str);
	}
	static public int mapFme2IoxConsistency(String value) {
		int consistency=IomConstants.IOM_COMPLETE;
		if(value!=null){
			if(value.equals("INCOMPLETE")){
				consistency=IomConstants.IOM_INCOMPLETE;
			}else if(value.equals("COMPLETE")){
					consistency=IomConstants.IOM_COMPLETE;
			}else if(value.equals("INCONSISTENT")){
				consistency=IomConstants.IOM_INCONSISTENT;
			}else if(value.equals("ADAPTED")){
				consistency=IomConstants.IOM_ADAPTED;
			}
		}
		return consistency;
	}
	static public String mapIox2FmeConsistency(int value) {
		String consistency=null; // IomConstants.IOM_COMPLETE
		if(value==IomConstants.IOM_INCOMPLETE){
			consistency="INCOMPLETE";
		}else if(value==IomConstants.IOM_INCONSISTENT){
			consistency="INCONSISTENT";
		}else if(value==IomConstants.IOM_ADAPTED){
			consistency="ADAPTED";
		}
		return consistency;
	}
	static public int mapFme2IoxOperation(String value) {
		int operation=IomConstants.IOM_OP_INSERT;
		if(value!=null){
			if(value.equals("DELETE")){
				operation=IomConstants.IOM_OP_DELETE;
			}else if(value.equals("INSERT")){
				operation=IomConstants.IOM_OP_INSERT;
			}else if(value.equals("UPDATE")){
				operation=IomConstants.IOM_OP_UPDATE;
			}
		}
		return operation;
	}
	static public String mapIox2FmeOperation(int value) {
		String operation=null; // ==IomConstants.IOM_OP_INSERT
		if(value==IomConstants.IOM_OP_DELETE){
			operation="DELETE";
		}else if(value==IomConstants.IOM_OP_UPDATE){
			operation="UPDATE";
		}
		return operation;
	}
}
