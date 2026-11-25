import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;

/**
 * Farmer First Load Tracker
 * Complete Java-only desktop application (Swing + CSV file storage).
 * No HTML, CSS, JavaScript, external libraries, or database required.
 */
public class Main {
    private static final Color DARK = new Color(25, 65, 39);
    private static final Color GREEN = new Color(43, 115, 61);
    private static final Color LIGHT = new Color(237, 246, 238);
    private static final Color PAGE = new Color(247, 249, 247);
    private static final Color TEXT = new Color(45, 59, 47);
    private static final Color MUTED = new Color(107, 124, 110);
    private static final Color AMBER = new Color(190, 123, 35);
    private static final Font NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font MEDIUM = new Font("Segoe UI", Font.BOLD, 13);
    private static final Path DATA_FILE = Paths.get("farmer_loads.csv");

    private final JFrame frame = new JFrame("Farmer First | Load Tracker");
    private final JPanel pages = new JPanel(new CardLayout());
    private final CardLayout pageLayout = (CardLayout) pages.getLayout();
    private final List<LoadRecord> records = new ArrayList<>();
    private DefaultTableModel loadModel;
    private DefaultTableModel areaModel;
    private JTable loadTable;
    private JTable transportTable;
    private JTextArea reportText;
    private JLabel totalNumber, deliveredNumber, remainingNumber, areasNumber;
    private JTable farmerTable;
    private String currentPage = "Dashboard";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().launch());
    }

    private void launch() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        UIManager.put("Button.foreground", DARK);

        loadData();
        if (records.isEmpty()) addSampleData();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1040, 680));
        frame.setSize(1200, 760);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        frame.add(buildSidebar(), BorderLayout.WEST);
        pages.add(buildDashboard(), "Dashboard");
        pages.add(buildAreas(), "Areas");
        pages.add(buildLoads(), "Harvest & Stock");
        pages.add(buildTransport(), "Transport");
        pages.add(buildReports(), "Reports");
        pages.add(buildFarmers(), "Farmer Details");
        pages.add(buildFamily(), "Family Details");
        pages.add(buildTracking(), "Live Tracking");
        frame.add(pages, BorderLayout.CENTER);

        refreshAll();
        frame.setVisible(true);
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(DARK);
        side.setPreferredSize(new Dimension(228, 0));
        side.setBorder(new EmptyBorder(22, 14, 18, 14));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("🌿 Farmer First");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);P
        side.add(brand);

        JLabel sub = new JLabel("LOAD TRACKER");
        sub.setFont(new Font("Segoe UI", Font.BOLD, 10));
        sub.setForeground(new Color(190, 220, 196));
        sub.setBorder(new EmptyBorder(7, 2, 25, 0));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(sub);

        addNav(side, "▦   Dashboard", "Dashboard");
        addNav(side, "▤   Areas", "Areas");
        addNav(side, "▣   Harvest & Stock", "Harvest & Stock");
        addNav(side, "➜   Transport", "Transport");
        addNav(side, "▥   Reports", "Reports");
        addNav(side, "♙   Farmer Details", "Farmer Details");
        addNav(side, "⌂   Family Details", "Family Details");
        addNav(side, "◎   Live Tracking", "Live Tracking");

        side.add(Box.createVerticalGlue());
        JLabel footer = new JLabel("<html><font color='#C7DEC9'>Simple farm load management<br>Java Swing • Local CSV storage</font></html>");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(footer);
        return side;
    }

    private void addNav(JPanel side, String label, String destination) {
        JButton b = new JButton(label);
        b.setFont(MEDIUM);
        b.setForeground(Color.WHITE);
        b.setBackground(DARK);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setBorder(new EmptyBorder(12, 12, 12, 8));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addActionListener(e -> {
            currentPage = destination;
            pageLayout.show(pages, destination);
            refreshAll();
        });
        side.add(b);
        side.add(Box.createVerticalStrut(5));
    }

    private JPanel makePage(String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setBackground(PAGE);
        p.setBorder(new EmptyBorder(26, 28, 26, 28));
        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 27));
        t.setForeground(DARK);
        JLabel s = new JLabel(subtitle);
        s.setFont(NORMAL);
        s.setForeground(MUTED);
        s.setBorder(new EmptyBorder(5, 0, 0, 0));
        head.add(t);
        head.add(s);
        p.add(head, BorderLayout.NORTH);
        return p;
    }

    private JPanel buildDashboard() {
        JPanel p = makePage("Dashboard", "Overview of farm loads and delivery progress.");
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel stats = new JPanel(new GridLayout(1, 4, 13, 0));
        stats.setOpaque(false);
        totalNumber = new JLabel("0");
        deliveredNumber = new JLabel("0");
        remainingNumber = new JLabel("0");
        areasNumber = new JLabel("0");
        stats.add(statCard("TOTAL LOADS", totalNumber, GREEN, "Recorded loads"));
        stats.add(statCard("DELIVERED LOADS", deliveredNumber, new Color(37, 135, 73), "Completed"));
        stats.add(statCard("REMAINING LOADS", remainingNumber, AMBER, "Still pending"));
        stats.add(statCard("TOTAL AREAS", areasNumber, new Color(70, 109, 151), "Farm areas"));
        body.add(stats);
        body.add(Box.createVerticalStrut(18));
        body.add(new LoadChart());
        body.add(Box.createVerticalStrut(20));

        JLabel recent = new JLabel("Recent Load Records");
        recent.setFont(new Font("Segoe UI", Font.BOLD, 18));
        recent.setForeground(DARK);
        recent.setBorder(new EmptyBorder(0, 0, 10, 0));
        body.add(recent);
        JTable table = new JTable(new DefaultTableModel(
                new Object[]{"Area", "Crop", "Total", "Delivered", "Remaining", "Status", "Farmer"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        });
        styleTable(table);
        table.putClientProperty("dashboardTable", Boolean.TRUE);
        body.add(wrapTable(table));
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    private JPanel statCard(String title, JLabel number, Color accent, String caption) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                new EmptyBorder(15, 14, 15, 12)));
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        label.setForeground(MUTED);
        number.setFont(new Font("Segoe UI", Font.BOLD, 29));
        number.setForeground(accent);
        JLabel hint = new JLabel(caption);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(MUTED);
        JPanel mid = new JPanel(new BorderLayout());
        mid.setOpaque(false);
        mid.add(number, BorderLayout.CENTER);
        mid.add(hint, BorderLayout.SOUTH);
        card.add(label, BorderLayout.NORTH);
        card.add(mid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildAreas() {
        JPanel p = makePage("Area Management", "Add and review areas that supply loads.");
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton add = primary("＋ Add Area");
        add.addActionListener(e -> addArea());
        actions.add(add);
        body.add(actions, BorderLayout.NORTH);

        areaModel = new DefaultTableModel(
                new Object[]{"Area", "Crop Types", "Total Loads", "Delivered", "Remaining"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(areaModel);
        styleTable(table);
        body.add(wrapTable(table), BorderLayout.CENTER);
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildLoads() {
        JPanel p = makePage("Harvest & Stock", "Add load records, update quantities, or remove records.");
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton add = primary("＋ Add Load");
        add.addActionListener(e -> addLoad());
        JButton edit = secondary("Edit Selected");
        edit.addActionListener(e -> editSelected());
        JButton delete = secondary("Delete Selected");
        delete.addActionListener(e -> deleteSelected());
        actions.add(add);
        actions.add(edit);
        actions.add(delete);
        body.add(actions, BorderLayout.NORTH);
        loadTable = new JTable(loadModel());
        styleTable(loadTable);
        body.add(wrapTable(loadTable), BorderLayout.CENTER);
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildTransport() {
        JPanel p = makePage("Transport", "Select a record and update delivery status.");
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JLabel tip = new JLabel("Marking a load as Delivered sets delivered quantity equal to total quantity.");
        tip.setFont(NORMAL);
        tip.setForeground(MUTED);
        top.add(tip);
        top.add(Box.createVerticalStrut(10));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton done = primary("Mark Delivered");
        done.addActionListener(e -> setStatus("Delivered"));
        JButton pending = secondary("Mark In Progress");
        pending.addActionListener(e -> setStatus("In Progress"));
        actions.add(done);
        actions.add(pending);
        top.add(actions);
        body.add(top, BorderLayout.NORTH);
        JTable table = new JTable(loadModel());
        styleTable(table);
        transportTable = table;
        body.add(wrapTable(table), BorderLayout.CENTER);
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildFarmers() {
        JPanel p = makePage("Farmer Details", "Farmer contact list from your load records.");
        farmerTable = new JTable(new DefaultTableModel(new Object[]{"Farmer", "Phone", "Area", "Crop", "Harvest Date", "Delivery Date"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        });
        styleTable(farmerTable);
        p.add(wrapTable(farmerTable), BorderLayout.CENTER);
        return p;
    }

    private JTable familyTable;
    private DefaultTableModel familyModel;
    private JTextField gpsLatField, gpsLonField;
    private JComboBox<String> trackingLoadChoice, trackingStatusChoice;

    private JPanel buildFamily() {
        JPanel p = makePage("Family Details", "Manage farmer family contacts and emergency numbers.");
        familyModel = new DefaultTableModel(new Object[]{"Farmer", "Family Member", "Relationship", "Phone", "Emergency Contact"}, 0) { public boolean isCellEditable(int r,int c){return false;} };
        familyTable = new JTable(familyModel); styleTable(familyTable);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false);
        JButton add = primary("+ Add Family Contact");
        add.addActionListener(e -> {
            JTextField farmer=new JTextField(), member=new JTextField(), relation=new JTextField(), phone=new JTextField(), emergency=new JTextField();
            JPanel f=new JPanel(new GridLayout(0,2,8,8));
            f.add(new JLabel("Farmer name"));f.add(farmer); f.add(new JLabel("Family member"));f.add(member); f.add(new JLabel("Relationship"));f.add(relation); f.add(new JLabel("Phone number"));f.add(phone); f.add(new JLabel("Emergency contact"));f.add(emergency);
            if(JOptionPane.showConfirmDialog(frame,f,"Add Family Contact",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION){
                if(farmer.getText().trim().isEmpty()||member.getText().trim().isEmpty()){message("Farmer and family member names are required.");return;}
                familyModel.addRow(new Object[]{farmer.getText().trim(),member.getText().trim(),relation.getText().trim(),phone.getText().trim(),emergency.getText().trim()}); saveFamily();
            }
        });
        JButton del=secondary("Delete Selected"); del.addActionListener(e->{int r=familyTable.getSelectedRow();if(r>=0){familyModel.removeRow(r);saveFamily();}else message("Select a family contact first.");});
        actions.add(add);actions.add(del);p.add(actions,BorderLayout.NORTH);p.add(wrapTable(familyTable),BorderLayout.CENTER);loadFamily();return p;
    }
    private void loadFamily(){ if(familyModel==null)return; familyModel.setRowCount(0);Path f=Paths.get("farmer_family.csv");if(!Files.exists(f))return;try{for(String line:Files.readAllLines(f,StandardCharsets.UTF_8).subList(1,Files.readAllLines(f,StandardCharsets.UTF_8).size())){List<String> c=parseCsv(line);if(c.size()>=5)familyModel.addRow(c.toArray());}}catch(Exception ignored){} }
    private void saveFamily(){try(PrintWriter out=new PrintWriter(Files.newBufferedWriter(Paths.get("farmer_family.csv"),StandardCharsets.UTF_8))){out.println("Farmer,FamilyMember,Relationship,Phone,EmergencyContact");for(int i=0;i<familyModel.getRowCount();i++){for(int j=0;j<5;j++){if(j>0)out.print(",");out.print(csv(String.valueOf(familyModel.getValueAt(i,j))));}out.println();}}catch(IOException ex){message("Could not save family contacts: "+ex.getMessage());}}
    private JPanel buildTracking(){
        JPanel p=makePage("Live Tracking", "Select a delivery, preview its location, and open the point on a real map.");
        JPanel form=new JPanel(new GridLayout(0,2,10,10));form.setBackground(Color.WHITE);form.setBorder(new EmptyBorder(16,18,16,18));
        trackingLoadChoice=new JComboBox<>(); for(LoadRecord r:records)trackingLoadChoice.addItem(r.area+" | "+r.crop+" | "+r.farmer);
        trackingStatusChoice=new JComboBox<>(new String[]{"Picked Up","In Transit","Delivered","Delayed"});
        gpsLatField=new JTextField();gpsLonField=new JTextField();
        form.add(new JLabel("Load (area | crop | farmer)"));form.add(trackingLoadChoice);
        form.add(new JLabel("Delivery status"));form.add(trackingStatusChoice);
        form.add(new JLabel("GPS latitude"));form.add(gpsLatField);
        form.add(new JLabel("GPS longitude"));form.add(gpsLonField);
        MapPreview mapPreview=new MapPreview();
        Runnable refreshMap=()->{try{double lat=Double.parseDouble(gpsLatField.getText().trim()),lon=Double.parseDouble(gpsLonField.getText().trim());if(lat>=-90&&lat<=90&&lon>=-180&&lon<=180)mapPreview.setLocation(lat,lon);else mapPreview.clearLocation();}catch(Exception ex){mapPreview.clearLocation();}};
        gpsLatField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){public void insertUpdate(javax.swing.event.DocumentEvent e){refreshMap.run();}public void removeUpdate(javax.swing.event.DocumentEvent e){refreshMap.run();}public void changedUpdate(javax.swing.event.DocumentEvent e){refreshMap.run();}});
        gpsLonField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){public void insertUpdate(javax.swing.event.DocumentEvent e){refreshMap.run();}public void removeUpdate(javax.swing.event.DocumentEvent e){refreshMap.run();}public void changedUpdate(javax.swing.event.DocumentEvent e){refreshMap.run();}});
        JButton map=primary("Open Location in Google Maps");map.addActionListener(e->{try{double lat=Double.parseDouble(gpsLatField.getText().trim()),lon=Double.parseDouble(gpsLonField.getText().trim());if(lat < -90||lat>90||lon < -180||lon>180)throw new IllegalArgumentException();if(!Desktop.isDesktopSupported())throw new Exception("Desktop browser is unavailable.");Desktop.getDesktop().browse(new java.net.URI("https://www.google.com/maps?q="+lat+","+lon));}catch(Exception ex){message("Enter valid GPS coordinates (latitude -90 to 90, longitude -180 to 180). Map opening needs internet access.");}});
        JButton status=secondary("Update Selected Load Status");status.addActionListener(e->{int i=trackingLoadChoice.getSelectedIndex();if(i<0||i>=records.size()){message("No load selected.");return;}LoadRecord r=records.get(i);String v=String.valueOf(trackingStatusChoice.getSelectedItem());r.status=v;if(v.equals("Delivered"))r.delivered=r.total;saveData();refreshAll();message("Delivery status updated to "+v+".");});
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.LEFT));buttons.setOpaque(false);buttons.add(map);buttons.add(status);
        JPanel mapCard=new JPanel(new BorderLayout());mapCard.setBackground(Color.WHITE);mapCard.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220,230,221)),new EmptyBorder(8,8,8,8)));
        JLabel mapTitle=new JLabel("  Map Preview  •  Marker shows the entered coordinates");mapTitle.setFont(new Font("SansSerif",Font.BOLD,14));mapTitle.setBorder(new EmptyBorder(4,4,10,4));mapCard.add(mapTitle,BorderLayout.NORTH);mapCard.add(mapPreview,BorderLayout.CENTER);
        JLabel note=new JLabel("Enter coordinates from your GPS/device. This preview is illustrative; Google Maps opens the real map. GPS is not collected automatically.");note.setForeground(MUTED);note.setBorder(new EmptyBorder(8,2,2,2));
        JPanel center=new JPanel(new BorderLayout(0,8));center.setOpaque(false);center.add(form,BorderLayout.NORTH);center.add(mapCard,BorderLayout.CENTER);JPanel south=new JPanel(new BorderLayout());south.setOpaque(false);south.add(buttons,BorderLayout.NORTH);south.add(note,BorderLayout.SOUTH);center.add(south,BorderLayout.SOUTH);p.add(center,BorderLayout.CENTER);return p;
    }

    private static class MapPreview extends JPanel {
        private Double latitude=null,longitude=null;
        MapPreview(){setPreferredSize(new Dimension(600,220));setMinimumSize(new Dimension(300,170));setBackground(new Color(235,242,226));}
        void setLocation(double lat,double lon){latitude=lat;longitude=lon;repaint();}
        void clearLocation(){latitude=null;longitude=null;repaint();}
        protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);int w=getWidth(),h=getHeight();
            g2.setColor(new Color(225,237,216));g2.fillRect(0,0,w,h);
            g2.setColor(new Color(198,222,190));g2.fillOval(w/10,h/5,w/4,h/3);g2.fillOval(w*2/3,h/2,w/3,h/2);g2.fillOval(w/2,h/12,w/5,h/4);
            g2.setStroke(new BasicStroke(13f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));g2.setColor(new Color(255,255,255));g2.drawLine(-20,h*3/4,w+20,h/3);g2.drawLine(w/4,-20,w*3/4,h+20);g2.drawLine(-20,h/4,w+20,h*4/5);
            g2.setStroke(new BasicStroke(2f));g2.setColor(new Color(224,190,119));g2.drawLine(-20,h*3/4,w+20,h/3);g2.drawLine(w/4,-20,w*3/4,h+20);g2.drawLine(-20,h/4,w+20,h*4/5);
            if(latitude!=null&&longitude!=null){int x=(int)(w*(0.12+((longitude+180)/360.0)*0.76));int y=(int)(h*(0.12+((90-latitude)/180.0)*0.76));x=Math.max(20,Math.min(w-20,x));y=Math.max(20,Math.min(h-20,y));g2.setColor(new Color(214,57,57));g2.fillOval(x-10,y-10,20,20);g2.setColor(Color.WHITE);g2.fillOval(x-4,y-4,8,8);g2.setColor(new Color(45,70,48));g2.setFont(new Font("SansSerif",Font.BOLD,12));g2.drawString(String.format(java.util.Locale.US,"Location: %.5f, %.5f",latitude,longitude),12,h-12);}else{g2.setColor(new Color(60,86,64));g2.setFont(new Font("SansSerif",Font.BOLD,14));String msg="Enter latitude and longitude to preview the marker";g2.drawString(msg,Math.max(10,(w-g2.getFontMetrics().stringWidth(msg))/2),h/2);}g2.dispose();}
    }

    private static class LoadChart extends JPanel {
        LoadChart() { setPreferredSize(new Dimension(500, 145)); setBackground(Color.WHITE); setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(222,232,223)), new EmptyBorder(12,16,12,16))); }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(new Font("Segoe UI",Font.BOLD,15)); g2.setColor(DARK); g2.drawString("Load Progress Chart",16,24);
            int total=0,del=0; for(LoadRecord r:chartRecords){total+=r.total;del+=r.delivered;}
            int max=Math.max(1,total); int x=20,y=48,w=Math.max(120,getWidth()-55),h=20;
            g2.setColor(new Color(229,238,230));g2.fillRoundRect(x,y,w,h,10,10);
            g2.setColor(GREEN);g2.fillRoundRect(x,y,(int)(w*Math.min(1.0,(double)del/max)),h,10,10);
            g2.setFont(new Font("Segoe UI",Font.PLAIN,12));g2.setColor(TEXT);g2.drawString("Delivered: "+del,20,90);g2.drawString("Remaining: "+(total-del),130,90);g2.drawString("Total: "+total,260,90);g2.dispose();
        }
        static java.util.List<LoadRecord> chartRecords = java.util.Collections.emptyList();
    }

    private JPanel buildReports() {
        JPanel p = makePage("Reports", "Review summary and export the current data as a CSV file.");
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        reportText = new JTextArea();
        reportText.setEditable(false);
        reportText.setFont(new Font("Consolas", Font.PLAIN, 14));
        reportText.setForeground(TEXT);
        reportText.setBackground(Color.WHITE);
        reportText.setBorder(new EmptyBorder(18, 18, 18, 18));
        body.add(new JScrollPane(reportText), BorderLayout.CENTER);
        JButton export = primary("Export CSV Report");
        export.addActionListener(e -> exportCsv());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        bottom.add(export);
        body.add(bottom, BorderLayout.SOUTH);
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    private DefaultTableModel loadModel() {
        return new DefaultTableModel(
                new Object[]{"Area", "Crop", "Total Loads", "Delivered", "Remaining", "Status", "Farmer", "Phone", "Harvest Date", "Delivery Date"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private JPanel wrapTable(JTable table) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(222, 232, 223)));
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(34);
        table.setFont(NORMAL);
        table.setForeground(TEXT);
        table.setGridColor(new Color(230, 237, 230));
        table.setSelectionBackground(new Color(218, 237, 221));
        table.setSelectionForeground(DARK);
        table.getTableHeader().setFont(MEDIUM);
        table.getTableHeader().setForeground(DARK);
        table.getTableHeader().setBackground(LIGHT);
        table.setFillsViewportHeight(true);
    }

    private JButton primary(String label) {
        JButton b = new JButton(label);
        b.setFont(MEDIUM);
        b.setForeground(Color.WHITE);
        b.setBackground(GREEN);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 15, 10, 15));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton secondary(String label) {
        JButton b = new JButton(label);
        b.setFont(MEDIUM);
        b.setForeground(GREEN);
        b.setBackground(LIGHT);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 15, 10, 15));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void addArea() {
        String name = JOptionPane.showInputDialog(frame, "Enter area name:");
        if (name == null) return;
        name = name.trim();
        if (name.isEmpty()) { message("Area name cannot be empty."); return; }
        for (LoadRecord r : records) {
            if (r.area.equalsIgnoreCase(name)) {
                message("This area already exists.");
                return;
            }
        }
        records.add(new LoadRecord(name, "Not specified", 0, 0, "In Progress", "-"));
        saveData();
        refreshAll();
        message("Area added successfully.");
    }

    private void addLoad() {
        JTextField area = new JTextField();
        JTextField crop = new JTextField();
        JTextField total = new JTextField();
        JTextField delivered = new JTextField("0");
        JTextField farmer = new JTextField();
        JTextField phone = new JTextField();
        JTextField harvestDate = new JTextField(java.time.LocalDate.now().toString());
        JTextField deliveryDate = new JTextField();
        JComboBox<String> status = new JComboBox<>(new String[]{"In Progress", "Delivered"});
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Area name:")); form.add(area);
        form.add(new JLabel("Crop / product:")); form.add(crop);
        form.add(new JLabel("Total loads:")); form.add(total);
        form.add(new JLabel("Delivered loads:")); form.add(delivered);
        form.add(new JLabel("Farmer name:")); form.add(farmer);
        form.add(new JLabel("Farmer phone:")); form.add(phone);
        form.add(new JLabel("Harvest date (YYYY-MM-DD):")); form.add(harvestDate);
        form.add(new JLabel("Expected delivery date:")); form.add(deliveryDate);
        form.add(new JLabel("Delivery status:")); form.add(status);

        int choice = JOptionPane.showConfirmDialog(frame, form, "Add Load Record",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;
        try {
            String a = area.getText().trim(), c = crop.getText().trim(), f = farmer.getText().trim();
            int t = Integer.parseInt(total.getText().trim());
            int d = Integer.parseInt(delivered.getText().trim());
            if (a.isEmpty() || c.isEmpty() || f.isEmpty() || t < 0 || d < 0 || d > t)
                throw new IllegalArgumentException();
            String s = (String) status.getSelectedItem();
            if ("Delivered".equals(s)) d = t;
            records.add(new LoadRecord(a, c, t, d, s, f, phone.getText().trim(), harvestDate.getText().trim(), deliveryDate.getText().trim()));
            saveData();
            refreshAll();
            message("Load record added.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    "Please enter valid values. Delivered loads must be between 0 and total loads.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LoadRecord selectedRecord() {
        return selectedRecordFrom(loadTable);
    }

    private LoadRecord selectedTransportRecord() {
        return selectedRecordFrom(transportTable);
    }

    private LoadRecord selectedRecordFrom(JTable table) {
        if (table == null) { message("Load table is not available."); return null; }
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { message("Please select a load record first."); return null; }
        int row = table.convertRowIndexToModel(viewRow);
        // The load tables are rebuilt from records in the same order by refreshAll().
        if (row < 0 || row >= records.size()) {
            message("Could not identify the selected record. Refresh the page and try again.");
            return null;
        }
        return records.get(row);
    }

    private void editSelected() {
        LoadRecord r = selectedRecord();
        if (r == null) return;
        JTextField area = new JTextField(r.area);
        JTextField crop = new JTextField(r.crop);
        JTextField total = new JTextField(String.valueOf(r.total));
        JTextField delivered = new JTextField(String.valueOf(r.delivered));
        JTextField farmer = new JTextField(r.farmer);
        JTextField phone = new JTextField(r.phone);
        JTextField harvestDate = new JTextField(r.harvestDate);
        JTextField deliveryDate = new JTextField(r.deliveryDate);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Area name:")); form.add(area);
        form.add(new JLabel("Crop / product:")); form.add(crop);
        form.add(new JLabel("Total loads:")); form.add(total);
        form.add(new JLabel("Delivered loads:")); form.add(delivered);
        form.add(new JLabel("Farmer name:")); form.add(farmer);
        form.add(new JLabel("Farmer phone:")); form.add(phone);
        form.add(new JLabel("Harvest date (YYYY-MM-DD):")); form.add(harvestDate);
        form.add(new JLabel("Expected delivery date:")); form.add(deliveryDate);
        int choice = JOptionPane.showConfirmDialog(frame, form, "Edit Load Record",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;
        try {
            String a = area.getText().trim(), c = crop.getText().trim(), f = farmer.getText().trim();
            int t = Integer.parseInt(total.getText().trim());
            int d = Integer.parseInt(delivered.getText().trim());
            if (a.isEmpty() || c.isEmpty() || f.isEmpty() || t < 0 || d < 0 || d > t)
                throw new IllegalArgumentException();
            r.area = a; r.crop = c; r.total = t; r.delivered = d; r.farmer = f;
            r.phone = phone.getText().trim(); r.harvestDate = harvestDate.getText().trim(); r.deliveryDate = deliveryDate.getText().trim();
            r.status = d == t ? "Delivered" : "In Progress";
            saveData(); refreshAll(); message("Record updated.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Please enter valid values.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        LoadRecord r = selectedRecord();
        if (r == null) return;
        int choice = JOptionPane.showConfirmDialog(frame, "Delete the selected load record?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            records.remove(r);
            saveData(); refreshAll();
        }
    }

    private void setStatus(String status) {
        LoadRecord r = selectedTransportRecord();
        if (r == null) return;
        r.status = status;
        if ("Delivered".equals(status)) r.delivered = r.total;
        else if (r.delivered >= r.total && r.total > 0) r.delivered = Math.max(0, r.total - 1);
        saveData(); refreshAll();
    }

    private void refreshAll() {
        int total = 0, delivered = 0;
        for (LoadRecord r : records) { total += r.total; delivered += r.delivered; }
        if (totalNumber != null) totalNumber.setText(String.valueOf(total));
        if (deliveredNumber != null) deliveredNumber.setText(String.valueOf(delivered));
        if (remainingNumber != null) remainingNumber.setText(String.valueOf(Math.max(0, total - delivered)));
        if (areasNumber != null) areasNumber.setText(String.valueOf(uniqueAreas().size()));

        if (areaModel != null) {
            areaModel.setRowCount(0);
            for (String area : uniqueAreas()) {
                int t = 0, d = 0;
                Set<String> crops = new LinkedHashSet<>();
                for (LoadRecord r : records) if (r.area.equals(area)) {
                    t += r.total; d += r.delivered; crops.add(r.crop);
                }
                areaModel.addRow(new Object[]{area, String.join(", ", crops), t, d, t - d});
            }
        }

        refreshTables(pages);
        if (reportText != null) reportText.setText(makeReport());
        LoadChart.chartRecords = new ArrayList<>(records);
        pages.repaint();
        if (farmerTable != null) { DefaultTableModel fm=(DefaultTableModel)farmerTable.getModel(); fm.setRowCount(0); for (LoadRecord r:records) fm.addRow(new Object[]{r.farmer,r.phone,r.area,r.crop,r.harvestDate,r.deliveryDate}); }
        if (trackingLoadChoice != null) {
            int oldIndex = trackingLoadChoice.getSelectedIndex();
            trackingLoadChoice.removeAllItems();
            for (LoadRecord r : records) trackingLoadChoice.addItem(r.area + " | " + r.crop + " | " + r.farmer);
            if (!records.isEmpty()) trackingLoadChoice.setSelectedIndex(Math.max(0, Math.min(oldIndex, records.size() - 1)));
        }
    }

    private void refreshTables(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JTable) {
                JTable t = (JTable) component;
                Object dash = t.getClientProperty("dashboardTable");
                if (Boolean.TRUE.equals(dash)) fillModel((DefaultTableModel)t.getModel());
            }
            if (component instanceof JScrollPane) {
                Component view = ((JScrollPane)component).getViewport().getView();
                if (view instanceof JTable) {
                    JTable t = (JTable)view;
                    if (t.getModel() instanceof DefaultTableModel) {
                        DefaultTableModel m = (DefaultTableModel)t.getModel();
                        if (m.getColumnCount() >= 7) fillModel(m);
                    }
                }
            }
            if (component instanceof Container) refreshTables((Container)component);
        }
    }

    private void fillModel(DefaultTableModel model) {
        model.setRowCount(0);
        for (LoadRecord r : records)
            model.addRow(new Object[]{r.area, r.crop, r.total, r.delivered, r.remaining(), r.status, r.farmer, r.phone, r.harvestDate, r.deliveryDate});
    }

    private String makeReport() {
        int total = 0, delivered = 0;
        for (LoadRecord r : records) { total += r.total; delivered += r.delivered; }
        StringBuilder s = new StringBuilder();
        s.append("FARMER FIRST LOAD TRACKER REPORT\n");
        s.append("========================================\n\n");
        s.append("Total loads      : ").append(total).append('\n');
        s.append("Delivered loads  : ").append(delivered).append('\n');
        s.append("Remaining loads  : ").append(total - delivered).append('\n');
        s.append("Total areas      : ").append(uniqueAreas().size()).append("\n\n");
        s.append("AREA-WISE SUMMARY\n");
        s.append("----------------------------------------\n");
        for (String area : uniqueAreas()) {
            int t = 0, d = 0;
            for (LoadRecord r : records) if (r.area.equals(area)) { t += r.total; d += r.delivered; }
            s.append(String.format(Locale.ROOT, "%-20s total %3d | delivered %3d | remaining %3d%n",
                    area, t, d, t-d));
        }
        s.append("\nLOAD DETAILS\n");
        s.append("----------------------------------------\n");
        for (LoadRecord r : records)
            s.append(r.area).append(" | ").append(r.crop).append(" | total ").append(r.total)
                    .append(" | delivered ").append(r.delivered).append(" | remaining ").append(r.remaining())
                    .append(" | ").append(r.status).append(" | ").append(r.farmer).append(" | harvest ").append(r.harvestDate).append(" | delivery ").append(r.deliveryDate).append('\n');
        return s.toString();
    }

    private List<String> uniqueAreas() {
        List<String> areas = new ArrayList<>();
        for (LoadRecord r : records) if (!areas.contains(r.area)) areas.add(r.area);
        return areas;
    }

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save load report");
        chooser.setSelectedFile(new File("farmer_load_report.csv"));
        if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(chooser.getSelectedFile()), StandardCharsets.UTF_8))) {
            out.println("Area,Crop,Total Loads,Delivered,Remaining,Status,Farmer,Phone,Harvest Date,Delivery Date");
            for (LoadRecord r : records) {
                out.println(csv(r.area) + "," + csv(r.crop) + "," + r.total + "," + r.delivered + ","
                        + r.remaining() + "," + csv(r.status) + "," + csv(r.farmer) + "," + csv(r.phone) + "," + csv(r.harvestDate) + "," + csv(r.deliveryDate));
            }
            message("Report exported successfully.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Unable to export: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String csv(String value) { return "\"" + value.replace("\"", "\"\"") + "\""; }

    private void loadData() {
        if (!Files.exists(DATA_FILE)) return;
        try {
            List<String> lines = Files.readAllLines(DATA_FILE, StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) {
                List<String> cols = parseCsv(lines.get(i));
                if (cols.size() >= 6) {
                    String area = cols.get(0), crop = cols.get(1);
                    int total = Integer.parseInt(cols.get(2));
                    int delivered = Integer.parseInt(cols.get(3));
                    String status = cols.get(4), farmer = cols.get(5);
                    records.add(new LoadRecord(area, crop, total, delivered, status, farmer, cols.size()>6?cols.get(6):"", cols.size()>7?cols.get(7):"", cols.size()>8?cols.get(8):""));
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Could not read saved data. Starting with demo data.\n" + ex.getMessage());
            records.clear();
        }
    }

    private void saveData() {
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(DATA_FILE, StandardCharsets.UTF_8))) {
            out.println("Area,Crop,Total,Delivered,Status,Farmer,Phone,HarvestDate,DeliveryDate");
            for (LoadRecord r : records) {
                out.println(csv(r.area) + "," + csv(r.crop) + "," + r.total + "," + r.delivered + ","
                        + csv(r.status) + "," + csv(r.farmer) + "," + csv(r.phone) + "," + csv(r.harvestDate) + "," + csv(r.deliveryDate));
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Could not save data: " + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<String> parseCsv(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    value.append('"'); i++;
                } else quoted = !quoted;
            } else if (c == ',' && !quoted) {
                result.add(value.toString()); value.setLength(0);
            } else value.append(c);
        }
        result.add(value.toString());
        return result;
    }

    private void addSampleData() {
        records.add(new LoadRecord("North Area", "Paddy", 30, 20, "In Progress", "Murugan"));
        records.add(new LoadRecord("South Area", "Vegetables", 25, 15, "In Progress", "Selvi"));
        records.add(new LoadRecord("East Area", "Coconut", 20, 20, "Delivered", "Kumar"));
        records.add(new LoadRecord("West Area", "Paddy", 45, 20, "In Progress", "Meena"));
        saveData();
    }

    private void message(String text) {
        JOptionPane.showMessageDialog(frame, text, "Farmer First", JOptionPane.INFORMATION_MESSAGE);
    }

    private static class LoadRecord {
        String area, crop, status, farmer, phone, harvestDate, deliveryDate;
        int total, delivered;
        LoadRecord(String area, String crop, int total, int delivered, String status, String farmer) { this(area,crop,total,delivered,status,farmer,"",java.time.LocalDate.now().toString(),""); }
        LoadRecord(String area, String crop, int total, int delivered, String status, String farmer, String phone, String harvestDate, String deliveryDate) {
            this.area = area; this.crop = crop; this.total = total; this.delivered = delivered; this.status = status; this.farmer = farmer; this.phone=phone; this.harvestDate=harvestDate; this.deliveryDate=deliveryDate;
        }
        int remaining() { return Math.max(0, total - delivered); }
    }
}
