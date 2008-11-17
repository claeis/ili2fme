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

public class GeometryEncoding {
	static public final int OGC_HEXBIN=1;
	static public final int FME_XML=2;
	static public final int FME_BIN=3;
	static public final int FME_HEXBIN=4;
	static public final String OGC_HEXBIN_TXT="OGCHEXBIN";
	static public final String FME_XML_TXT="FMEXML";
	static public final String FME_BIN_TXT="FMEBIN";
	static public final String FME_HEXBIN_TXT="FMEHEXBIN";
	private GeometryEncoding(){}
	static public int valueOf(String value) {
		int mappingStrategy=FME_XML;
		if(value!=null){
			if(value.equals(OGC_HEXBIN_TXT)){
				mappingStrategy=OGC_HEXBIN;
			}else if(value.equals(FME_XML_TXT)){
				mappingStrategy=FME_XML;
			}else if(value.equals(FME_BIN_TXT)){
				mappingStrategy=FME_BIN;
			}else if(value.equals(FME_HEXBIN_TXT)){
				mappingStrategy=FME_HEXBIN;
			}else{
				EhiLogger.logError("illegal GeometryEncoding value <"+value+">");
			}
		}
		return mappingStrategy;
	}
	static public String toString(int value) {
		String mappingStrategyTxt=FME_XML_TXT;
		if(value==OGC_HEXBIN){
			mappingStrategyTxt=OGC_HEXBIN_TXT;
		}else if(value==FME_XML){
			mappingStrategyTxt=FME_XML_TXT;
		}else if(value==FME_BIN){
			mappingStrategyTxt=FME_BIN_TXT;
		}else if(value==FME_HEXBIN){
			mappingStrategyTxt=FME_HEXBIN_TXT;
		}else{
			throw new IllegalArgumentException("GeometryEncoding "+value);
		}
		return mappingStrategyTxt;
	}
}
