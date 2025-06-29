CREATE TABLE Role_pengguna (
id_role SERIAL PRIMARY KEY,
nama_role VARCHAR(50) NOT NULL
);

CREATE TABLE Cabang (
id_cabang SERIAL PRIMARY KEY,
nama_cabang VARCHAR(100) NOT NULL,
alamat VARCHAR(255) NOT NULL,
kota VARCHAR(100) NOT NULL
);

CREATE TABLE Status_pesanan (
id_status SERIAL PRIMARY KEY,
status VARCHAR(50) NOT NULL,
waktu TIMESTAMP NOT NULL
);

CREATE TABLE Promo (
id_promo SERIAL PRIMARY KEY,
nama_promo VARCHAR(100) NOT NULL,
deskripsi TEXT,
diskon INT NOT NULL,
tgl_mulai DATE NOT NULL,
tgl_selesai DATE NOT NULL
);

CREATE TABLE Staf (
id_staf SERIAL PRIMARY KEY,
nama_staf VARCHAR(100) NOT NULL,
jabatan VARCHAR(50) NOT NULL
);

-- Tabel Pengguna dengan id_cabang yang sudah termasuk
CREATE TABLE Pengguna (
id_pengguna SERIAL PRIMARY KEY,
nama VARCHAR(100) NOT NULL,
username VARCHAR(100) UNIQUE NOT NULL,
password VARCHAR(100) NOT NULL,
id_role INT REFERENCES Role_pengguna(id_role),
id_cabang INT REFERENCES Cabang(id_cabang)
);

CREATE TABLE Menu (
id_menu SERIAL PRIMARY KEY,
nama_menu VARCHAR(100) NOT NULL,
deskripsi TEXT,
harga INT NOT NULL,
kategori VARCHAR(50),
stok INT NOT NULL DEFAULT 0,
tersedia BOOLEAN NOT NULL DEFAULT TRUE,
id_cabang INT REFERENCES Cabang(id_cabang)
);

CREATE TABLE Log_aktivitas (
id_log SERIAL PRIMARY KEY,
id_pengguna INT REFERENCES Pengguna(id_pengguna),
aktivitas VARCHAR(255) NOT NULL,
timestamp TIMESTAMP NOT NULL
);

CREATE TABLE Promo_cabang (
id_promo INT REFERENCES Promo(id_promo),
id_cabang INT REFERENCES Cabang(id_cabang),
PRIMARY KEY (id_promo, id_cabang)
);

CREATE TABLE Pemesanan (
id_pemesanan SERIAL PRIMARY KEY,
id_pengguna INT REFERENCES Pengguna(id_pengguna),
id_status INT REFERENCES Status_pesanan(id_status),
id_cabang INT REFERENCES Cabang(id_cabang),
tgl_pesan DATE NOT NULL,
total_harga INT NOT NULL,
id_review INT,
rating INT,
komentar TEXT,
tgl_review DATE
);

CREATE TABLE Detil_pesanan (
id_pemesanan INT REFERENCES Pemesanan(id_pemesanan),
id_menu INT REFERENCES Menu(id_menu),
jumlah INT NOT NULL,
subtotal INT NOT NULL,
PRIMARY KEY (id_pemesanan, id_menu)
);

CREATE TABLE Jadwal_pengiriman (
id_jadwal SERIAL PRIMARY KEY,
id_pengiriman INT REFERENCES Pemesanan(id_pemesanan),
id_staf INT REFERENCES Staf(id_staf),
waktu_kirim TIMESTAMP NOT NULL,
alamat_pengiriman VARCHAR(255) NOT NULL
);

INSERT INTO Role_pengguna (id_role, nama_role) VALUES
(1, 'Pelanggan'),
(2, 'Admin Cabang'),
(3, 'Admin Pusat');

INSERT INTO Cabang (nama_cabang, alamat, kota) VALUES
('Cabang Utama', 'Jl. Merdeka 1', 'Jakarta'),
('Cabang Timur', 'Jl. Timur 2', 'Surabaya'),
('Cabang Barat', 'Jl. Barat 3', 'Bandung'),
('Cabang Selatan', 'Jl. Selatan 4', 'Yogyakarta'),
('Cabang Utara', 'Jl. Utara 5', 'Medan');

INSERT INTO Status_pesanan (status, waktu) VALUES
('Pending', '2025-06-01 10:00:00'),
('Processed', '2025-06-01 11:00:00'),
('Shipped', '2025-06-02 15:00:00'),
('Delivered', '2025-06-03 18:00:00'),
('Cancelled', '2025-06-04 16:00:00');

