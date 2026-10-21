# JARVIS Android kaynak projesi

Bu, Android APK derlemek için kaynak projedir; ZIP'in kendisi APK değildir.

## APK üretme (AndroidIDE gerekmez)
1. ZIP'i bilgisayarda veya telefonda aç ve dosyaları yeni bir GitHub deposuna yükle.
2. `main` dalına gönder. GitHub'da **Actions** sekmesinden **Build JARVIS APK** iş akışını çalıştır.
3. İşlem başarılı olursa `JARVIS-debug-APK` artifact'ini indir; içinden `app-debug.apk` dosyasını çıkar.
4. APK test amaçlı debug imzalıdır. Telefonun güvenlik uyarılarını gelişigüzel kapatma; yalnızca kendi oluşturduğun dosyayı kur.

## Gerçek AI bağlantısı
`server/` klasörünü HTTPS destekli Node.js sunucusuna dağıt. Sunucuda `OPENAI_API_KEY` ortam değişkenini ayarla; isteğe bağlı `OPENAI_MODEL` değişkeniyle erişimin olan modeli seçebilirsin. API anahtarını APK'ya, HTML'e veya GitHub deposuna koyma. Uygulamada sunucunun HTTPS kök adresini girip Kaydet'e bas. API kullanımının ücretli olabileceğini ve kota belirlemen gerektiğini unutma.

## Ses
Android sisteminin konuşma tanıma ve Türkçe metin-okuma motorlarını kullanır. Filmdeki sesin birebir kopyası değildir.
