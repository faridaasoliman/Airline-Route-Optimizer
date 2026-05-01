package airline;

public class CargoItem {
    public int id;
    public String name;
    public int weight;     // tonnes
    public int volume;     // cubic metres
    public int revenue;    // $1000s

    public CargoItem(int id, String name, int weight, int volume, int revenue) {
        this.id      = id;
        this.name    = name;
        this.weight  = weight;
        this.volume  = volume;
        this.revenue = revenue;
    }
}
