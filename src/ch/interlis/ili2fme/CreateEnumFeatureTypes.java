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

import ch.ehi.basics.logging.EhiLogger;

public class CreateEnumFeatureTypes {
	static public final int NO=1;
	static public final int SINGLETYPE=2;
	static public final int ONETYPEPERENUMDEF=3;
	static public final String NO_TXT="No";
	static public final String SINGLETYPE_TXT="SingleType";
	static public final String ONETYPEPERENUMDEF_TXT="OneTypePerEnumDef";
	private CreateEnumFeatureTypes(){}
	static public int valueOf(String value) {
		int createEnumType=NO;
		if(value!=null){
			if(value.equals(NO_TXT)){
				createEnumType=NO;
			}else if(value.equals(SINGLETYPE_TXT)){
				createEnumType=SINGLETYPE;
			}else if(value.equals(ONETYPEPERENUMDEF_TXT)){
				createEnumType=ONETYPEPERENUMDEF;
			}else{
				EhiLogger.logError("illegal CreateEnumFeatureTypes value <"+value+">");
			}
		}
		return createEnumType;
	}
	static public String toString(int value) {
		String createEnumTypeTxt=NO_TXT;
		if(value==NO){
			createEnumTypeTxt=NO_TXT;
		}else if(value==SINGLETYPE){
			createEnumTypeTxt=SINGLETYPE_TXT;
		}else if(value==ONETYPEPERENUMDEF){
			createEnumTypeTxt=ONETYPEPERENUMDEF_TXT;
		}else{
			throw new IllegalArgumentException("CreateEnumFeatureTypes "+value);
		}
		return createEnumTypeTxt;
	}
}
