package barokahzaya;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class FormKasirUtama extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FormKasirUtama.class.getName());

    /**
     * Creates new form FormKasirUtama
     */
    
    private ArrayList<Barang> daftarBarang = new ArrayList<>();
    private ArrayList<ItemKeranjang> keranjang = new ArrayList<>();
    private DefaultTableModel tableModel;
    private String kodeTerpilih = "";
    private String ringkasanTerakhir = "";
    
    public FormKasirUtama() {
        initComponents();
        setLocationRelativeTo(null);
        muatDataBarangAwal();
    }
    
    // 1. Memuat data sembako awal & tampilkan ke JTable
    private void muatDataBarangAwal() {
        if (!muatDataBarangDariFile()) {
        daftarBarang.add(new Barang("B01", "Beras Ramos 5kg", 65000, 72000, 20));
        daftarBarang.add(new Barang("M02", "Minyak Goreng 1L", 15500, 18000, 15));
        daftarBarang.add(new Barang("G03", "Gula Pasir 1kg", 15000, 17500, 12));
        daftarBarang.add(new Barang("T04", "Telur Ayam 1kg", 25000, 28000, 5));
        daftarBarang.add(new Barang("I05", "Mi Instan Goreng", 2700, 3500, 40));
        daftarBarang.add(new Barang("K06", "Kopi Kapal Api 165g", 13000, 15500, 8));
        simpanDataBarangKeFile();
    }
        String[] kolom = {"Kode", "Nama Barang", "Stok", "Harga Jual"};
        tableModel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Mencegah edit langsung di sel tabel
            }
        };
        tblBarang.setModel(tableModel);
        filterTabelBarang("");
    }

    // 2. Filter JTable dengan perulangan & percabangan
    private void filterTabelBarang(String kataKunci) {
        tableModel.setRowCount(0);
        String key = kataKunci.toLowerCase().trim();

        for (Barang b : daftarBarang) {
            if (b.getKodeBarang().toLowerCase().contains(key) || 
                b.getNamaBarang().toLowerCase().contains(key)) {
                Object[] row = {
                    b.getKodeBarang(),
                    b.getNamaBarang(),
                    b.getStok(),
                    b.getHargaJual()
                };
                tableModel.addRow(row);
            }
        }
    }

    // 3. Isi form input otomatis saat baris tabel diklik
    private void pilihBarangDariTabel() {
        int baris = tblBarang.getSelectedRow();
        if (baris != -1) {
            kodeTerpilih = tblBarang.getValueAt(baris, 0).toString();
            for (Barang b : daftarBarang) {
                if (b.getKodeBarang().equals(kodeTerpilih)) {
                    txtNamaBarang.setText(b.getNamaBarang());
                    txtHargaJual.setText(String.valueOf(b.getHargaJual()));
                    txtHargaModal.setText(String.valueOf(b.getHargaBeli()));
                    txtStokBarang.setText(String.valueOf(b.getStok()));
                    break;
                }
            }
        }
    }

    // 4a. Validasi Input Transaksi
    private boolean validasiInputTransaksi() {
        if (kodeTerpilih.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih barang dari tabel terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String inputJumlah = txtJumlahBeli.getText().trim();
        if (inputJumlah.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Jumlah beli tidak boleh kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            int jumlah = Integer.parseInt(inputJumlah);
            int stok = Integer.parseInt(txtStokBarang.getText());
            if (jumlah <= 0) {
                JOptionPane.showMessageDialog(this, "Jumlah beli harus lebih dari 0!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            if (jumlah > stok) {
                JOptionPane.showMessageDialog(this, "Jumlah beli melebihi stok tersedia (" + stok + ")!", "Stok Kurang", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah beli harus berupa angka bulat!", "Input Tidak Valid", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    // 4b. Validasi Input Tambah Stok
    private boolean validasiInputStok() {
        if (kodeTerpilih.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih barang yang akan ditambah stoknya!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String inputStok = txtTambahStok.getText().trim();
        if (inputStok.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kolom tambah stok tidak boleh kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            int tambahan = Integer.parseInt(inputStok);
            if (tambahan <= 0) {
                JOptionPane.showMessageDialog(this, "Jumlah tambahan stok harus lebih dari 0!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Input tambah stok harus berupa angka!", "Input Tidak Valid", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    // 5. Hitung Subtotal Item
    private double hitungSubtotalItem(double hargaJual, int jumlahBeli) {
        return hargaJual * jumlahBeli;
    }

    // 6. Tambah Item ke Keranjang
    private void tambahItemKeKeranjang() {
        if (!validasiInputTransaksi()) return;

        String nama = txtNamaBarang.getText();
        double hargaJual = Double.parseDouble(txtHargaJual.getText());
        double hargaModal = Double.parseDouble(txtHargaModal.getText());
        int jumlah = Integer.parseInt(txtJumlahBeli.getText().trim());
        double subtotal = hitungSubtotalItem(hargaJual, jumlah);

        keranjang.add(new ItemKeranjang(kodeTerpilih, nama, hargaModal, hargaJual, jumlah, subtotal));

        // Update tampilan areaKeranjang
        areaKeranjang.append("- " + nama + " x " + jumlah + " = Rp" + String.format("%,.0f", subtotal) + "\n");
        txtJumlahBeli.setText("");
    }

    // 7. Tambah Stok Barang
    private void tambahStokBarang() {
        if (!validasiInputStok()) return;

        int tambahan = Integer.parseInt(txtTambahStok.getText().trim());
        for (Barang b : daftarBarang) {
            if (b.getKodeBarang().equals(kodeTerpilih)) {
                int stokBaru = b.getStok() + tambahan;
                b.setStok(stokBaru);
                txtStokBarang.setText(String.valueOf(stokBaru));
                JOptionPane.showMessageDialog(this, "Stok " + b.getNamaBarang() + " berhasil ditambah menjadi " + stokBaru);
                break;
            }
        }
        txtTambahStok.setText("");
        simpanDataBarangKeFile();
        filterTabelBarang(txtCariBarang.getText());
    }

    // 8. Hitung Diskon 5% jika >= Rp100.000
    private double hitungDiskon(double totalBelanja) {
        if (totalBelanja >= 100000) {
            return totalBelanja * 0.05;
        } else {
            return 0;
        }
    }

    // Helper status stok (Percabangan)
    private String cekStatusStok(int sisaStok) {
        if (sisaStok <= 0) {
            return "Stok Habis";
        } else if (sisaStok <= 5) {
            return "Stok Menipis";
        } else {
            return "Stok Aman";
        }
    }

    // 9. Proses Keranjang, Update Stok & Hitung Keuntungan
    private void prosesKeranjangDanStok() {
        if (keranjang.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keranjang masih kosong! Tambahkan item terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double totalKotor = 0;
        double totalKeuntunganKotor = 0;
        StringBuilder infoStok = new StringBuilder();
        StringBuilder detailItem = new StringBuilder();

        for (ItemKeranjang item : keranjang) {
            totalKotor += item.getSubtotal();
            double untungItem = (item.getHargaJual() - item.getHargaBeli()) * item.getJumlahBeli();
            totalKeuntunganKotor += untungItem;

            detailItem.append(String.format("  • %s (%d x Rp%,.0f) = Rp%,.0f\n",
                    item.getNamaBarang(), item.getJumlahBeli(), item.getHargaJual(), item.getSubtotal()));

            // Potong stok pada master daftarBarang
            for (Barang b : daftarBarang) {
                if (b.getKodeBarang().equals(item.getKodeBarang())) {
                    int sisa = b.getStok() - item.getJumlahBeli();
                    b.setStok(sisa);
                    String status = cekStatusStok(sisa);
                    infoStok.append(String.format("  • %s -> Sisa: %d [%s]\n", b.getNamaBarang(), sisa, status));
                    break;
                }
            }
        }

        double diskon = hitungDiskon(totalKotor);
        double totalBayar = totalKotor - diskon;
        double keuntunganBersih = totalKeuntunganKotor - diskon;
        simpanDataBarangKeFile(); // <-- Simpan perubahan stok terbaru
        simpanRiwayatTransaksi(totalKotor, diskon, totalBayar, keuntunganBersih); // <-- Catat transaksi

        // Simpan hasil ke variabel untuk ditampilkan
        ringkasanTerakhir = "=== STRUK & RINGKASAN TRANSAKSI BAROKAH ZAYA ===\n"
                + "Rincian Belanja:\n" + detailItem.toString()
                + "--------------------------------------------------\n"
                + String.format("Total Kotor      : Rp%,.0f\n", totalKotor)
                + String.format("Diskon (5%%)      : Rp%,.0f\n", diskon)
                + String.format("Total Bayar      : Rp%,.0f\n", totalBayar)
                + String.format("Estimasi Untung  : Rp%,.0f\n", keuntunganBersih)
                + "--------------------------------------------------\n"
                + "Pembaruan Status Stok:\n" + infoStok.toString();

        filterTabelBarang(txtCariBarang.getText());
        keranjang.clear();
        areaKeranjang.setText("");
        JOptionPane.showMessageDialog(this, "Transaksi berhasil diproses! Klik 'Tampilkan Hasil' untuk melihat struk.");
    }
    
    private final String FILE_BARANG = "data_barang.csv";
    private final String FILE_TRANSAKSI = "riwayat_transaksi.csv";

    // 10. Tampilkan Hasil ke JTextArea
    private void tampilkanHasil() {
        if (ringkasanTerakhir.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Belum ada transaksi yang diproses!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        areaHasil.setText(ringkasanTerakhir);
    }

    // 11. Reset Form
    private void resetForm() {
        kodeTerpilih = "";
        ringkasanTerakhir = "";
        txtCariBarang.setText("");
        txtNamaBarang.setText("");
        txtHargaJual.setText("");
        txtHargaModal.setText("");
        txtStokBarang.setText("");
        txtJumlahBeli.setText("");
        txtTambahStok.setText("");
        areaKeranjang.setText("");
        areaHasil.setText("");
        keranjang.clear();
        tblBarang.clearSelection();
        filterTabelBarang("");
    }
    
    // A. Menyimpan seluruh isi ArrayList daftarBarang ke file CSV (Overwrite)
    private void simpanDataBarangKeFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_BARANG))) {
            for (Barang b : daftarBarang) {
                String baris = b.getKodeBarang() + ";"
                        + b.getNamaBarang() + ";"
                        + b.getHargaBeli() + ";"
                        + b.getHargaJual() + ";"
                        + b.getStok();
                writer.write(baris);
                writer.newLine();
            }
        } catch(IOException e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data barang: " 
                    + e.getMessage(), "Error File", JOptionPane.ERROR_MESSAGE);
        }
    }

    // B. Membaca data dari file CSV saat aplikasi pertama kali dibuka
    private boolean muatDataBarangDariFile() {
        File file = new File(FILE_BARANG);
        if (!file.exists()) {
            return false; // File belum ada (aplikasi baru pertama kali dijalankan)
        }
        
        daftarBarang.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String baris;
            while ((baris = reader.readLine()) != null) {
                if (baris.trim().isEmpty()) continue;
                String[] data = baris.split(";");
                if (data.length == 5) {
                    String kode = data[0];
                    String nama = data[1];
                    double beli = Double.parseDouble(data[2]);
                    double jual = Double.parseDouble(data[3]);
                    int stok = Integer.parseInt(data[4]);
                    daftarBarang.add(new Barang(kode, nama, beli, jual, stok));
                }
            }
            return true;
        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Gagal membaca file database lokal: "
                    + e.getMessage(), "Error File", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // C. Mencatat riwayat transaksi & keuntungan ke file CSV (Append / Tambah baris baru)
    private void simpanRiwayatTransaksi(double totalKotor, double diskon, double totalBayar, double untungBersih) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String waktu = LocalDateTime.now().format(dtf);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_TRANSAKSI, true))) {
            String baris = waktu + ";" + totalKotor + ";" + diskon + ";" + totalBayar + ";" + untungBersih;
            writer.write(baris);
            writer.newLine();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Gagal mencatat riwayat transaksi: " 
                    + e.getMessage(), "Error File", JOptionPane.ERROR_MESSAGE);
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Title = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtCariBarang = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblBarang = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        txtNamaBarang = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtHargaJual = new javax.swing.JTextField();
        txtHargaModal = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtStokBarang = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtJumlahBeli = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtTambahStok = new javax.swing.JTextField();
        btnCancel = new javax.swing.JButton();
        btnTambahStok = new javax.swing.JButton();
        btnTambahItem = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        areaKeranjang = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        areaHasil = new javax.swing.JTextArea();
        btnProses = new javax.swing.JButton();
        btnTampilkanHasil = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        Title.setFont(new java.awt.Font("Arial Black", 0, 18)); // NOI18N
        Title.setText("APLIKASI KASIR TOKO SEMBAKO \"BAROKAH ZAYA\"");

        jLabel1.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
        jLabel1.setText("Cari Nama Barang:");

        txtCariBarang.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtCariBarangKeyReleased(evt);
            }
        });

        jScrollPane1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jScrollPane1MouseClicked(evt);
            }
        });

        tblBarang.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblBarang.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblBarangMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblBarang);

        jLabel2.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
        jLabel2.setText("Nama Barang:");

        txtNamaBarang.setEditable(false);

        jLabel3.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
        jLabel3.setText("Harga Barang:");

        txtHargaJual.setEditable(false);

        txtHargaModal.setEditable(false);

        jLabel4.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
        jLabel4.setText("Harga Modal:");

        jLabel5.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
        jLabel5.setText("Stok Barang:");

        txtStokBarang.setEditable(false);

        jLabel6.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
        jLabel6.setText("Jumlah Beli:");

        jLabel7.setFont(new java.awt.Font("Arial Black", 0, 14)); // NOI18N
        jLabel7.setText("Tambah Stok:");

        btnCancel.setText("Cancel");

        btnTambahStok.setText("Tambah Stok");
        btnTambahStok.addActionListener(this::btnTambahStokActionPerformed);

        btnTambahItem.setText("Tambah Keranjang");
        btnTambahItem.addActionListener(this::btnTambahItemActionPerformed);

        areaKeranjang.setColumns(20);
        areaKeranjang.setRows(5);
        jScrollPane2.setViewportView(areaKeranjang);

        areaHasil.setEditable(false);
        areaHasil.setColumns(20);
        areaHasil.setRows(5);
        jScrollPane3.setViewportView(areaHasil);

        btnProses.setText("Hitung");
        btnProses.addActionListener(this::btnProsesActionPerformed);

        btnTampilkanHasil.setText("Hasil");
        btnTampilkanHasil.addActionListener(this::btnTampilkanHasilActionPerformed);

        btnReset.setText("Reset");
        btnReset.addActionListener(this::btnResetActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCariBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 580, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtJumlahBeli))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtNamaBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnTambahStok)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnTambahItem))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtTambahStok))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtHargaJual, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtHargaModal))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtStokBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane2)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 803, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnProses, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnTampilkanHasil, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(22, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(Title)
                .addGap(186, 186, 186))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Title)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtCariBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(txtNamaBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(txtHargaJual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtHargaModal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(txtStokBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(txtTambahStok, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(txtJumlahBeli, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnTambahStok, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnTambahItem, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnProses, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnTampilkanHasil, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jScrollPane1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jScrollPane1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jScrollPane1MouseClicked

    private void txtCariBarangKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCariBarangKeyReleased
        // TODO add your handling code here:
        filterTabelBarang(txtCariBarang.getText());
    }//GEN-LAST:event_txtCariBarangKeyReleased

    private void btnTambahStokActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahStokActionPerformed
        // TODO add your handling code here:
        tambahStokBarang();
    }//GEN-LAST:event_btnTambahStokActionPerformed

    private void btnTambahItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahItemActionPerformed
        // TODO add your handling code here:
        tambahItemKeKeranjang();
    }//GEN-LAST:event_btnTambahItemActionPerformed

    private void btnProsesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProsesActionPerformed
        // TODO add your handling code here:
        prosesKeranjangDanStok();
    }//GEN-LAST:event_btnProsesActionPerformed

    private void btnTampilkanHasilActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTampilkanHasilActionPerformed
        // TODO add your handling code here:
        tampilkanHasil();
    }//GEN-LAST:event_btnTampilkanHasilActionPerformed

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        // TODO add your handling code here:
        resetForm();
    }//GEN-LAST:event_btnResetActionPerformed

    private void tblBarangMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblBarangMouseClicked
        // TODO add your handling code here:
        pilihBarangDariTabel();
    }//GEN-LAST:event_tblBarangMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new FormKasirUtama().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Title;
    private javax.swing.JTextArea areaHasil;
    private javax.swing.JTextArea areaKeranjang;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnProses;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnTambahItem;
    private javax.swing.JButton btnTambahStok;
    private javax.swing.JButton btnTampilkanHasil;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable tblBarang;
    private javax.swing.JTextField txtCariBarang;
    private javax.swing.JTextField txtHargaJual;
    private javax.swing.JTextField txtHargaModal;
    private javax.swing.JTextField txtJumlahBeli;
    private javax.swing.JTextField txtNamaBarang;
    private javax.swing.JTextField txtStokBarang;
    private javax.swing.JTextField txtTambahStok;
    // End of variables declaration//GEN-END:variables
}
