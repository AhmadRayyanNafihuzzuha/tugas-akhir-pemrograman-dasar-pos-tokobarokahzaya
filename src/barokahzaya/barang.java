package barokahzaya;

public class Barang {
    private String kodeBarang;
    private String namaBarang;
    private double hargaBeli;
    private double hargaJual;
    private int stok;

    public Barang(String kodeBarang, String namaBarang, double hargaBeli, double hargaJual, int stok) {
        this.kodeBarang = kodeBarang;
        this.namaBarang = namaBarang;
        this.hargaBeli = hargaBeli;
        this.hargaJual = hargaJual;
        this.stok = stok;
    }

    public String getKodeBarang() { return kodeBarang; }
    public String getNamaBarang() { return namaBarang; }
    public double getHargaBeli() { return hargaBeli; }
    public double getHargaJual() { return hargaJual; }
    public int getStok() { return stok; }
    public void setStok(int stok) { this.stok = stok; }
}