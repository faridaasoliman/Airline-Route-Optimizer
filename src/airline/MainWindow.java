package airline;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * MainWindow — the main JFrame with five tabs:
 *   1. Route Network   2. Dijkstra   3. MST   4. Cargo   5. Comparison
 */
public class MainWindow extends JFrame {

    // ── Shared canvas (reused across tabs) ──────────────────────────────────
    private NetworkCanvas netCanvas   = new NetworkCanvas();
    private NetworkCanvas dijkCanvas  = new NetworkCanvas();
    private NetworkCanvas mstCanvas   = new NetworkCanvas();
    private CargoCanvas   cargoCanvas = new CargoCanvas();

    // ── Comparison data store ─────────────────────────────────────────────────
    private final Map<String, String> cmpData = new LinkedHashMap<>();
    private DefaultTableModel reportModel;

    // ── Colours / fonts ───────────────────────────────────────────────────────
    private static final Color BG      = new Color(6,  11, 24);
    private static final Color BG2     = new Color(13, 22, 48);
    private static final Color BG3     = new Color(14, 28, 55);
    private static final Color BORDER  = new Color(30, 58, 95);
    private static final Color ACCENT  = new Color(0,  200, 255);
    private static final Color ACCENT2 = new Color(255, 107, 53);
    private static final Color ACCENT3 = new Color(0,  255, 157);
    private static final Color TEXT    = new Color(200, 220, 240);
    private static final Color DIMTEXT = new Color(90, 130, 180);

    private static final Font FONT_TITLE  = new Font("Monospaced", Font.BOLD, 11);
    private static final Font FONT_BODY   = new Font("SansSerif",  Font.PLAIN, 12);
    private static final Font FONT_MONO   = new Font("Monospaced", Font.PLAIN, 12);
    private static final Font FONT_RESULT = new Font("Monospaced", Font.PLAIN, 13);

