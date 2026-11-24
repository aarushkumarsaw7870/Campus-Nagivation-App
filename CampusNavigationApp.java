import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class CampusNavigationApp extends JFrame {

    static class Place {
        String name, info;
        int x, y;

        Place(String name, int x, int y, String info) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.info = info;
        }
    }

    static class Edge {
        String to;
        int distance;

        Edge(String to, int distance) {
            this.to = to;
            this.distance = distance;
        }
    }

    static class State {
        String name;
        int distance;

        State(String name, int distance) {
            this.name = name;
            this.distance = distance;
        }
    }

    Map<String, Place> places = new LinkedHashMap<>();
    Map<String, List<Edge>> graph = new LinkedHashMap<>();

    JComboBox<String> startBox;
    JComboBox<String> endBox;
    JComboBox<String> blockBox;
    JTextArea result;
    MapPanel mapPanel;
    JPanel controlsPanel, rightPanel;
    JLabel infoLabel;
    JButton themeBtn;

    List<String> currentPath = new ArrayList<>();
    boolean selectStartNext = true;
    String hoveredPlace = null;
    boolean darkMode = false;

    // Animation variables
    Timer animTimer;
    int animSegment = 0;
    double animProgress = 0.0;

    CampusNavigationApp() {
        setTitle("Campus Navigation App - Rungta University");
        setSize(1180, 740);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        addPlaces();
        addRoads();
        createInterface();
        setupAnimation();
        applyTheme();
    }

    void addPlaces() {
        addPlace("Main Gate", 90, 260, "Main Entrance & Security Checkpost");
        addPlace("Admin Block", 250, 130, "Admission Cell, Registrar & Accounts");
        addPlace("Academic Block", 420, 130, "Classrooms, Faculty Rooms & Seminar Hall");
        addPlace("Library", 610, 100, "Central Library & Digital Reading Zone");
        addPlace("Computer Lab", 610, 280, "CSE/AI Programming & Networking Labs");
        addPlace("Canteen", 420, 410, "Student Cafeteria & Refreshment Area");
        addPlace("Auditorium", 710, 450, "Main Event & Cultural Auditorium");
        addPlace("Parking", 190, 460, "Student & Faculty Vehicle Parking");
    }

    void addPlace(String name, int x, int y, String info) {
        places.put(name, new Place(name, x, y, info));
        graph.put(name, new ArrayList<>());
    }

    void addRoad(String a, String b, int distance) {
        graph.get(a).add(new Edge(b, distance));
        graph.get(b).add(new Edge(a, distance));
    }

    void addRoads() {
        addRoad("Main Gate", "Admin Block", 120);
        addRoad("Main Gate", "Parking", 90);
        addRoad("Admin Block", "Academic Block", 100);
        addRoad("Admin Block", "Canteen", 180);
        addRoad("Academic Block", "Library", 110);
        addRoad("Academic Block", "Computer Lab", 140);
        addRoad("Academic Block", "Canteen", 130);
        addRoad("Library", "Computer Lab", 150);
        addRoad("Computer Lab", "Auditorium", 170);
        addRoad("Canteen", "Auditorium", 190);
        addRoad("Parking", "Canteen", 160);
    }

    void createInterface() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
        top.setBackground(new Color(22, 38, 68));

        JLabel heading = new JLabel("CAMPUS NAVIGATION SYSTEM");
        heading.setForeground(Color.WHITE);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 22));
        top.add(heading);

        controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        startBox = new JComboBox<>(places.keySet().toArray(new String[0]));
        endBox = new JComboBox<>(places.keySet().toArray(new String[0]));

        blockBox = new JComboBox<>(new String[]{
            "None (All Roads Open)",
            "Admin Block <-> Academic Block",
            "Main Gate <-> Parking",
            "Academic Block <-> Canteen",
            "Computer Lab <-> Auditorium"
        });

        JButton find = styleButton("Find Shortest Route", new Color(26, 135, 84), Color.WHITE);
        JButton reset = styleButton("Reset", new Color(108, 117, 125), Color.WHITE);
        themeBtn = styleButton("Dark Mode", new Color(45, 65, 105), Color.WHITE);

        find.addActionListener(e -> findRoute());
        reset.addActionListener(e -> resetApp());
        themeBtn.addActionListener(e -> {
            darkMode = !darkMode;
            themeBtn.setText(darkMode ? "Light Mode" : "Dark Mode");
            applyTheme();
        });

        blockBox.addActionListener(e -> {
            if (!currentPath.isEmpty()) findRoute();
            else mapPanel.repaint();
        });

        controlsPanel.add(new JLabel("Start:"));
        controlsPanel.add(startBox);
        controlsPanel.add(new JLabel("Destination:"));
        controlsPanel.add(endBox);
        controlsPanel.add(new JLabel("Road Block:"));
        controlsPanel.add(blockBox);
        controlsPanel.add(find);
        controlsPanel.add(reset);
        controlsPanel.add(themeBtn);

        JPanel header = new JPanel(new BorderLayout());
        header.add(top, BorderLayout.NORTH);
        header.add(controlsPanel, BorderLayout.SOUTH);

        mapPanel = new MapPanel();

        result = new JTextArea();
        result.setEditable(false);
        result.setFont(new Font("Consolas", Font.PLAIN, 13));
        result.setBorder(new EmptyBorder(12, 12, 12, 12));
        result.setText("Welcome!\n\n1. Select Start & Destination\n   (or click dots on map).\n2. Test 'Road Block' to see\n   dynamic rerouting!\n3. Click Find Shortest Route.");

        JScrollPane scroll = new JScrollPane(result);
        scroll.setPreferredSize(new Dimension(305, 500));
        scroll.setBorder(BorderFactory.createEmptyBorder());

        infoLabel = new JLabel("  NAVIGATION DETAILS");
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        infoLabel.setBorder(new EmptyBorder(10, 8, 8, 8));

        rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(infoLabel, BorderLayout.NORTH);
        rightPanel.add(scroll, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(mapPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    JButton styleButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    void applyTheme() {
        Color panelBg = darkMode ? new Color(30, 34, 42) : new Color(245, 247, 250);
        Color textFg = darkMode ? new Color(230, 235, 240) : new Color(30, 35, 45);
        Color cardBg = darkMode ? new Color(24, 27, 34) : Color.WHITE;

        controlsPanel.setBackground(panelBg);
        for (Component c : controlsPanel.getComponents()) {
            if (c instanceof JLabel) c.setForeground(textFg);
        }

        rightPanel.setBackground(panelBg);
        infoLabel.setForeground(textFg);
        result.setBackground(cardBg);
        result.setForeground(textFg);

        mapPanel.setBackground(darkMode ? new Color(18, 22, 29) : new Color(242, 247, 243));
        mapPanel.repaint();
    }

    void setupAnimation() {
        animTimer = new Timer(25, e -> {
            if (currentPath.size() < 2) {
                animTimer.stop();
                return;
            }
            animProgress += 0.025;
            if (animProgress >= 1.0) {
                animProgress = 0.0;
                animSegment = (animSegment + 1) % (currentPath.size() - 1);
            }
            mapPanel.repaint();
        });
    }

    boolean isRoadBlocked(String a, String b) {
        String blocked = (String) blockBox.getSelectedItem();
        if (blocked == null || blocked.startsWith("None")) return false;
        String[] parts = blocked.split(" <-> ");
        return (a.equals(parts[0]) && b.equals(parts[1])) ||
               (a.equals(parts[1]) && b.equals(parts[0]));
    }

    void findRoute() {
        String start = (String) startBox.getSelectedItem();
        String end = (String) endBox.getSelectedItem();

        if (start.equals(end)) {
            JOptionPane.showMessageDialog(this, "Starting point and destination cannot be same!");
            return;
        }

        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> previous = new HashMap<>();

        for (String name : places.keySet()) dist.put(name, Integer.MAX_VALUE);

        PriorityQueue<State> pq = new PriorityQueue<>(Comparator.comparingInt(s -> s.distance));
        dist.put(start, 0);
        pq.add(new State(start, 0));

        while (!pq.isEmpty()) {
            State current = pq.poll();
            if (current.distance != dist.get(current.name)) continue;
            if (current.name.equals(end)) break;

            for (Edge edge : graph.get(current.name)) {
                if (isRoadBlocked(current.name, edge.to)) continue;
                int newDistance = current.distance + edge.distance;
                if (newDistance < dist.get(edge.to)) {
                    dist.put(edge.to, newDistance);
                    previous.put(edge.to, current.name);
                    pq.add(new State(edge.to, newDistance));
                }
            }
        }

        currentPath.clear();
        animTimer.stop();

        if (dist.get(end) == Integer.MAX_VALUE) {
            result.setText("NO ROUTE AVAILABLE!\n\nSelected road blockage\ncuts off the destination.");
            mapPanel.repaint();
            return;
        }

        String current = end;
        while (current != null) {
            currentPath.add(current);
            current = previous.get(current);
        }
        Collections.reverse(currentPath);

        int totalDist = dist.get(end);
        int walkMinutes = Math.max(1, (int) Math.ceil(totalDist / 80.0));

        StringBuilder output = new StringBuilder();
        output.append("SHORTEST ROUTE FOUND!\n");
        output.append("========================\n\n");
        output.append("From: ").append(start).append("\n");
        output.append("To:   ").append(end).append("\n");

        if (!((String) blockBox.getSelectedItem()).startsWith("None")) {
            output.append("Alert: Rerouted around\n       blocked road!\n");
        }

        output.append("\nTURN-BY-TURN PATH:\n\n");
        for (int i = 0; i < currentPath.size(); i++) {
            output.append((i + 1) + ". ").append(currentPath.get(i)).append("\n");
            if (i < currentPath.size() - 1) {
                output.append("     |\n     v\n");
            }
        }

        output.append("\n------------------------\n");
        output.append("Total Distance: ").append(totalDist).append(" m\n");
        output.append("Est. Walk Time: ~").append(walkMinutes).append(" min\n");
        output.append("Algorithm: Dijkstra");

        result.setText(output.toString());

        animSegment = 0;
        animProgress = 0.0;
        animTimer.start();
        mapPanel.repaint();
    }

    void resetApp() {
        animTimer.stop();
        startBox.setSelectedIndex(0);
        endBox.setSelectedIndex(0);
        blockBox.setSelectedIndex(0);
        currentPath.clear();
        selectStartNext = true;

        result.setText("Welcome!\n\n1. Select Start & Destination\n   (or click dots on map).\n2. Test 'Road Block' to see\n   dynamic rerouting!\n3. Click Find Shortest Route.");
        mapPanel.repaint();
    }

    class MapPanel extends JPanel {

        MapPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    for (Place p : places.values()) {
                        if (Math.hypot(e.getX() - p.x, e.getY() - p.y) <= 16) {
                            if (selectStartNext) {
                                startBox.setSelectedItem(p.name);
                                selectStartNext = false;
                            } else {
                                endBox.setSelectedItem(p.name);
                                selectStartNext = true;
                                if (!startBox.getSelectedItem().equals(endBox.getSelectedItem())) {
                                    findRoute();
                                }
                            }
                            repaint();
                            break;
                        }
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    String found = null;
                    for (Place p : places.values()) {
                        if (Math.hypot(e.getX() - p.x, e.getY() - p.y) <= 16) {
                            found = p.name;
                            break;
                        }
                    }
                    if (!Objects.equals(hoveredPlace, found)) {
                        hoveredPlace = found;
                        setCursor(found != null ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
                        repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Subtle grid background
            g2.setColor(darkMode ? new Color(28, 33, 42) : new Color(230, 236, 232));
            for (int gx = 0; gx < getWidth(); gx += 40) g2.drawLine(gx, 0, gx, getHeight());
            for (int gy = 0; gy < getHeight(); gy += 40) g2.drawLine(0, gy, getWidth(), gy);

            // Campus garden zone
            g2.setColor(darkMode ? new Color(25, 52, 42) : new Color(218, 238, 222));
            g2.fillRoundRect(275, 215, 230, 125, 30, 30);
            g2.setColor(darkMode ? new Color(95, 165, 130) : new Color(110, 160, 120));
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            g2.drawString("Central Campus Lawn", 332, 282);

            g2.setColor(darkMode ? new Color(230, 235, 245) : new Color(35, 45, 65));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g2.drawString("Rungta University Campus Map", 30, 35);

            // Draw double-layered roads & pill distance badges
            for (String name : graph.keySet()) {
                Place p1 = places.get(name);
                for (Edge edge : graph.get(name)) {
                    if (name.compareTo(edge.to) < 0) {
                        Place p2 = places.get(edge.to);
                        int midX = (p1.x + p2.x) / 2;
                        int midY = (p1.y + p2.y) / 2;

                        if (isRoadBlocked(name, edge.to)) {
                            Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{8}, 0);
                            g2.setStroke(dashed);
                            g2.setColor(new Color(235, 70, 70));
                            g2.drawLine(p1.x, p1.y, p2.x, p2.y);

                            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                            g2.drawString("BLOCKED", midX - 22, midY - 6);
                        } else {
                            // Outer border
                            g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                            g2.setColor(darkMode ? new Color(55, 65, 80) : new Color(180, 190, 200));
                            g2.drawLine(p1.x, p1.y, p2.x, p2.y);

                            // Inner fill
                            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                            g2.setColor(darkMode ? new Color(40, 46, 58) : new Color(250, 252, 255));
                            g2.drawLine(p1.x, p1.y, p2.x, p2.y);

                            // Pill badge for distance so text never overlaps line
                            String distText = edge.distance + "m";
                            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                            int w = g2.getFontMetrics().stringWidth(distText) + 8;
                            g2.setColor(darkMode ? new Color(35, 42, 54) : Color.WHITE);
                            g2.fillRoundRect(midX - w / 2, midY - 8, w, 16, 8, 8);
                            g2.setColor(darkMode ? new Color(75, 85, 105) : new Color(190, 200, 210));
                            g2.setStroke(new BasicStroke(1));
                            g2.drawRoundRect(midX - w / 2, midY - 8, w, 16, 8, 8);

                            g2.setColor(darkMode ? new Color(200, 210, 225) : new Color(60, 70, 85));
                            g2.drawString(distText, midX - w / 2 + 4, midY + 4);
                        }
                    }
                }
            }

            // Highlight selected route with glow
            if (currentPath.size() >= 2) {
                g2.setColor(new Color(30, 190, 105, 70));
                g2.setStroke(new BasicStroke(12, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 0; i < currentPath.size() - 1; i++) {
                    Place p1 = places.get(currentPath.get(i));
                    Place p2 = places.get(currentPath.get(i + 1));
                    g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                }

                g2.setColor(new Color(28, 175, 95));
                g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 0; i < currentPath.size() - 1; i++) {
                    Place p1 = places.get(currentPath.get(i));
                    Place p2 = places.get(currentPath.get(i + 1));
                    g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }

            // Animated moving traveler dot
            if (currentPath.size() >= 2 && animSegment < currentPath.size() - 1) {
                Place p1 = places.get(currentPath.get(animSegment));
                Place p2 = places.get(currentPath.get(animSegment + 1));
                int curX = (int) (p1.x + (p2.x - p1.x) * animProgress);
                int curY = (int) (p1.y + (p2.y - p1.y) * animProgress);

                g2.setColor(new Color(255, 190, 0, 110));
                g2.fillOval(curX - 12, curY - 12, 24, 24);
                g2.setColor(new Color(255, 145, 0));
                g2.fillOval(curX - 6, curY - 6, 12, 12);
            }

            // Draw locations with halo & S/D badges
            String selectedStart = (String) startBox.getSelectedItem();
            String selectedEnd = (String) endBox.getSelectedItem();

            for (Place p : places.values()) {
                boolean isStart = !currentPath.isEmpty() && p.name.equals(selectedStart);
                boolean isEnd = !currentPath.isEmpty() && p.name.equals(selectedEnd);

                if (isStart) {
                    g2.setColor(new Color(25, 180, 90, 75));
                    g2.fillOval(p.x - 16, p.y - 16, 32, 32);
                    g2.setColor(new Color(25, 180, 90));
                } else if (isEnd) {
                    g2.setColor(new Color(230, 60, 60, 75));
                    g2.fillOval(p.x - 16, p.y - 16, 32, 32);
                    g2.setColor(new Color(230, 60, 60));
                } else {
                    g2.setColor(new Color(55, 125, 235));
                }

                g2.fillOval(p.x - 10, p.y - 10, 20, 20);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(p.x - 10, p.y - 10, 20, 20);

                if (isStart || isEnd) {
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    g2.drawString(isStart ? "S" : "D", p.x - 3, p.y + 4);
                }

                g2.setColor(darkMode ? new Color(235, 240, 248) : new Color(25, 30, 40));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.drawString(p.name, p.x - 32, p.y - 18);
            }

            // Hover info tooltip card
            if (hoveredPlace != null) {
                Place hp = places.get(hoveredPlace);
                int boxX = Math.min(hp.x + 16, getWidth() - 255);
                g2.setColor(new Color(20, 32, 56, 240));
                g2.fillRoundRect(boxX, hp.y - 12, 240, 44, 12, 12);
                g2.setColor(new Color(80, 140, 240));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(boxX, hp.y - 12, 240, 44, 12, 12);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.drawString(hp.name, boxX + 10, hp.y + 6);
                g2.setColor(new Color(200, 215, 235));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.drawString(hp.info, boxX + 10, hp.y + 22);
            }

            // Legend at bottom
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            Color legendText = darkMode ? new Color(205, 215, 225) : Color.BLACK;

            g2.setColor(new Color(25, 180, 90));
            g2.fillOval(30, 560, 12, 12);
            g2.setColor(legendText);
            g2.drawString("Start (S)", 48, 570);

            g2.setColor(new Color(230, 60, 60));
            g2.fillOval(110, 560, 12, 12);
            g2.setColor(legendText);
            g2.drawString("Destination (D)", 128, 570);

            g2.setColor(new Color(255, 145, 0));
            g2.fillOval(230, 560, 12, 12);
            g2.setColor(legendText);
            g2.drawString("Live Simulation", 248, 570);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CampusNavigationApp app = new CampusNavigationApp();
            app.setVisible(true);
        });
    }
}