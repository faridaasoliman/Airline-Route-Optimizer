package airline;

public class Airport {
    public int id;
    public String code;
    public String name;
    public int x, y;       // position on the canvas
    public boolean isHub;

    public Airport(int id, String code, String name, int x, int y, boolean isHub) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.x = x;
        this.y = y;
        this.isHub = isHub;
    }

    @Override
    public String toString() {
        return code + " — " + name;
    }
}
