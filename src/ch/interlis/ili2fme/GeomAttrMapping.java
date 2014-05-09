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

public class GeomAttrMapping {
	static public final int ENCODE_AS_ATTRIBUTE=1;
	static public final int REPEAT_FEATRUE=2;
	static public final String ENCODE_AS_ATTRIBUTE_TXT="EncodeAsAttribute";
	static public final String REPEAT_FEATRUE_TXT="RepeatFeature";
	private GeomAttrMapping(){}
	static public int valueOf(String value) {
		int mappingStrategy=ENCODE_AS_ATTRIBUTE;
		if(value!=null){
			if(value.equals(ENCODE_AS_ATTRIBUTE_TXT)){
				mappingStrategy=ENCODE_AS_ATTRIBUTE;
			}else if(value.equals(REPEAT_FEATRUE_TXT)){
				mappingStrategy=REPEAT_FEATRUE;
			}else{
				EhiLogger.logError("illegal GeomAttrMapping value <"+value+">");
			}
		}
		return mappingStrategy;
	}
	static public String toString(int value) {
		String mappingStrategyTxt=ENCODE_AS_ATTRIBUTE_TXT;
		if(value==ENCODE_AS_ATTRIBUTE){
			mappingStrategyTxt=ENCODE_AS_ATTRIBUTE_TXT;
		}else if(value==REPEAT_FEATRUE){
			mappingStrategyTxt=REPEAT_FEATRUE_TXT;
		}else{
			throw new IllegalArgumentException("GeomAttrMapping "+value);
		}
		return mappingStrategyTxt;
	}
}
