# 🚀 BuzzAI: Yapay Zeka Destekli Yaratıcı Ajans Uygulaması

BuzzAI, sıradan ürün veya portre fotoğraflarını saniyeler içinde yüksek kaliteli, profesyonel stüdyo konseptlerine dönüştüren yapay zeka tabanlı bir Android mobil uygulamasıdır.

## 🎯 Proje Amacı
İşletmelerin ve bireylerin pahalı fotoğraf çekimlerine ihtiyaç duymadan, mobil cihazları üzerinden yaratıcı ajans kalitesinde (Cyberpunk, Lüks Mermer, Neon vb.) içerik üretebilmesini sağlamak.

## ✨ Temel Özellikler
* **Dinamik AI Konsept Vitrini:** Yatay kaydırılabilir, görsel ağırlıklı keşif arayüzü.
* **REST API Entegrasyonu:** Stable Diffusion 3 (SD3) uç noktası üzerinden `Retrofit2` ile asenkron görsel işleme (image-to-image).
* **Fil Hafızası (SharedPreferences):** Uygulama kapansa bile son kalınan çalışma alanının ve geçmiş verilerin yerel cihazda güvenle saklanması.
* **Gelişmiş UX:** Bildirim yönetimi (NotificationManager), tam boy görsel önizleme, cihaza (Galeri) kaydetme ve diğer platformlarda paylaşma (Intent).

## 🛠 Kullanılan Teknolojiler ve Mimariler
* **Dil:** Kotlin
* **Mimari:** Single Activity - Multiple Fragments (Navigation)
* **Ağ İşlemleri:** Retrofit2, OkHttp
* **Görsel İşleme ve Caching:** Glide
* **Yerel Depolama:** SharedPreferences
* **Asenkron İşlemler:** Kotlin Coroutines
* **Versiyon Kontrol:** Git & GitHub

## 👤 Geliştirici
**Muhammed Kaya**
Manisa Celal Bayar Üniversitesi - Yazılım Mühendisliği Bölümü
(Proje, dönem sonu bitirme / vize ödevi kapsamında geliştirilmiştir.)
