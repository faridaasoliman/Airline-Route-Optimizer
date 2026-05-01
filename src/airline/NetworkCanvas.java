package airline;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * NetworkCanvas — draws the airport graph, MST edges, and shortest paths.
 */
public class NetworkCanvas extends JPanel {

    // ── Drawing modes ────────────────────────────────────────────────────────
    public enum Mode { NORMAL, DIJKSTRA, MST }

    private Mode   mode          = Mode.NORMAL;
    private List<int[]> mstEdges = new ArrayList<>();   // {from,to,cost}
    private List<Integer> shortPath = new ArrayList<>(); // Dijkstra path nodes

    // Colours
    private static final Color BG          = new Color(5,  13,  26);
    private static final Color EDGE_DIM    = new Color(30,  60, 100);
    private static final Color EDGE_MST    = new Color(0,  255, 157);
    private static final Color EDGE_PATH   = new Color(255, 107,  53);
    private static final Color NODE_NORMAL = new Color(0,  200, 255);
    private static final Color NODE_HUB    = new Color(255, 220,  50);
    private static final Color NODE_SOURCE = new Color(255, 220,  50);
    private static final Color NODE_DEST   = new Color(255,  77, 109);
    private static final Color TEXT_COLOR  = new Color(200, 220, 240);
    private static final Color COST_COLOR  = new Color(100, 180, 255);

    private int dijkSource = -1, dijkDest = -1;

    public NetworkCanvas() {
        setPreferredSize(new Dimension(720, 440));
        setBackground(BG);
    }

    // ── Public setters called by the tab panels ───────────────────────────────

    public void showNormal() {
        mode = Mode.NORMAL;
        mstEdges.clear();
        shortPath.clear();
        dijkSource = -1; dijkDest = -1;
        repaint();
    }

    public void showMST(List<int[]> edges) {
        mode = Mode.MST;
        mstEdges = edges;
        shortPath.clear();
        repaint();
    }

    public void showDijkstra(List<Integer> path, int src, int dst) {
        mode     = Mode.DIJKSTRA;
        shortPath = path;
        dijkSource = src; dijkDest = dst;
        mstEdges.clear();
        repaint();
    }

    // ── Painting ──────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // scale to fit panel
        double sx = (double) getWidth()  / 720;
        double sy = (double) getHeight() / 440;
        g2.scale(sx, sy);

        drawEdges(g2);
        drawNodes(g2);
    }

    private void drawEdges(Graphics2D g2) {
        Airport[] airports = DataStore.AIRPORTS;

        // Build set of MST / path edges for quick lookup
        Set<String> mstSet  = new HashSet<>();
        Set<String> pathSet = new HashSet<>();

        for (int[] e : mstEdges) mstSet.add(Math.min(e[0],e[1]) + "-" + Math.max(e[0],e[1]));

        if (shortPath.size() > 1) {
            for (int i = 0; i < shortPath.size() - 1; i++) {
                int a = shortPath.get(i), b = shortPath.get(i + 1);
                pathSet.add(Math.min(a,b) + "-" + Math.max(a,b));
            }
        }

        for (Route r : DataStore.ROUTES) {
            Airport a = airports[r.from], b = airports[r.to];
            String key = Math.min(r.from,r.to) + "-" + Math.max(r.from,r.to);

            boolean isMST  = mstSet.contains(key);
            boolean isPath = pathSet.contains(key);

            Color edgeColor;
            float strokeW;

            if      (isPath) { edgeColor = EDGE_PATH; strokeW = 3.5f; }
            else if (isMST)  { edgeColor = EDGE_MST;  strokeW = 2.5f; }
            else             { edgeColor = EDGE_DIM;   strokeW = 1.2f; }

            g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(edgeColor);
            g2.drawLine(a.x, a.y, b.x, b.y);

            // Cost label at midpoint
            if (isMST || isPath) {
                int mx = (a.x + b.x) / 2, my = (a.y + b.y) / 2;
                g2.setFont(new Font("Monospaced", Font.BOLD, 11));
                g2.setColor(isPath ? EDGE_PATH.brighter() : EDGE_MST.darker());
                g2.drawString("$" + r.cost + "k", mx + 3, my - 3);
            }
        }
    }

    private void drawNodes(Graphics2D g2) {
        Airport[] airports = DataStore.AIRPORTS;

        for (Airport ap : airports) {
            boolean isSource = (ap.id == dijkSource);
            boolean isDest   = (ap.id == dijkDest);
            boolean inPath   = shortPath.contains(ap.id);
            boolean inMST    = mstEdges.stream().anyMatch(e -> e[0] == ap.id || e[1] == ap.id);

            // Glow ring
            Color glowColor;
            if      (isSource) glowColor = NODE_SOURCE;
            else if (isDest)   glowColor = NODE_DEST;
            else if (inPath)   glowColor = EDGE_PATH;
            else if (inMST)    glowColor = EDGE_MST;
            else if (ap.isHub) glowColor = NODE_HUB;
            else               glowColor = NODE_NORMAL;

            // Outer glow
            g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 40));
            g2.fillOval(ap.x - 18, ap.y - 18, 36, 36);

            // Node circle
            int r = ap.isHub ? 14 : 10;
            g2.setColor(new Color(5, 13, 26));
            g2.fillOval(ap.x - r, ap.y - r, r * 2, r * 2);
            g2.setColor(glowColor);
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawOval(ap.x - r, ap.y - r, r * 2, r * 2);

            // Airport code
            g2.setFont(new Font("Monospaced", Font.BOLD, ap.isHub ? 11 : 10));
            g2.setColor(TEXT_COLOR);
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(ap.code);
            g2.drawString(ap.code, ap.x - tw / 2, ap.y + fm.getAscent() / 2 - 1);

            // City name below
            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            g2.setColor(new Color(150, 180, 210));
            int nw = g2.getFontMetrics().stringWidth(ap.name);
            g2.drawString(ap.name, ap.x - nw / 2, ap.y + r + 11);
        }
    }
}
