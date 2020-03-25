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

public class LinetableMapping {
    public static final int ILI1_LINETABLES_POLYGON = 1;
    public static final int ILI1_LINETABLES_POLYGONRAW = 2;
    public static final int ILI1_LINETABLES_RAW = 3;
    static public final String ILI1_LINETABLES_RAW_TXT="Raw";
    static public final String ILI1_LINETABLES_POLYGONRAW_TXT="Polygon+Raw";
    static public final String ILI1_LINETABLES_POLYGON_TXT="Polygon";
	private LinetableMapping(){}
	static public int valueOf(String value) {
		int mappingStrategy=ILI1_LINETABLES_POLYGON;
		if(value!=null){
			if(value.equals(ILI1_LINETABLES_POLYGON_TXT)){
				mappingStrategy=ILI1_LINETABLES_POLYGON;
			}else if(value.equals(ILI1_LINETABLES_RAW_TXT)){
				mappingStrategy=ILI1_LINETABLES_RAW;
            }else if(value.equals(ILI1_LINETABLES_POLYGONRAW_TXT)){
                mappingStrategy=ILI1_LINETABLES_POLYGONRAW;
			}else{
				EhiLogger.logError("illegal LinetableMapping value <"+value+">");
			}
		}
		return mappingStrategy;
	}
	static public String toString(int value) {
		String mappingStrategyTxt=ILI1_LINETABLES_POLYGON_TXT;
		if(value==ILI1_LINETABLES_POLYGON){
			mappingStrategyTxt=ILI1_LINETABLES_POLYGON_TXT;
		}else if(value==ILI1_LINETABLES_RAW){
			mappingStrategyTxt=ILI1_LINETABLES_RAW_TXT;
        }else if(value==ILI1_LINETABLES_POLYGONRAW){
            mappingStrategyTxt=ILI1_LINETABLES_POLYGONRAW_TXT;
		}else{
			throw new IllegalArgumentException("LinetableMapping "+value);
		}
		return mappingStrategyTxt;
	}
}
