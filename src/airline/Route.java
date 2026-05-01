package airline;

public class Route {
    public int from, to;
    public int cost;       // operating cost in $1000s
    public double time;    // flight time in hours

    public Route(int from, int to, int cost, double time) {
        this.from = from;
        this.to   = to;
        this.cost = cost;
        this.time = time;
    }
}
