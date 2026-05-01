package airline;

import java.util.*;

/**
 * Algorithms.java
 * ────────────────────────────────────────────────────────────────────────────
 * Contains all four required algorithm families:
 *   1. Dijkstra's  – shortest flight path
 *   2. Prim's      – minimum spanning tree
 *   3. Kruskal's   – minimum spanning tree (alternative)
 *   4. Knapsack DP – optimal cargo selection
 *   5. Greedy      – fast cargo selection + bin packing
 * ────────────────────────────────────────────────────────────────────────────
 */
public class Algorithms {

    // ══════════════════════════════════════════════════════════════════════════
    //  1. DIJKSTRA'S ALGORITHM
    //     Finds the shortest (cheapest) path from one airport to all others.
    //     Time complexity: O((V + E) log V)
    // ══════════════════════════════════════════════════════════════════════════

    public static class DijkstraResult {
        public int[]   dist;        // dist[i] = cheapest cost to airport i
        public int[]   prev;        // prev[i] = previous airport on the path
        public long    execTimeMs;  // how long it took in milliseconds

        public DijkstraResult(int n) {
            dist = new int[n];
            prev = new int[n];
            Arrays.fill(dist, Integer.MAX_VALUE);
            Arrays.fill(prev, -1);
        }

        /** Reconstructs the path from source to `target` as a list of airport IDs. */
        public List<Integer> getPath(int target) {
            List<Integer> path = new ArrayList<>();
            for (int v = target; v != -1; v = prev[v]) path.add(0, v);
            return path;
        }
    }

    public static DijkstraResult dijkstra(int source) {
        long start = System.currentTimeMillis();

        int n = DataStore.AIRPORTS.length;
        List<int[]>[] adj = DataStore.buildAdjacencyList();
        DijkstraResult result = new DijkstraResult(n);
        result.dist[source] = 0;

        // Priority queue: int[]{cost, nodeId}
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
        pq.offer(new int[]{0, source});

        boolean[] visited = new boolean[n];

        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int cost = curr[0], u = curr[1];

            if (visited[u]) continue;   // already processed
            visited[u] = true;

            for (int[] edge : adj[u]) {
                int v = edge[0], w = edge[1];
                if (!visited[v] && result.dist[u] + w < result.dist[v]) {
                    result.dist[v] = result.dist[u] + w;
                    result.prev[v] = u;
                    pq.offer(new int[]{result.dist[v], v});
                }
            }
        }

        result.execTimeMs = System.currentTimeMillis() - start;
        return result;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  2. PRIM'S ALGORITHM
    //     Builds the minimum spanning tree starting from node 0.
    //     Grows the MST one edge at a time (always pick the cheapest edge
    //     connecting a visited node to an unvisited node).
    //     Time complexity: O(E log V)
    // ══════════════════════════════════════════════════════════════════════════

    public static class MSTResult {
        public List<int[]> edges;       // each int[]{from, to, cost}
        public int         totalCost;
        public long        execTimeMs;
        public String      algorithm;

        public MSTResult(String algo) {
            edges = new ArrayList<>();
            algorithm = algo;
        }
    }

    public static MSTResult prims() {
        long start = System.currentTimeMillis();

        int n = DataStore.AIRPORTS.length;
        List<int[]>[] adj = DataStore.buildAdjacencyList();
        MSTResult result = new MSTResult("Prim's");

        int[]     minCost = new int[n];    // cheapest edge to reach each node
        int[]     parent  = new int[n];    // where each node came from
        boolean[] inMST   = new boolean[n];

        Arrays.fill(minCost, Integer.MAX_VALUE);
        Arrays.fill(parent, -1);
        minCost[0] = 0;

        // Priority queue: int[]{cost, nodeId}
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
        pq.offer(new int[]{0, 0});

        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int u = curr[1];
            if (inMST[u]) continue;
            inMST[u] = true;

            if (parent[u] != -1) {
                result.edges.add(new int[]{parent[u], u, minCost[u]});
                result.totalCost += minCost[u];
            }

            for (int[] edge : adj[u]) {
                int v = edge[0], w = edge[1];
                if (!inMST[v] && w < minCost[v]) {
                    minCost[v] = w;
                    parent[v]  = u;
                    pq.offer(new int[]{w, v});
                }
            }
        }

        result.execTimeMs = System.currentTimeMillis() - start;
        return result;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  3. KRUSKAL'S ALGORITHM
    //     Also builds the MST, but by sorting ALL edges first and using a
    //     Union-Find structure to detect cycles.
    //     Time complexity: O(E log E)
    // ══════════════════════════════════════════════════════════════════════════

    // --- Union-Find (Disjoint Set) helper class ---
    static class UnionFind {
        int[] parent, rank;

        UnionFind(int n) {
            parent = new int[n];
            rank   = new int[n];
            for (int i = 0; i < n; i++) parent[i] = i;
        }

        int find(int x) {
            if (parent[x] != x) parent[x] = find(parent[x]); // path compression
            return parent[x];
        }

        boolean union(int a, int b) {
            int ra = find(a), rb = find(b);
            if (ra == rb) return false;   // same component → would create a cycle
            if (rank[ra] < rank[rb]) { int t = ra; ra = rb; rb = t; }
            parent[rb] = ra;
            if (rank[ra] == rank[rb]) rank[ra]++;
            return true;
        }
    }

