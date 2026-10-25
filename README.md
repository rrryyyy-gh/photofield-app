# PhotoField App

Aplikasi Android untuk dokumentasi foto lapangan dengan lokasi GPS, pengelolaan proyek/activity, dan ekspor PDF unlimited.

## Fitur Utama

- 📸 **Kamera dengan Autofokus** - Menggunakan CameraX, support autofokus kontinu
- 📍 **GPS Otomatis** - Setiap foto otomatis ditag lokasi GPS (lat/long, akurasi, altitude)
- 📁 **Manajemen Proyek & Activity** - Hierarki: Proyek → Activity → Foto (tanpa batas jumlah)
- 🖼️ **Edit Foto per Activity** - Hapus, tambah, urutkan ulang foto (drag & drop)
- 📄 **Ekspor PDF Unlimited** - Semua foto di activity masuk PDF, nggak dibatasi 4 foto
- 🎨 **Template PDF Custom** - Header, footer, watermark, orientasi, foto per halaman
- 🗺️ **Peta Lokasi** - Lihat semua foto di peta (Google Maps)
- 💾 **Offline-First** - Room Database, semua data lokal, sync nanti ke cloud
- ⚙️ **Pengaturan Lengkap** - Kamera, PDF, penyimpanan, backup

## Tech Stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **CameraX** untuk kamera
- **FusedLocationProvider** untuk GPS
- **Room Database** untuk offline storage
- **Hilt** untuk Dependency Injection
- **Google Maps Compose** untuk peta
- **PdfDocument** (Android built-in) untuk generate PDF
- **Coil** untuk image loading
- **DataStore** untuk preferences
- **Kotlinx Serialization** & **DateTime**

## Struktur Project

```
app/
├── src/main/java/com/photofieldapp/
│   ├── data/
│   │   ├── local/           # Room entities, DAOs, Database
│   │   └── repository/      # Repository implementations
│   ├── domain/
│   │   ├── model/           # Domain models
│   │   └── usecase/         # Use cases (business logic)
│   ├── presentation/
│   │   ├── ui/              # Compose screens
│   │   │   ├── projectlist/
│   │   │   ├── projectdetail/
│   │   │   ├── activitydetail/
│   │   │   ├── pdfpreview/
│   │   │   ├── map/
│   │   │   └── settings/
│   │   └── viewmodel/       # ViewModels
│   ├── di/                  # Hilt modules
│   ├── util/                # Utilities (Camera, Location, PDF, File)
│   ├── PhotoFieldApplication.kt
│   └── MainActivity.kt
├── src/main/res/
│   ├── values/              # strings, colors, themes
│   └── xml/                 # file_paths, backup_rules
└── build.gradle.kts
```

## Cara Build & Jalankan

1. **Clone project** ke Android Studio
2. **Tambahkan Google Maps API Key** di `AndroidManifest.xml`:
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="YOUR_API_KEY_HERE" />
   ```
3. **Sync Gradle** & jalankan di device/emulator (min API 26)

## Permissions Required

- `CAMERA` - Untuk mengambil foto
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` - Untuk GPS
- `READ_MEDIA_IMAGES` (API 33+) / `READ_EXTERNAL_STORAGE` - Untuk akses galeri
- `INTERNET` - Untuk Google Maps & backup cloud

## Arsitektur

MVVM + Repository + Use Cases + Clean Architecture principles:

```
UI (Compose) → ViewModel → UseCase → Repository → Room DAO → Database
                    ↓
              CameraX / Location / PDF Generator
```

## Customization

### Template PDF
Edit di Settings → Template PDF. Placeholders tersedia:
- `{project}` - Nama proyek
- `{activity}` - Nama activity
- `{page}` - Nomor halaman
- `{total}` - Total halaman
- `{date}` - Tanggal hari ini

### Camera Settings
- Autofokus: On/Off
- Flash Mode: Auto/On/Off
- Camera: Back/Front

### PDF Settings
- Orientasi: Potret/Lanskap
- Foto per halaman: 1-6
- Sertakan GPS: Ya/Tidak
- Sertakan Timestamp: Ya/Tidak

## License

MIT License - Feel free to use for personal or commercial projects.

## Credits

- CameraX, Room, Hilt, Compose by Google
- Google Maps SDK
- Coil Image Loading
- Kotlinx Coroutines, Serialization, DateTime