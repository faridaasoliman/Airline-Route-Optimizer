package airline;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * CargoCanvas — draws the aircraft hold zones with loaded cargo items.
 */
public class CargoCanvas extends JPanel {

    private Algorithms.BinPackResult binResult;
    private Algorithms.KnapsackResult knapsackResult;
    private boolean hasData = false;

    private static final Color BG       = new Color(5,  13,  26);
    private static final Color ZONE_BG  = new Color(14, 28, 50);
    private static final Color ZONE_BOR = new Color(30, 60, 100);
    private static final Color TEXT     = new Color(200, 220, 240);

    // Palette for cargo items
    private static final Color[] ITEM_COLORS = {
        new Color(0,  200, 255, 200), new Color(0,  255, 157, 200),
        new Color(255, 107,  53, 200), new Color(255, 220,  50, 200),
        new Color(180,  80, 255, 200), new Color(255,  80, 140, 200),
        new Color( 80, 200, 120, 200), new Color(255, 160,  40, 200),
        new Color( 60, 160, 255, 200), new Color(200, 255,  80, 200),
        new Color(255, 100, 100, 200), new Color(100, 220, 255, 200),
    };

    public CargoCanvas() {
        setPreferredSize(new Dimension(720, 260));
        setBackground(BG);
    }

    public void setResults(Algorithms.KnapsackResult kr, Algorithms.BinPackResult bp) {
        this.knapsackResult = kr;
        this.binResult      = bp;
        this.hasData        = true;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!hasData) {
            g2.setColor(new Color(70, 100, 140));
            g2.setFont(new Font("Monospaced", Font.BOLD, 15));
            g2.drawString("Run Knapsack DP or Greedy to see cargo loading", 160, getHeight() / 2);
            return;
        }

        int W = getWidth(), H = getHeight();
        String[] zoneNames = {"FORWARD HOLD", "CENTER HOLD", "AFT HOLD"};
        int[]    zoneCaps  = {15, 20, 15};
        int      padding   = 20;
        int      zoneW     = (W - padding * 4) / 3;
        int      zoneH     = H - 80;

        // Draw aircraft silhouette background
        g2.setColor(new Color(10, 20, 40));
        g2.fillRoundRect(10, 30, W - 20, H - 50, 40, 40);

        for (int z = 0; z < 3; z++) {
            int x = padding + z * (zoneW + padding);
            int y = 50;

            // Zone background
            g2.setColor(ZONE_BG);
            g2.fillRoundRect(x, y, zoneW, zoneH, 10, 10);
            g2.setColor(ZONE_BOR);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x, y, zoneW, zoneH, 10, 10);

            // Zone label
            g2.setFont(new Font("Monospaced", Font.BOLD, 10));
            g2.setColor(new Color(0, 200, 255));
            g2.drawString(zoneNames[z], x + 6, y + 14);

            // Cap label
            g2.setColor(new Color(100, 150, 200));
            g2.drawString("CAP: " + zoneCaps[z] + "t", x + zoneW - 60, y + 14);

            // Draw items from bottom up
            if (binResult != null) {
                List<Integer> items = binResult.zones.get(z);
                int load  = binResult.zoneLoad[z];
                int drawY = y + zoneH - 4;

                for (int idx : items) {
                    CargoItem item  = DataStore.CARGO[idx];
                    double   ratio  = (double) item.weight / zoneCaps[z];
                    int      itemH  = (int) (ratio * (zoneH - 24));
                    itemH = Math.max(itemH, 22);
                    drawY -= itemH + 2;

                    // Item rectangle
                    Color col = ITEM_COLORS[idx % ITEM_COLORS.length];
                    g2.setColor(col);
                    g2.fillRoundRect(x + 4, drawY, zoneW - 8, itemH, 6, 6);
                    g2.setColor(col.darker());
                    g2.drawRoundRect(x + 4, drawY, zoneW - 8, itemH, 6, 6);

                    // Label inside item
                    if (itemH > 18) {
                        g2.setFont(new Font("Monospaced", Font.BOLD, 9));
                        g2.setColor(Color.WHITE);
                        String label = item.weight + "t  $" + item.revenue + "k";
                        g2.drawString(label, x + 8, drawY + 13);
                        if (itemH > 28) {
                            g2.setFont(new Font("SansSerif", Font.PLAIN, 8));
                            String name = item.name.length() > 16
                                          ? item.name.substring(0, 15) + "…"
                                          : item.name;
                            g2.drawString(name, x + 8, drawY + 23);
                        }
                    }
                }

                // Utilisation bar label
                g2.setFont(new Font("Monospaced", Font.BOLD, 10));
                g2.setColor(load > zoneCaps[z] * 0.9 ? new Color(255,100,80) : new Color(0, 255, 157));
                g2.drawString(load + "/" + zoneCaps[z] + "t", x + 6, y + zoneH - 4);
            }
        }

        // Title
        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        g2.setColor(new Color(0, 200, 255));
        g2.drawString("✈  AIRCRAFT CARGO HOLD LAYOUT", padding, 24);
    }
}
