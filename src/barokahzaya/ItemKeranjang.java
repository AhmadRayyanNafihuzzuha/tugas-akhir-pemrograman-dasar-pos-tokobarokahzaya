package barokahzaya;

public class ItemKeranjang {
    private String kodeBarang;
    private String namaBarang;
    private double hargaBeli;
    private double hargaJual;
    private int jumlahBeli;
    private double subtotal;

    public ItemKeranjang(String kodeBarang, String namaBarang, double hargaBeli, double hargaJual, int jumlahBeli, double subtotal) {
        this.kodeBarang = kodeBarang;
        this.namaBarang = namaBarang;
        this.hargaBeli = hargaBeli;
        this.hargaJual = hargaJual;
        this.jumlahBeli = jumlahBeli;
        this.subtotal = subtotal;
    }


    public String getKodeBarang() { return kodeBarang; }
    public String getNamaBarang() { return namaBarang; }
    public double getHargaBeli() { return hargaBeli; }
    public double getHargaJual() { return hargaJual; }
    public int getJumlahBeli() { return jumlahBeli; }
    public double getSubtotal() { return subtotal; }
}