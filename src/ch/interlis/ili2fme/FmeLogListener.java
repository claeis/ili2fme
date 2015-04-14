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

import COM.safe.fmeobjects.IFMELogFile;

import ch.ehi.basics.logging.*;
import java.util.*;

/** Bridge to log EhiLogger log-events to the FME-logging system. 
 * @author ce
 * @version $Revision: 1.0 $ $Date: 07.02.2005 $
 */
public class FmeLogListener implements LogListener {
	IFMELogFile out=null;
	private boolean errors=false;
	private int initCount=0;
	synchronized public void logEvent(LogEvent event){
		int fmeKind;
		switch(event.getEventKind()){
			case LogEvent.ERROR:
				errors=true;
				fmeKind=IFMELogFile.FME_ERROR;
				break;
			default:
				fmeKind=IFMELogFile.FME_INFORM;
				break;
		}
		ArrayList msgv=StdListener.formatOutput(event,true,true);
		Iterator msgi=msgv.iterator();
		while(msgi.hasNext()){
			String msg=(String)msgi.next();
			out.logMessageString(msg,fmeKind);
		}
	}
	public FmeLogListener(IFMELogFile out1){
		this.out=out1;
	}
	/** have there been errors logged?
	 */
	public boolean hasSeenErrors(){
		return errors;
	}
	public void incrCount(){
		initCount++;
	}
	public void decrCount(){
		initCount--;
	}
	public int getCount(){
		return initCount;
	}
	public void clearErrors() {
		errors=false;
	}
}