INSERT INTO Promo (nama_promo, deskripsi, diskon, tgl_mulai, tgl_selesai) VALUES
('Diskon Lebaran', 'Diskon 10% untuk semua menu', 10, '2025-06-01', '2025-06-10'),
('Promo Akhir Pekan', 'Diskon 15% untuk minuman', 15, '2025-06-05', '2025-06-07'),
('Promo Ulang Tahun', 'Gratis menu untuk pelanggan yang berulang tahun', 100, '2025-06-01', '2025-06-30'),
('Promo Hari Kemerdekaan', 'Diskon 20% untuk semua pesanan', 20, '2025-08-17', '2025-08-17'),
('Diskon Tengah Bulan', 'Diskon 5% untuk pesanan di atas 50k', 5, '2025-06-15', '2025-06-20'),
('Diskon Tengah Tahun', 'Diskon 7% untuk pelanggan tercinta', 8, '2025-06-29', '2025-07-29'),
('Diskon 10th Anniversary', 'Diskon 10% untuk pelanggan setia katering', 10, '2025-07-01', '2025-12-01');

INSERT INTO Staf (nama_staf, jabatan) VALUES
('Hendra', 'Manager'),
('Susi', 'Koki'),
('Andi', 'Pelayan'),
('Rina', 'Kasir'),
('Doni', 'Driver');

-- Sisipkan data Pengguna dengan id_cabang yang sesuai untuk Admin Cabang
INSERT INTO Pengguna (nama, username, password, id_role, id_cabang) VALUES
('John Doe', 'john', 'password123', 2, 1),
('Jane Smith', 'jane', 'securepass', 2, 2),
('Alice Johnson', 'alice', '12345abc', 1, NULL),
('Bob Brown', 'bob', 'qwerty123', 1, NULL),
('Charlie White', 'charlie', 'pass123', 3, NULL);

INSERT INTO Menu (nama_menu, deskripsi, harga, kategori, stok, tersedia, id_cabang) VALUES
('Nasi Goreng Spesial', 'Nasi goreng dengan ayam, telur, dan kerupuk', 25000, 'Makanan', 50, TRUE, 1),
('Mie Ayam Bakso', 'Mie ayam lengkap dengan bakso sapi', 28000, 'Makanan', 40, TRUE, 1),
('Sate Ayam Madura', 'Sate ayam dengan bumbu kacang khas Madura', 22000, 'Makanan', 60, TRUE, 2),
('Es Jeruk Dingin', 'Minuman segar dari perasan jeruk asli', 10000, 'Minuman', 100, TRUE, 1),
('Kopi Susu Kekinian', 'Kopi dengan campuran susu dan gula aren', 18000, 'Minuman', 75, TRUE, 2),
('Paket Hemat A', 'Nasi Goreng + Es Teh Manis', 30000, 'Paket', 30, TRUE, 1),
('Paket Keluarga', 'Ayam Bakar (Whole) + 4 Nasi + 4 Es Teh', 120000, 'Paket', 15, TRUE, 3);

INSERT INTO Log_aktivitas (id_pengguna, aktivitas, timestamp) VALUES
(1, 'Login', '2025-06-01 08:00:00'),
(2, 'Logout', '2025-06-01 12:00:00'),
(3, 'Update profile', '2025-06-02 09:30:00'),
(4, 'Place order', '2025-06-03 14:20:00'),
(5, 'Cancel order', '2025-06-04 10:15:00');

INSERT INTO Promo_cabang (id_promo, id_cabang) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5),
(6, 1), -- Promo baru 'Diskon Tengah Tahun' untuk Cabang Utama
(7, 2); -- Promo baru 'Diskon 10th Anniversary' untuk Cabang Timur

INSERT INTO Pemesanan (id_pengguna, id_status, id_cabang, tgl_pesan, total_harga, id_review, rating, komentar, tgl_review) VALUES
(1, 1, 1, '2025-06-01', 75000, 1, 5, 'Pelayanan memuaskan', '2025-06-02'),
(2, 2, 2, '2025-06-02', 50000, 2, 4, 'Makanan enak', '2025-06-03'),
(3, 3, 3, '2025-06-03', 45000, 3, 3, 'Agak lama pengirimannya', '2025-06-04'),
(4, 4, 4, '2025-06-04', 60000, 4, 4, 'Pesanan sesuai', '2025-06-05'),
(5, 5, 5, '2025-06-05', 30000, 5, 5, 'Sangat memuaskan', '2025-06-06');

INSERT INTO Detil_pesanan (id_pemesanan, id_menu, jumlah, subtotal) VALUES
(1, 1, 2, 50000),
(1, 5, 2, 10000),
(2, 2, 2, 40000),
(3, 3, 1, 30000),
(4, 4, 2, 30000);

INSERT INTO Jadwal_pengiriman (id_pengiriman, id_staf, waktu_kirim, alamat_pengiriman) VALUES
(1, 1, '2025-06-01 12:00:00', 'Jl. Mawar No. 5, Jakarta'),
(2, 2, '2025-06-02 13:00:00', 'Jl. Melati No. 3, Surabaya'),
(3, 3, '2025-06-03 14:00:00', 'Jl. Kenanga No. 7, Bandung'),
(4, 4, '2025-06-04 15:00:00', 'Jl. Dahlia No. 2, Yogyakarta'),
(5, 5, '2025-06-05 16:00:00', 'Jl. Anggrek No. 4, Medan');