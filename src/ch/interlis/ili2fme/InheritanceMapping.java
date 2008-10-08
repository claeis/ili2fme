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

public class InheritanceMapping {
	static public final int SUPERCLASS=1;
	static public final int SUBCLASS=2;
	static public final String SUPERCLASS_TXT="SuperClass";
	static public final String SUBCLASS_TXT="SubClass";
	private InheritanceMapping(){}
	static public int valueOf(String value) {
		int mappingStrategy=SUPERCLASS;
		if(value!=null){
			if(value.equals(SUPERCLASS_TXT)){
				mappingStrategy=SUPERCLASS;
			}else if(value.equals(SUBCLASS_TXT)){
				mappingStrategy=SUBCLASS;
			}else{
				EhiLogger.logError("illegal InheritanceMapping value <"+value+">");
			}
		}
		return mappingStrategy;
	}
	static public String toString(int value) {
		String mappingStrategyTxt=SUPERCLASS_TXT;
		if(value==SUPERCLASS){
			mappingStrategyTxt=SUPERCLASS_TXT;
		}else if(value==SUBCLASS){
			mappingStrategyTxt=SUBCLASS_TXT;
		}else{
			throw new IllegalArgumentException("InheritanceMapping "+value);
		}
		return mappingStrategyTxt;
	}
}