    public static MSTResult kruskals() {
        long start = System.currentTimeMillis();

        int n = DataStore.AIRPORTS.length;
        MSTResult result = new MSTResult("Kruskal's");

        // Sort all routes by cost ascending
        Route[] sorted = DataStore.ROUTES.clone();
        Arrays.sort(sorted, Comparator.comparingInt(r -> r.cost));

        UnionFind uf = new UnionFind(n);

        for (Route r : sorted) {
            if (uf.union(r.from, r.to)) {          // safe to add (no cycle)
                result.edges.add(new int[]{r.from, r.to, r.cost});
                result.totalCost += r.cost;
                if (result.edges.size() == n - 1) break;   // MST is complete
            }
        }

        result.execTimeMs = System.currentTimeMillis() - start;
        return result;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  4. KNAPSACK DP  (0/1 Knapsack — exact optimal cargo selection)
    //     Selects cargo shipments that maximise revenue without exceeding
    //     the aircraft's weight limit.
    //     Time complexity: O(n × W)
    // ══════════════════════════════════════════════════════════════════════════

    public static class KnapsackResult {
        public int[][]       dpTable;        // the full DP table (for display)
        public List<Integer> selectedItems;  // indices of chosen cargo items
        public int           totalRevenue;
        public int           totalWeight;
        public int           totalVolume;
        public long          execTimeMs;
        public boolean       isOptimal = true;

        public KnapsackResult() { selectedItems = new ArrayList<>(); }
    }

    public static KnapsackResult knapsackDP(int weightLimit, int volumeLimit) {
        long start = System.currentTimeMillis();

        CargoItem[] items = DataStore.CARGO;
        int n = items.length;
        KnapsackResult result = new KnapsackResult();

        // ── Build 2D DP table: dp[i][w] = max revenue using first i items
        //    with weight limit w  (we ignore volume here for simplicity of
        //    display; volume is checked as a hard constraint at the end)
        int W = weightLimit;
        int[][] dp = new int[n + 1][W + 1];

        for (int i = 1; i <= n; i++) {
            CargoItem item = items[i - 1];
            for (int w = 0; w <= W; w++) {
                dp[i][w] = dp[i - 1][w];                          // skip item
                if (item.weight <= w) {
                    dp[i][w] = Math.max(dp[i][w],
                                        dp[i - 1][w - item.weight] + item.revenue);
                }
            }
        }

        result.dpTable = dp;

        // ── Backtrack to find which items were selected ──
        int w = W;
        for (int i = n; i >= 1; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                CargoItem item = items[i - 1];
                // also check volume limit
                if (result.totalVolume + item.volume <= volumeLimit) {
                    result.selectedItems.add(i - 1);
                    result.totalRevenue += item.revenue;
                    result.totalWeight  += item.weight;
                    result.totalVolume  += item.volume;
                    w -= item.weight;
                }
            }
        }

        result.execTimeMs = System.currentTimeMillis() - start;
        return result;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  5a. GREEDY KNAPSACK  (sort by revenue/weight ratio — fast approximation)
    // ══════════════════════════════════════════════════════════════════════════

    public static KnapsackResult greedyKnapsack(int weightLimit, int volumeLimit) {
        long start = System.currentTimeMillis();

        CargoItem[] items = DataStore.CARGO;
        int n = items.length;
        KnapsackResult result = new KnapsackResult();
        result.isOptimal = false;

        // Sort by revenue-to-weight ratio descending
        Integer[] indices = new Integer[n];
        for (int i = 0; i < n; i++) indices[i] = i;
        Arrays.sort(indices, (a, b) ->
            Double.compare((double) items[b].revenue / items[b].weight,
                           (double) items[a].revenue / items[a].weight));

        int remWeight = weightLimit, remVolume = volumeLimit;
        for (int idx : indices) {
            CargoItem item = items[idx];
            if (item.weight <= remWeight && item.volume <= remVolume) {
                result.selectedItems.add(idx);
                result.totalRevenue += item.revenue;
                result.totalWeight  += item.weight;
                result.totalVolume  += item.volume;
                remWeight -= item.weight;
                remVolume -= item.volume;
            }
        }

        result.execTimeMs = System.currentTimeMillis() - start;
        return result;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  5b. GREEDY BIN PACKING — First Fit Decreasing (FFD)
    //      Packs selected cargo into aircraft hold zones.
    //      Each zone has a weight capacity. Items are sorted largest-first.
    // ══════════════════════════════════════════════════════════════════════════

    public static class BinPackResult {
        public List<List<Integer>> zones;   // zones.get(z) = list of item indices
        public int[]               zoneLoad;
        public int                 zonesUsed;
        public long                execTimeMs;

        public BinPackResult(int numZones) {
            zones    = new ArrayList<>();
            zoneLoad = new int[numZones];
            for (int i = 0; i < numZones; i++) zones.add(new ArrayList<>());
        }
    }

    /** Zone capacities in tonnes */
    private static final int[] ZONE_CAP = {15, 20, 15};

    public static BinPackResult binPack(List<Integer> itemIndices) {
        long start = System.currentTimeMillis();

        CargoItem[] items  = DataStore.CARGO;
        int         nZones = ZONE_CAP.length;
        BinPackResult res  = new BinPackResult(nZones);

        // Sort items by weight descending (FFD)
        List<Integer> sorted = new ArrayList<>(itemIndices);
        sorted.sort((a, b) -> items[b].weight - items[a].weight);

        for (int idx : sorted) {
            CargoItem item = items[idx];
            boolean placed = false;
            for (int z = 0; z < nZones; z++) {
                if (res.zoneLoad[z] + item.weight <= ZONE_CAP[z]) {
                    res.zones.get(z).add(idx);
                    res.zoneLoad[z] += item.weight;
                    placed = true;
                    break;
                }
            }
            if (!placed) res.zonesUsed++;   // overflow: count unplaced
        }

        // count actually used zones
        res.zonesUsed = 0;
        for (int z = 0; z < nZones; z++) if (!res.zones.get(z).isEmpty()) res.zonesUsed++;

        res.execTimeMs = System.currentTimeMillis() - start;
        return res;
    }
}
