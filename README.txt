FARMER FIRST LOAD TRACKER - CORRECTED BUILD
Java Swing desktop application. No HTML/CSS/JavaScript or external libraries.

FIXES INCLUDED
- Harvest & Stock and Transport now use separate JTable references.
- Edit Selected and Delete Selected resolve the row selected in Harvest & Stock.
- Mark Delivered / Mark In Progress resolve the row selected in Transport.
- Row selection is converted from view row to model row before reading the record.
- Live Tracking load choices refresh when records are added, edited, or deleted.
- GPS map button opens Google Maps using entered latitude/longitude.
- Farmer Details and Family Details remain included.
- Existing CSV persistence remains: farmer_loads.csv and farmer_family.csv.

RUN
1. Install a JDK (Java 17 or later recommended).
2. Double-click run.bat, or open a terminal in this folder and run:
   javac -encoding UTF-8 -d out src\Main.java
   java -cp out Main

GPS NOTE
This Swing desktop app does not independently acquire GPS from a vehicle. Enter coordinates supplied by a GPS-enabled phone/device, then use Open GPS Location in Map. Opening Google Maps requires internet access and a browser.
