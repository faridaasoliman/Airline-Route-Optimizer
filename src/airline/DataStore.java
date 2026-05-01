package airline;

import java.util.*;

/**
 * DataStore — central place for all airports, routes, and cargo items.
 * Think of this as the "database" of our application.
 */
public class DataStore {

    // ── AIRPORTS ─────────────────────────────────────────────────────────────
    public static final Airport[] AIRPORTS = {
        new Airport(0, "CAI", "Cairo",      360, 220, true),
        new Airport(1, "DXB", "Dubai",      560, 270, true),
        new Airport(2, "LHR", "London",     110, 100, true),
        new Airport(3, "CDG", "Paris",      160, 150, false),
        new Airport(4, "FRA", "Frankfurt",  240, 120, false),
        new Airport(5, "IST", "Istanbul",   320, 140, false),
        new Airport(6, "BOM", "Mumbai",     600, 330, false),
        new Airport(7, "SIN", "Singapore",  660, 380, true),
        new Airport(8, "JFK", "New York",    70, 200, false),
        new Airport(9, "NBO", "Nairobi",    430, 350, false),
    };

    // ── ROUTES (undirected edges) ─────────────────────────────────────────────
    public static final Route[] ROUTES = {
        new Route(0, 1, 12, 3.5),
        new Route(0, 5,  8, 2.5),
        new Route(0, 9, 10, 4.0),
        new Route(0, 3, 18, 5.0),
        new Route(1, 6,  9, 3.0),
        new Route(1, 7, 14, 7.0),
        new Route(1, 5,  7, 2.0),
        new Route(2, 3,  4, 1.5),
        new Route(2, 4,  5, 2.0),
        new Route(2, 8, 20, 8.0),
        new Route(3, 4,  3, 1.0),
        new Route(3, 5, 11, 3.5),
        new Route(4, 5,  9, 3.0),
        new Route(5, 0,  8, 2.5),
        new Route(6, 7,  6, 4.0),
        new Route(6, 9, 15, 5.5),
        new Route(7, 9, 22, 9.0),
        new Route(8, 3, 19, 7.5),
        new Route(9, 1, 13, 5.0),
        new Route(2, 5, 13, 4.5),
    };

    // ── CARGO SHIPMENTS ───────────────────────────────────────────────────────
    public static final CargoItem[] CARGO = {
        new CargoItem(0,  "Electronics Batch A",  4,  8, 28),
        new CargoItem(1,  "Pharma Supplies",       2,  3, 22),
        new CargoItem(2,  "Automotive Parts",      8, 12, 35),
        new CargoItem(3,  "Luxury Goods",          1,  2, 18),
        new CargoItem(4,  "Fresh Produce",         6, 15, 20),
        new CargoItem(5,  "Machinery Crate",      10, 20, 40),
        new CargoItem(6,  "Textiles Box",          3, 10, 12),
        new CargoItem(7,  "Medical Equipment",     2,  5, 30),
        new CargoItem(8,  "Chemical Drums",        7,  9, 25),
        new CargoItem(9,  "Electronics Batch B",   5,  7, 32),
        new CargoItem(10, "Frozen Seafood",        4,  6, 16),
        new CargoItem(11, "Art & Collectibles",    1,  3, 24),
    };

    // ── ADJACENCY LIST builder ────────────────────────────────────────────────
    /**
     * Builds an adjacency list from ROUTES.
     * adj[i] = list of int[]{neighbour, cost}
     */
    public static List<int[]>[] buildAdjacencyList() {
        int n = AIRPORTS.length;
        @SuppressWarnings("unchecked")
        List<int[]>[] adj = new ArrayList[n];
        for (int i = 0; i < n; i++) adj[i] = new ArrayList<>();

        for (Route r : ROUTES) {
            adj[r.from].add(new int[]{r.to,   r.cost});
            adj[r.to  ].add(new int[]{r.from, r.cost});   // undirected
        }
        return adj;
    }
}