    public MainWindow() {
        super("✈  SkyRoute AI — Airline Route + Cargo Optimizer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 760);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);

        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildHeader(),   BorderLayout.NORTH);
        add(buildTabbedPane(), BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HEADER
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        header.setBackground(new Color(8, 16, 32));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        JLabel icon  = new JLabel("✈");
        icon.setFont(new Font("SansSerif", Font.PLAIN, 30));
        icon.setForeground(ACCENT);

        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        JLabel title = new JLabel("SKYROUTE AI");
        title.setFont(new Font("Monospaced", Font.BOLD, 18));
        title.setForeground(ACCENT);
        JLabel sub = new JLabel("Airline Route + Cargo Optimizer  •  Computing Algorithms Project 5");
        sub.setFont(new Font("Monospaced", Font.PLAIN, 10));
        sub.setForeground(DIMTEXT);
        titles.add(title);
        titles.add(sub);

        header.add(icon);
        header.add(titles);
        return header;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TABBED PANE
    // ─────────────────────────────────────────────────────────────────────────
    private JTabbedPane buildTabbedPane() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG);
        tabs.setForeground(TEXT);
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));

        UIManager.put("TabbedPane.selected",              BG3);
        UIManager.put("TabbedPane.background",            BG2);
        UIManager.put("TabbedPane.foreground",            TEXT);
        UIManager.put("TabbedPane.selectedForeground",    ACCENT);
        UIManager.put("TabbedPane.contentBorderInsets",   new Insets(0,0,0,0));

        tabs.addTab("🗺  Route Network",  buildNetworkTab());
        tabs.addTab("📍 Dijkstra",         buildDijkstraTab());
        tabs.addTab("🌐 MST",              buildMSTTab());
        tabs.addTab("📦 Cargo",            buildCargoTab());
        tabs.addTab("📊 Comparison",       buildCompareTab());

        return tabs;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TAB 1 — ROUTE NETWORK
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildNetworkTab() {
        JPanel tab = darkPanel(new BorderLayout(12, 0));
        tab.setBorder(pad(12));

        // Canvas side
        JPanel canvasWrap = darkCard();
        canvasWrap.setLayout(new BorderLayout());
        canvasWrap.add(sectionTitle("✈  AIRPORT ROUTE NETWORK"), BorderLayout.NORTH);
        canvasWrap.add(netCanvas, BorderLayout.CENTER);
        canvasWrap.add(legend(), BorderLayout.SOUTH);

        // Sidebar
        JPanel side = buildSidePanel();
        side.add(sectionTitle("📋  AIRPORTS"));
        side.add(Box.createVerticalStrut(6));
        side.add(scrollPane(buildAirportList()));
        side.add(Box.createVerticalStrut(10));
        side.add(sectionTitle("🔗  ROUTES"));
        side.add(Box.createVerticalStrut(6));
        side.add(scrollPane(buildRouteList()));

        tab.add(canvasWrap, BorderLayout.CENTER);
        tab.add(wrappedSide(side), BorderLayout.EAST);
        return tab;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TAB 2 — DIJKSTRA
    // ─────────────────────────────────────────────────────────────────────────
    private JTextArea dijkResult;
    private JComboBox<String> dijkFrom, dijkTo;
    private JList<String> dijkDistList;
    private DefaultListModel<String> dijkDistModel = new DefaultListModel<>();

    private JPanel buildDijkstraTab() {
        JPanel tab = darkPanel(new BorderLayout(12, 0));
        tab.setBorder(pad(12));

        JPanel canvasWrap = darkCard();
        canvasWrap.setLayout(new BorderLayout());
        canvasWrap.add(sectionTitle("📍  DIJKSTRA — SHORTEST FLIGHT PATH"), BorderLayout.NORTH);
        canvasWrap.add(dijkCanvas, BorderLayout.CENTER);
        canvasWrap.add(legend(), BorderLayout.SOUTH);

        // Controls
        JPanel side = buildSidePanel();
        side.add(sectionTitle("⚙  CONTROLS"));
        side.add(Box.createVerticalStrut(8));

        side.add(mono("From airport:"));
        dijkFrom = styledCombo();
        side.add(dijkFrom);
        side.add(Box.createVerticalStrut(6));

        side.add(mono("To airport:"));
        dijkTo = styledCombo();
        side.add(dijkTo);
        side.add(Box.createVerticalStrut(10));

        for (Airport a : DataStore.AIRPORTS) {
            dijkFrom.addItem(a.code + " — " + a.name);
            dijkTo  .addItem(a.code + " — " + a.name);
        }
        dijkTo.setSelectedIndex(1);

        JButton btnRun = accentButton("🚀  Find Shortest Path", ACCENT);
        btnRun.addActionListener(e -> runDijkstra());
        side.add(btnRun);

        side.add(Box.createVerticalStrut(12));
        side.add(sectionTitle("📊  RESULT"));
        side.add(Box.createVerticalStrut(6));
        dijkResult = resultArea();
        side.add(scrollPane(dijkResult));

        side.add(Box.createVerticalStrut(10));
        side.add(sectionTitle("🗺  ALL DISTANCES FROM SOURCE"));
        side.add(Box.createVerticalStrut(6));
        dijkDistList = new JList<>(dijkDistModel);
        styleList(dijkDistList);
        side.add(scrollPane(dijkDistList));

        tab.add(canvasWrap, BorderLayout.CENTER);
        tab.add(wrappedSide(side), BorderLayout.EAST);
        return tab;
    }

    private void runDijkstra() {
        int src = dijkFrom.getSelectedIndex();
        int dst = dijkTo  .getSelectedIndex();

        Algorithms.DijkstraResult res = Algorithms.dijkstra(src);
        List<Integer> path = res.getPath(dst);

        // Update canvas
        dijkCanvas.showDijkstra(path, src, dst);

        // Result text
        if (res.dist[dst] == Integer.MAX_VALUE) {
            dijkResult.setText("No path found between\n"
                + DataStore.AIRPORTS[src].name + " → " + DataStore.AIRPORTS[dst].name);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("FROM : ").append(DataStore.AIRPORTS[src].name).append("\n");
            sb.append("TO   : ").append(DataStore.AIRPORTS[dst].name).append("\n");
            sb.append("COST : $").append(res.dist[dst]).append("k\n");
            sb.append("HOPS : ").append(path.size() - 1).append("\n");
            sb.append("TIME : ").append(res.execTimeMs).append(" ms\n\n");
            sb.append("PATH : ");
            for (int i = 0; i < path.size(); i++) {
                if (i > 0) sb.append(" → ");
                sb.append(DataStore.AIRPORTS[path.get(i)].code);
            }
            dijkResult.setText(sb.toString());
        }

        // Distance list
        dijkDistModel.clear();
        for (int i = 0; i < DataStore.AIRPORTS.length; i++) {
            String cost = (res.dist[i] == Integer.MAX_VALUE) ? "∞" : "$" + res.dist[i] + "k";
            dijkDistModel.addElement(DataStore.AIRPORTS[i].code + " — " + DataStore.AIRPORTS[i].name
                + "   →   " + cost);
        }

        // Comparison data
        cmpData.put("dijkstra", "From: " + DataStore.AIRPORTS[src].code
            + " → To: " + DataStore.AIRPORTS[dst].code
            + "\nCost: $" + res.dist[dst] + "k"
            + "\nExec: " + res.execTimeMs + " ms");
        updateReportTable("Dijkstra", "Shortest Path",
            DataStore.AIRPORTS.length + " nodes",
            res.execTimeMs + " ms",
            res.dist[dst] == Integer.MAX_VALUE ? "∞" : "$" + res.dist[dst] + "k",
            "N/A", "N/A");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TAB 3 — MST
    // ─────────────────────────────────────────────────────────────────────────
    private JTextArea mstResult;
    private DefaultListModel<String> mstEdgeModel = new DefaultListModel<>();
    private DefaultListModel<String> mstCmpModel  = new DefaultListModel<>();
    private Algorithms.MSTResult primResult, kruskalResult;

    private JPanel buildMSTTab() {
        JPanel tab = darkPanel(new BorderLayout(12, 0));
        tab.setBorder(pad(12));

        JPanel canvasWrap = darkCard();
        canvasWrap.setLayout(new BorderLayout());
        canvasWrap.add(sectionTitle("🌐  MST — PRIM'S vs KRUSKAL'S"), BorderLayout.NORTH);
        canvasWrap.add(mstCanvas, BorderLayout.CENTER);
        canvasWrap.add(legend(), BorderLayout.SOUTH);

        JPanel side = buildSidePanel();
        side.add(sectionTitle("⚙  CONTROLS"));
        side.add(Box.createVerticalStrut(8));

        JButton btnPrim = accentButton("🌱  Run Prim's Algorithm", ACCENT3);
        btnPrim.addActionListener(e -> runPrims());
        side.add(btnPrim);
        side.add(Box.createVerticalStrut(6));

        JButton btnKrus = accentButton("⛓  Run Kruskal's Algorithm", ACCENT2);
        btnKrus.addActionListener(e -> runKruskals());
        side.add(btnKrus);

        side.add(Box.createVerticalStrut(12));
        side.add(sectionTitle("📊  RESULT"));
        side.add(Box.createVerticalStrut(6));
        mstResult = resultArea();
        side.add(scrollPane(mstResult));

        side.add(Box.createVerticalStrut(10));
        side.add(sectionTitle("🔗  MST EDGES"));
        side.add(Box.createVerticalStrut(6));
        JList<String> edgeList = new JList<>(mstEdgeModel);
        styleList(edgeList);
        side.add(scrollPane(edgeList));

        side.add(Box.createVerticalStrut(10));
        side.add(sectionTitle("📈  COST COMPARISON"));
        side.add(Box.createVerticalStrut(6));
        JList<String> cmpList = new JList<>(mstCmpModel);
        styleList(cmpList);
        side.add(scrollPane(cmpList));

        tab.add(canvasWrap, BorderLayout.CENTER);
        tab.add(wrappedSide(side), BorderLayout.EAST);
        return tab;
    }

    private void runPrims() {
        primResult = Algorithms.prims();
        showMSTResult(primResult);
        cmpData.put("prims", "Total cost: $" + primResult.totalCost + "k\nEdges: "
            + primResult.edges.size() + "\nExec: " + primResult.execTimeMs + " ms");
        updateReportTable("Prim's MST", "MST",
            DataStore.AIRPORTS.length + " nodes",
            primResult.execTimeMs + " ms",
            "$" + primResult.totalCost + "k", "N/A", "N/A");
        updateMSTCompare();
    }

    private void runKruskals() {
        kruskalResult = Algorithms.kruskals();
        showMSTResult(kruskalResult);
        cmpData.put("kruskals", "Total cost: $" + kruskalResult.totalCost + "k\nEdges: "
            + kruskalResult.edges.size() + "\nExec: " + kruskalResult.execTimeMs + " ms");
        updateReportTable("Kruskal's MST", "MST",
            DataStore.ROUTES.length + " edges sorted",
            kruskalResult.execTimeMs + " ms",
            "$" + kruskalResult.totalCost + "k", "N/A", "N/A");
        updateMSTCompare();
    }

    private void showMSTResult(Algorithms.MSTResult res) {
        mstCanvas.showMST(res.edges);

        StringBuilder sb = new StringBuilder();
        sb.append("ALGORITHM : ").append(res.algorithm).append("\n");
        sb.append("TOTAL COST: $").append(res.totalCost).append("k\n");
        sb.append("EDGES     : ").append(res.edges.size()).append("\n");
        sb.append("EXEC TIME : ").append(res.execTimeMs).append(" ms\n\n");
        sb.append("Both algorithms must produce\nthe same total MST cost.");
        mstResult.setText(sb.toString());

        mstEdgeModel.clear();
        for (int[] e : res.edges) {
            mstEdgeModel.addElement(DataStore.AIRPORTS[e[0]].code
                + " ↔ " + DataStore.AIRPORTS[e[1]].code
                + "   $" + e[2] + "k");
        }
    }

    private void updateMSTCompare() {
        mstCmpModel.clear();
        if (primResult != null)
            mstCmpModel.addElement("Prim's    total: $" + primResult.totalCost + "k  (" + primResult.execTimeMs + " ms)");
        if (kruskalResult != null)
            mstCmpModel.addElement("Kruskal's total: $" + kruskalResult.totalCost + "k  (" + kruskalResult.execTimeMs + " ms)");
        if (primResult != null && kruskalResult != null)
            mstCmpModel.addElement(primResult.totalCost == kruskalResult.totalCost
                ? "✔  Both produce the SAME MST cost"
                : "✘  Costs differ — check implementation");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TAB 4 — CARGO
    // ─────────────────────────────────────────────────────────────────────────
    private JTextArea cargoResult;
    private JSlider   weightSlider, volumeSlider;
    private JLabel    weightLabel, volumeLabel;
    private JTable    dpTable;

    private JPanel buildCargoTab() {
        JPanel tab = darkPanel(new BorderLayout(12, 0));
        tab.setBorder(pad(12));

        // Left: cargo canvas + DP table
        JPanel left = darkCard();
        left.setLayout(new BorderLayout(0, 10));
        left.add(sectionTitle("📦  AIRCRAFT HOLD LAYOUT"), BorderLayout.NORTH);
        left.add(cargoCanvas, BorderLayout.CENTER);

        JPanel dpWrap = darkPanel(new BorderLayout());
        dpWrap.add(sectionTitle("⚖  KNAPSACK DP TABLE  (items × weight capacity)"), BorderLayout.NORTH);
        dpTable = new JTable(new DefaultTableModel());
        dpTable.setBackground(new Color(8, 16, 32));
        dpTable.setForeground(TEXT);
        dpTable.setFont(new Font("Monospaced", Font.PLAIN, 10));
        dpTable.setGridColor(BORDER);
        dpTable.setRowHeight(18);
        dpTable.getTableHeader().setBackground(new Color(10, 22, 45));
        dpTable.getTableHeader().setForeground(ACCENT);
        dpWrap.add(new JScrollPane(dpTable), BorderLayout.CENTER);
        left.add(dpWrap, BorderLayout.SOUTH);

        // Right: controls
        JPanel side = buildSidePanel();
        side.add(sectionTitle("⚙  CONTROLS"));
        side.add(Box.createVerticalStrut(8));

        side.add(mono("Weight limit (tonnes):"));
        weightSlider = new JSlider(5, 60, 30);
        styleSlider(weightSlider);
        weightLabel = labelMono("30 t");
        weightSlider.addChangeListener(e -> weightLabel.setText(weightSlider.getValue() + " t"));
        side.add(weightSlider);
        side.add(weightLabel);

        side.add(Box.createVerticalStrut(8));
        side.add(mono("Volume limit (m³):"));
        volumeSlider = new JSlider(10, 100, 50);
        styleSlider(volumeSlider);
        volumeLabel = labelMono("50 m³");
        volumeSlider.addChangeListener(e -> volumeLabel.setText(volumeSlider.getValue() + " m³"));
        side.add(volumeSlider);
        side.add(volumeLabel);

        side.add(Box.createVerticalStrut(12));
        JButton btnDP     = accentButton("📦  Run DP Knapsack (Optimal)", ACCENT);
        JButton btnGreedy = accentButton("⚡  Run Greedy Knapsack (Fast)", ACCENT2);
        btnDP    .addActionListener(e -> runKnapsackDP());
        btnGreedy.addActionListener(e -> runGreedy());
        side.add(btnDP);
        side.add(Box.createVerticalStrut(6));
        side.add(btnGreedy);

        side.add(Box.createVerticalStrut(12));
        side.add(sectionTitle("📊  RESULT"));
        cargoResult = resultArea();
        side.add(scrollPane(cargoResult));

        side.add(Box.createVerticalStrut(10));
        side.add(sectionTitle("📋  ALL CARGO ITEMS"));
        side.add(scrollPane(buildCargoList()));

        tab.add(left, BorderLayout.CENTER);
        tab.add(wrappedSide(side), BorderLayout.EAST);
        return tab;
    }

    private void runKnapsackDP() {
        int W = weightSlider.getValue(), V = volumeSlider.getValue();
        Algorithms.KnapsackResult kr = Algorithms.knapsackDP(W, V);
        Algorithms.BinPackResult  bp = Algorithms.binPack(kr.selectedItems);
        cargoCanvas.setResults(kr, bp);
        displayCargoResult(kr, bp, "DP Knapsack (Optimal)");
        fillDPTable(kr.dpTable, W);
        cmpData.put("knapsack", "Revenue: $" + kr.totalRevenue + "k\nWeight: " + kr.totalWeight + "t\nExec: " + kr.execTimeMs + " ms");
        updateReportTable("Knapsack DP", "Cargo Optimization",
            DataStore.CARGO.length + " items",
            kr.execTimeMs + " ms",
            "$" + kr.totalRevenue + "k", "N/A", "N/A");
    }

    private void runGreedy() {
        int W = weightSlider.getValue(), V = volumeSlider.getValue();
        Algorithms.KnapsackResult gr = Algorithms.greedyKnapsack(W, V);
        Algorithms.BinPackResult  bp = Algorithms.binPack(gr.selectedItems);
        cargoCanvas.setResults(gr, bp);
        displayCargoResult(gr, bp, "Greedy Knapsack (Fast)");

        // Also compare against DP
        Algorithms.KnapsackResult dp = Algorithms.knapsackDP(W, V);
        double ratio = dp.totalRevenue > 0 ? 100.0 * gr.totalRevenue / dp.totalRevenue : 100;
        cmpData.put("greedy", "Revenue: $" + gr.totalRevenue + "k\nApprox ratio: " + String.format("%.1f", ratio) + "%");
        updateReportTable("Greedy Knapsack", "Cargo Optimization",
            DataStore.CARGO.length + " items",
            gr.execTimeMs + " ms",
            "$" + dp.totalRevenue + "k",
            "$" + gr.totalRevenue + "k",
            String.format("%.1f%%", ratio));
        updateReportTable("Greedy Bin Pack", "Hold Packing",
            gr.selectedItems.size() + " items",
            bp.execTimeMs + " ms",
            bp.zonesUsed + " zones", "N/A", "N/A");
    }

    private void displayCargoResult(Algorithms.KnapsackResult kr,
                                    Algorithms.BinPackResult  bp,
                                    String algo) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALGORITHM : ").append(algo).append("\n");
        sb.append("REVENUE   : $").append(kr.totalRevenue).append("k\n");
        sb.append("WEIGHT    : ").append(kr.totalWeight).append(" t\n");
        sb.append("VOLUME    : ").append(kr.totalVolume).append(" m³\n");
        sb.append("ITEMS     : ").append(kr.selectedItems.size()).append("\n");
        sb.append("ZONES USED: ").append(bp.zonesUsed).append("\n");
        sb.append("EXEC TIME : ").append(kr.execTimeMs + bp.execTimeMs).append(" ms\n\n");
        sb.append("SELECTED:\n");
        for (int idx : kr.selectedItems) {
            CargoItem ci = DataStore.CARGO[idx];
            sb.append("  • ").append(ci.name)
              .append(" (").append(ci.weight).append("t, $").append(ci.revenue).append("k)\n");
        }
        cargoResult.setText(sb.toString());
    }

    private void fillDPTable(int[][] dp, int W) {
        int rows = dp.length, cols = Math.min(W + 1, 21); // show up to capacity 20
        String[] colNames = new String[cols];
        colNames[0] = "Item \\ W";
        for (int c = 1; c < cols; c++) colNames[c] = String.valueOf(c - 1);

        Object[][] data = new Object[rows][cols];
        for (int r = 0; r < rows; r++) {
            data[r][0] = r == 0 ? "—" : DataStore.CARGO[r - 1].name.substring(0, Math.min(8, DataStore.CARGO[r-1].name.length()));
            for (int c = 1; c < cols; c++) data[r][c] = dp[r][c - 1];
        }
        dpTable.setModel(new DefaultTableModel(data, colNames));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TAB 5 — COMPARISON
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildCompareTab() {
        JPanel tab = darkPanel(new BorderLayout(0, 16));
        tab.setBorder(pad(16));

        tab.add(sectionTitle("📊  FULL ALGORITHM COMPARISON REPORT"), BorderLayout.NORTH);

        // Report table
        String[] cols = {"Algorithm","Category","Input Size","Exec Time","Optimal Value","Greedy Value","Approx Ratio"};
        reportModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(reportModel);
        table.setBackground(new Color(8, 16, 32));
        table.setForeground(TEXT);
        table.setFont(FONT_MONO);
        table.setGridColor(BORDER);
        table.setRowHeight(24);
        table.setSelectionBackground(new Color(0, 60, 100));
        table.getTableHeader().setBackground(new Color(10, 22, 45));
        table.getTableHeader().setForeground(ACCENT);
        table.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 11));

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(new Color(8, 16, 32));
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        tab.add(sp, BorderLayout.CENTER);

        JLabel hint = new JLabel("  ℹ  Run algorithms in the other tabs first — results appear here automatically.");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 12));
        hint.setForeground(DIMTEXT);
        tab.add(hint, BorderLayout.SOUTH);

        return tab;
    }

    private void updateReportTable(String algo, String cat, String input,
                                   String time, String opt, String greedy, String ratio) {
        if (reportModel == null) return;
        // Remove old row for same algo
        for (int i = reportModel.getRowCount() - 1; i >= 0; i--)
            if (reportModel.getValueAt(i, 0).equals(algo)) reportModel.removeRow(i);
        reportModel.addRow(new Object[]{algo, cat, input, time, opt, greedy, ratio});
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPER BUILDERS
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildAirportList() {
        JPanel p = darkPanel(new GridLayout(0, 1, 0, 3));
        for (Airport a : DataStore.AIRPORTS) {
            JLabel lbl = new JLabel("  " + a.code + "  —  " + a.name + (a.isHub ? "  [HUB]" : ""));
            lbl.setFont(FONT_MONO);
            lbl.setForeground(a.isHub ? new Color(255, 220, 50) : TEXT);
            lbl.setOpaque(true);
            lbl.setBackground(BG2);
            lbl.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            p.add(lbl);
        }
        return p;
    }

    private JPanel buildRouteList() {
        JPanel p = darkPanel(new GridLayout(0, 1, 0, 2));
        for (Route r : DataStore.ROUTES) {
            JLabel lbl = new JLabel("  " + DataStore.AIRPORTS[r.from].code
                + " ↔ " + DataStore.AIRPORTS[r.to].code
                + "   $" + r.cost + "k   " + r.time + "h");
            lbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
            lbl.setForeground(DIMTEXT);
            lbl.setOpaque(true);
            lbl.setBackground(BG2);
            lbl.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
            p.add(lbl);
        }
        return p;
    }

    private JPanel buildCargoList() {
        JPanel p = darkPanel(new GridLayout(0, 1, 0, 2));
        for (CargoItem ci : DataStore.CARGO) {
            JLabel lbl = new JLabel("  " + ci.name
                + "   " + ci.weight + "t   " + ci.volume + "m³   $" + ci.revenue + "k");
            lbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
            lbl.setForeground(DIMTEXT);
            lbl.setOpaque(true);
            lbl.setBackground(BG2);
            lbl.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
            p.add(lbl);
        }
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UI HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel darkPanel(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(BG);
        return p;
    }

    private JPanel darkCard() {
        JPanel p = new JPanel();
        p.setBackground(BG2);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        return p;
    }

    private JPanel buildSidePanel() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(BG2);
        side.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        return side;
    }

    private JScrollPane wrappedSide(JPanel side) {
        JScrollPane sp = new JScrollPane(side);
        sp.setPreferredSize(new Dimension(295, 0));
        sp.setBackground(BG2);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(BG2);
        return sp;
    }

    private JLabel sectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 10));
        lbl.setForeground(ACCENT);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        return lbl;
    }

    private JScrollPane scrollPane(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBackground(BG2);
        sp.getViewport().setBackground(BG2);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        sp.setAlignmentX(LEFT_ALIGNMENT);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        return sp;
    }

    private JPanel legend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        p.setBackground(BG2);
        p.add(legendDot(ACCENT3,  "MST Edge"));
        p.add(legendDot(DIMTEXT,  "Other Edge"));
        p.add(legendDot(ACCENT2,  "Shortest Path"));
        p.add(legendDot(ACCENT,   "Airport"));
        p.add(legendDot(new Color(255,220,50), "Hub"));
        return p;
    }

    private JLabel legendDot(Color col, String text) {
        JLabel l = new JLabel("⬤  " + text);
        l.setForeground(col);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        return l;
    }

    private JComboBox<String> styledCombo() {
        JComboBox<String> cb = new JComboBox<>();
        cb.setBackground(BG2);
        cb.setForeground(TEXT);
        cb.setFont(FONT_MONO);
        cb.setAlignmentX(LEFT_ALIGNMENT);
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        return cb;
    }
