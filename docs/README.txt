ili2fme - INTERLIS 2-plugin for FME

Features
- reads INTERLIS 2 transfer files
- writes INTERLIS 2 transfer files
- reads INTERLIS 1 transfer files
- writes INTERLIS 1 transfer files
- writes GML transfer files (ILIGML profile)

Homepage
http://www.eisenhutinformatik.ch/interlis/ili2fme/

Status
this version of ili2fme is in stable state.

License
ili2fme is licensed under the LGPL (Lesser GNU Public License, see LICENSE.lgpl).
Some libraries used by ili2fme are licensed under MIT/X (see LICENSE.mitx).
Some libraries used by ili2fme are licensed under Apache 2.0 (see LICENSE.apache).
Some libraries used by ili2fme are licensed under a library specific license (LICENSE.antlr).

System Requirements
For the current version of ili2fme, you will need a JRE (Java Runtime Environment) installed on your system, version 1.6.0 or later.
The JRE (Java Runtime Environment) can be downloaded for free from the Website http://www.java.com/.


Installing ili2fme
To install ili2fme, choose a directory and extract the distribution file there.
Copy the files and subdirectories of "${ili2fme}/FME Suite" to your FME directory.
To use ili2fme with the FME Universal Viewer, FME requires you to set an
environment variable: FME_VIEWER_THREADING=SINGLE (select Windows Start menu::Control Panel::System::Advanced::Envirnoment Variables).
Add your commonly used Interlis models to the directory "${FME}/plugins/interlis2/ilimodels".
If you run out of JAVA heap space (only the case with huge geometries; huge datasets should not be a problem), you may set FME_JVM_MAX_HEAP_SIZE and/or FME_JVM_MIN_HEAP_SIZE to some value (e.g. 1024M).
On FME versions prior to 2006GB:
Add the contents of the file "${ili2fme}/formats/formats_db-preFME2006GB.txt" to the file "${FME}/formats.db".
Add the contents of the file "${ili2fme}/formats/gallery_db-preFME2006GB.txt" to the file "${FME}/gallery.db".

How to migrate/update an existing ili2fme installation
Just copy the files and subdirectories of the new "${ili2fme}/FME Suite" to your FME directory.

Running ili2fme
ili2fme is called by FME (if you select "Swiss INTERLIS (ili2fme)" as file format).
INTERLIS 2 is read/written if you select xtf or xml as file extension.
INTERLIS 1 is read/written if you select itf as file extension.
You should read .ili files (INTERLIS 1 or 2) to import feature types into the workbench.

Limitations
- custom line forms
- XTF line attributes
- recursive structure attributes

Comments/Suggestions
Please send comments to ce@eisenhutinformatik.ch.