private JButton accentButton(String text, Color col) {
    JButton btn = new JButton(text);
    btn.setBackground(col.darker().darker());
    // Smart text color: use dark text on bright buttons, bright on dark
    Color textColor = isBrightColor(col) ? new Color(10, 20, 40) : col.brighter();
    btn.setForeground(textColor);
    btn.setFont(new Font("Monospaced", Font.BOLD, 12));
    btn.setFocusPainted(false);
    btn.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(col.darker(), 1),
        BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    btn.setAlignmentX(LEFT_ALIGNMENT);
    btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    btn.addMouseListener(new MouseAdapter() {
        public void mouseEntered(MouseEvent e) { btn.setBackground(col.darker()); }
        public void mouseExited(MouseEvent e)  { btn.setBackground(col.darker().darker()); }
    });
    return btn;
}

// Helper method to detect if a color is bright
private boolean isBrightColor(Color c) {
    int brightness = (int) Math.sqrt(
        c.getRed() * c.getRed() * 0.299 +
        c.getGreen() * c.getGreen() * 0.587 +
        c.getBlue() * c.getBlue() * 0.114
    );
    return brightness > 140;
}

    private JTextArea resultArea() {
        JTextArea ta = new JTextArea(6, 20);
        ta.setEditable(false);
        ta.setBackground(new Color(8, 16, 32));
        ta.setForeground(new Color(0, 255, 157));
        ta.setFont(FONT_RESULT);
        ta.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        return ta;
    }

    private void styleList(JList<String> list) {
        list.setBackground(new Color(8, 16, 32));
        list.setForeground(TEXT);
        list.setFont(new Font("Monospaced", Font.PLAIN, 11));
        list.setSelectionBackground(new Color(0, 60, 100));
        list.setFixedCellHeight(20);
    }

    private void styleSlider(JSlider s) {
        s.setBackground(BG2);
        s.setForeground(ACCENT);
        s.setAlignmentX(LEFT_ALIGNMENT);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    }

    private JLabel mono(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Monospaced", Font.PLAIN, 11));
        l.setForeground(DIMTEXT);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JLabel labelMono(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Monospaced", Font.BOLD, 13));
        l.setForeground(ACCENT);
        l.setAlignmentX(RIGHT_ALIGNMENT);
        return l;
    }

    private Border pad(int p) {
        return BorderFactory.createEmptyBorder(p, p, p, p);
    }
}
