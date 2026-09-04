#!/usr/bin/env python3
"""
Noor Quran - Authoritative Quran Dataset Preparation & Validation Script

This script downloads and verifies:
1. Uthmani Arabic text from authoritative source (Tanzil / Al-Quran Cloud)
2. Saheeh International English translation
3. Muhiuddin Khan Bengali translation
4. Official Surah and Juz metadata

Performs strict validation:
- Exactly 114 Surahs
- Correct Surah order (1 to 114)
- Correct Ayah count for every Surah (standard 6236 total)
- No missing, duplicate, or reordered Ayahs
- Non-empty Arabic text, English translation, Bengali translation
- Prepares JSON and SQLite formats in app/src/main/assets/quran/
- Computes SHA-256 checksum for data integrity verification
"""

import json
import os
import sys
import sqlite3
import hashlib
import time
import urllib.request

EXPECTED_AYAH_COUNTS = [
    7, 286, 200, 176, 120, 165, 206, 75, 129, 109,
    123, 111, 43, 52, 99, 128, 111, 110, 98, 135,
    112, 78, 118, 64, 77, 227, 93, 88, 69, 60,
    34, 30, 73, 54, 45, 83, 182, 88, 75, 85,
    54, 53, 89, 59, 37, 35, 38, 29, 18, 45,
    60, 49, 62, 55, 78, 96, 29, 22, 24, 13,
    14, 11, 11, 18, 12, 12, 30, 52, 52, 44,
    28, 28, 20, 56, 40, 31, 50, 40, 46, 42,
    29, 19, 36, 25, 22, 17, 19, 26, 30, 20,
    15, 21, 11, 8, 8, 19, 5, 8, 8, 11,
    11, 8, 3, 9, 5, 4, 7, 3, 6, 3,
    5, 4, 5, 6
]

BENGALI_SURAH_NAMES = [
    ("আল-ফাতিহা", "সূচনা"),
    ("আল-বাকারা", "বকনা বাছুর"),
    ("আলে ইমরান", "ইমরানের পরিবার"),
    ("আন-নিসা", "নারী"),
    ("আল-মায়িদাহ", "খাদ্য পরিবেশিত টেবিল"),
    ("আল-আনআম", "গৃহপালিত পশু"),
    ("আল-আরাফ", "উঁচু স্থানসমূহ"),
    ("আল-আনফাল", "যুদ্ধলব্ধ ধন-সম্পদ"),
    ("আত-তাওবাহ", "অনুশোচনা"),
    ("ইউনুস", "ইউনুস (নবী)"),
    ("হুদ", "হুদ (নবী)"),
    ("ইউসুফ", "ইউসুফ (নবী)"),
    ("আর-রাদ", "বজ্রপাত"),
    ("ইব্রাহিম", "ইব্রাহিম (নবী)"),
    ("আল-হিজর", "পাথুরে পাহাড়"),
    ("আন-নাহল", "মৌমাছি"),
    ("বনী ইসরাঈল", "ইসরাঈলের সন্তানগণ"),
    ("আল-কাহফ", "গুহা"),
    ("মারইয়াম", "মরিয়ম"),
    ("ত্বা-হা", "ত্বা-হা"),
    ("আল-আম্বিয়া", "নবীগণ"),
    ("আল-হাজ্জ", "হজ্জ"),
    ("আল-মুমিনুন", "বিশ্বাসীগণ"),
    ("আন-নূর", "আলো"),
    ("আল-ফুরকান", "সত্য-মিথ্যার পার্থক্যকারী"),
    ("আশ-শুয়ারা", "কবিগণ"),
    ("আন-নামল", "পিপীলিকা"),
    ("আল-কাসাস", "ইতিবৃত্ত"),
    ("আল-আনকাবুত", "মাকড়সা"),
    ("আর-রূম", "রোমান জাতি"),
    ("লুকমান", "লুকমান"),
    ("আস-সাজদাহ", "সিজদা"),
    ("আল-আহযাব", "জোটবদ্ধ বাহিনী"),
    ("সাবা", "সাবা জাতি"),
    ("ফাতির", "আদি স্রষ্টা"),
    ("ইয়াসীন", "ইয়াসীন"),
    ("আস-সাফফাত", "সারিবদ্ধ দলসমূহ"),
    ("সোয়াদ", "সোয়াদ"),
    ("আজ-জুমার", "দলবদ্ধ জনতা"),
    ("গাফির", "ক্ষমাশীল"),
    ("ফুসসিলাত", "সুস্পষ্ট বিবরণ"),
    ("আশ-শূরা", "পরামর্শ"),
    ("আজ-জুখরূফ", "সোনার অলংকার"),
    ("আদ-দুখান", "ধোঁয়া"),
    ("আল-জাসিয়াহ", "নতজানু"),
    ("আল-আহকাফ", "বালুর পাহাড়"),
    ("মুহাম্মদ", "মুহাম্মদ (সা.)"),
    ("আল-ফাতহ", "বিজয়"),
    ("আল-হুজুরাত", "বাসগৃহসমূহ"),
    ("কাফ", "কাফ"),
    ("আজ-যারিয়াত", "বিক্ষিপ্তকারী বাতাস"),
    ("আত-তূর", "তূর পর্বত"),
    ("আন-নাজম", "তারা"),
    ("আল-কামার", "চাঁদ"),
    ("আর-রাহমান", "পরম দয়ালু"),
    ("আল-ওয়াকিয়াহ", "অবধারিত ঘটনা"),
    ("আল-হাদীদ", "লোহা"),
    ("আল-মুজাদালাহ", "অনুযোগকারিণী"),
    ("আল-হাশর", "সমাবেশ"),
    ("আল-মুমতাহানাহ", "পরীক্ষিতা নারী"),
    ("আস-সফ", "সারিবদ্ধ সৈন্যদল"),
    ("আল-জুমুআহ", "শুক্রবার"),
    ("আল-মুনাফিকুন", "ভণ্ড বিশ্বাসী"),
    ("আত-তাগাবুন", "ক্ষতি ও লাভ"),
    ("আত-তালাক", "তালাক"),
    ("আত-তাহরীম", "নিষিদ্ধকরণ"),
    ("আল-মুলক", "সার্বভৌম কর্তৃত্ব"),
    ("আল-কলম", "কলম"),
    ("আল-হাক্কাহ", "নিশ্চিত সত্য"),
    ("আল-মাআরিজ", "উন্নয়নের সোপান"),
    ("নূহ", "নূহ (নবী)"),
    ("আল-জ্বিন", "জ্বিন জাতি"),
    ("আল-মুযযাম্মিল", "বস্ত্রাবৃত"),
    ("আল-মুদ্দাসসির", "পোশাক পরিহিত"),
    ("আল-কিয়ামাহ", "পুনরুত্থান"),
    ("আল-ইনসান", "মানবজাতি"),
    ("আল-মুরসালাত", "প্রেরিত বাতাস"),
    ("আন-নাবা", "মহাসংবাদ"),
    ("আন-নাযিআত", "উৎপাটনকারী ফেরেশতা"),
    ("আবাসা", "তিনি ভ্রূকুটি করলেন"),
    ("আত-তাকবীর", "সূর্য মলিন হওয়া"),
    ("আল-ইনফিতার", "বিদীর্ণ হওয়া"),
    ("আল-মুতাফফিফীন", "মাপে কমদানকারী"),
    ("আল-ইনশিকাক", "ফাটিয়ে ফেলা"),
    ("আল-বুরুজ", "নক্ষত্রপুঞ্জ"),
    ("আত-তারিক", "রাতের আগমনকারী"),
    ("আল-আলা", "সর্বোচ্চ মহান"),
    ("আল-গাশিয়াহ", "আচ্ছন্নকারী সংকট"),
    ("আল-ফাজর", "ভোরবেলা"),
    ("আল-বালাদ", "নগরী"),
    ("আশ-শামস", "সূর্য"),
    ("আল-লাইল", "রাত"),
    ("আদ-দুহা", "পূর্বাহ্ণ"),
    ("আল-ইনশিরাহ", "বক্ষ প্রশস্তকরণ"),
    ("আত-তীন", "ডুমুর ফল"),
    ("আল-আলাক", "রক্তপিণ্ড"),
    ("আল-কদর", "মহিমান্বিত রাত"),
    ("আল-বাইয্যিনাহ", "সুস্পষ্ট প্রমাণ"),
    ("আল-যিলযাল", "মহাকম্পন"),
    ("আল-আদিয়াত", "অভিযানকারী অশ্ব"),
    ("আল-কারিয়াহ", "মহা বিপদ"),
    ("আত-তাকাসুর", "প্রাচুর্যের প্রতিযোগিতা"),
    ("আল-আসর", "কাল / সময়"),
    ("আল-হুমাযাহ", "পরনিন্দাকারী"),
    ("আল-ফিল", "হাতি"),
    ("কুরাইশ", "কুরাইশ বংশ"),
    ("আল-মাউন", "নিত্য ব্যবহার্য বস্তু"),
    ("আল-কাওসার", "প্রচুর কল্যাণ"),
    ("আল-কাফিরুন", "অবিশ্বাসীগণ"),
    ("আন-নাসর", "সাহায্য"),
    ("আল-লাহাব", "অগ্নিশিখা"),
    ("আল-ইখলাস", "একনিষ্ঠতা"),
    ("আল-ফালাক", "ঊষাকাল"),
    ("আন-নাস", "মানবজাতি")
]

JUZ_METADATA = [
    {"juz": 1, "startSurah": 1, "startAyah": 1, "endSurah": 2, "endAyah": 141, "nameArabic": "آلم"},
    {"juz": 2, "startSurah": 2, "startAyah": 142, "endSurah": 2, "endAyah": 252, "nameArabic": "سَيَقُولُ"},
    {"juz": 3, "startSurah": 2, "startAyah": 253, "endSurah": 3, "endAyah": 92, "nameArabic": "تِلْكَ الرُّسُلُ"},
    {"juz": 4, "startSurah": 3, "startAyah": 93, "endSurah": 4, "endAyah": 23, "nameArabic": "لَنْ تَنَالُوا"},
    {"juz": 5, "startSurah": 4, "startAyah": 24, "endSurah": 4, "endAyah": 147, "nameArabic": "وَالْمُحْصَنَاتُ"},
    {"juz": 6, "startSurah": 4, "startAyah": 148, "endSurah": 5, "endAyah": 81, "nameArabic": "لَا يُحِبُّ اللَّهُ"},
    {"juz": 7, "startSurah": 5, "startAyah": 82, "endSurah": 6, "endAyah": 110, "nameArabic": "وَإِذَا سَمِعُوا"},
    {"juz": 8, "startSurah": 6, "startAyah": 111, "endSurah": 7, "endAyah": 87, "nameArabic": "وَلَوْ أَنَّنَا"},
    {"juz": 9, "startSurah": 7, "startAyah": 88, "endSurah": 8, "endAyah": 40, "nameArabic": "قَالَ الْمَلَأُ"},
    {"juz": 10, "startSurah": 8, "startAyah": 41, "endSurah": 9, "endAyah": 92, "nameArabic": "وَاعْلَمُوا"},
    {"juz": 11, "startSurah": 9, "startAyah": 93, "endSurah": 11, "endAyah": 5, "nameArabic": "يَعْتَذِرُونَ"},
    {"juz": 12, "startSurah": 11, "startAyah": 6, "endSurah": 12, "endAyah": 52, "nameArabic": "وَمَا مِنْ دَابَّةٍ"},
    {"juz": 13, "startSurah": 12, "startAyah": 53, "endSurah": 14, "endAyah": 52, "nameArabic": "وَمَا أُبَرِّئُ"},
    {"juz": 14, "startSurah": 15, "startAyah": 1, "endSurah": 16, "endAyah": 128, "nameArabic": "رُبَمَا"},
    {"juz": 15, "startSurah": 17, "startAyah": 1, "endSurah": 18, "endAyah": 74, "nameArabic": "سُبْحَانَ الَّذِي"},
    {"juz": 16, "startSurah": 18, "startAyah": 75, "endSurah": 20, "endAyah": 135, "nameArabic": "قَالَ أَلَمْ"},
    {"juz": 17, "startSurah": 21, "startAyah": 1, "endSurah": 22, "endAyah": 78, "nameArabic": "اقْتَرَبَ"},
    {"juz": 18, "startSurah": 23, "startAyah": 1, "endSurah": 25, "endAyah": 20, "nameArabic": "قَدْ أَفْلَحَ"},
    {"juz": 19, "startSurah": 25, "startAyah": 21, "endSurah": 27, "endAyah": 55, "nameArabic": "وَقَالَ الَّذِينَ"},
    {"juz": 20, "startSurah": 27, "startAyah": 56, "endSurah": 29, "endAyah": 45, "nameArabic": "أَمَّنْ خَلَقَ"},
    {"juz": 21, "startSurah": 29, "startAyah": 46, "endSurah": 33, "endAyah": 30, "nameArabic": "اتْلُ مَا أُوحِيَ"},
    {"juz": 22, "startSurah": 33, "startAyah": 31, "endSurah": 36, "endAyah": 27, "nameArabic": "وَمَنْ يَقْنُتْ"},
    {"juz": 23, "startSurah": 36, "startAyah": 28, "endSurah": 39, "endAyah": 31, "nameArabic": "وَمَا أَنْزَلْنَا"},
    {"juz": 24, "startSurah": 39, "startAyah": 32, "endSurah": 41, "endAyah": 46, "nameArabic": "فَمَنْ أَظْلَمُ"},
    {"juz": 25, "startSurah": 41, "startAyah": 47, "endSurah": 45, "endAyah": 37, "nameArabic": "إِلَيْهِ يُرَدُّ"},
    {"juz": 26, "startSurah": 46, "startAyah": 1, "endSurah": 51, "endAyah": 30, "nameArabic": "حم"},
    {"juz": 27, "startSurah": 51, "startAyah": 31, "endSurah": 57, "endAyah": 29, "nameArabic": "قَالَ فَمَا خَطْبُكُمْ"},
    {"juz": 28, "startSurah": 58, "startAyah": 1, "endSurah": 66, "endAyah": 12, "nameArabic": "قَدْ سَمِعَ اللَّهُ"},
    {"juz": 29, "startSurah": 67, "startAyah": 1, "endSurah": 77, "endAyah": 50, "nameArabic": "تَبَارَكَ الَّذِي"},
    {"juz": 30, "startSurah": 78, "startAyah": 1, "endSurah": 114, "endAyah": 6, "nameArabic": "عَمَّ"}
]


def fetch_url(url, retries=3, timeout=90):
    for attempt in range(retries):
        try:
            print(f"Fetching: {url} (Attempt {attempt+1}/{retries})...")
            req = urllib.request.Request(url, headers={"User-Agent": "NoorQuranDataBuilder/1.0"})
            with urllib.request.urlopen(req, timeout=timeout) as resp:
                return json.loads(resp.read().decode("utf-8"))
        except Exception as e:
            print(f"Error fetching {url}: {e}")
            if attempt == retries - 1:
                raise
            time.sleep(2)


def main():
    print("=== Step 1: Downloading Verified Quran Datasets ===")
    
    # 1. Download Uthmani Arabic text
    uthmani_data = fetch_url("https://api.alquran.cloud/v1/quran/quran-uthmani")
    uthmani_surahs = uthmani_data["data"]["surahs"]
    
    # 2. Download Saheeh International English translation
    english_data = fetch_url("https://api.alquran.cloud/v1/quran/en.sahih")
    english_surahs = english_data["data"]["surahs"]
    
    # 3. Download Muhiuddin Khan Bengali translation
    bengali_data = fetch_url("https://api.alquran.cloud/v1/quran/bn.bengali")
    bengali_surahs = bengali_data["data"]["surahs"]
    
    print("\n=== Step 2: Validating Raw Datasets ===")
    assert len(uthmani_surahs) == 114, f"Uthmani surah count is {len(uthmani_surahs)}, expected 114"
    assert len(english_surahs) == 114, f"English surah count is {len(english_surahs)}, expected 114"
    assert len(bengali_surahs) == 114, f"Bengali surah count is {len(bengali_surahs)}, expected 114"
    
    surahs_list = []
    ayahs_list = []
    
    total_ayahs_count = 0
    
    for s_idx in range(114):
        s_num = s_idx + 1
        u_s = uthmani_surahs[s_idx]
        e_s = english_surahs[s_idx]
        b_s = bengali_surahs[s_idx]
        
        # Verify surah numbers match
        assert u_s["number"] == s_num, f"Uthmani Surah number mismatch: {u_s['number']} vs {s_num}"
        assert e_s["number"] == s_num, f"English Surah number mismatch: {e_s['number']} vs {s_num}"
        assert b_s["number"] == s_num, f"Bengali Surah number mismatch: {b_s['number']} vs {s_num}"
        
        expected_count = EXPECTED_AYAH_COUNTS[s_idx]
        u_ayahs = u_s["ayahs"]
        e_ayahs = e_s["ayahs"]
        b_ayahs = b_s["ayahs"]
        
        assert len(u_ayahs) == expected_count, f"Surah {s_num} has {len(u_ayahs)} ayahs, expected {expected_count}"
        assert len(e_ayahs) == expected_count, f"Surah {s_num} English has {len(e_ayahs)} ayahs, expected {expected_count}"
        assert len(b_ayahs) == expected_count, f"Surah {s_num} Bengali has {len(b_ayahs)} ayahs, expected {expected_count}"
        
        bn_name, bn_meaning = BENGALI_SURAH_NAMES[s_idx]
        
        surah_obj = {
            "number": s_num,
            "nameArabic": u_s["name"],
            "nameEnglish": u_s["englishName"],
            "englishMeaning": u_s["englishNameTranslation"],
            "nameBengali": bn_name,
            "bengaliMeaning": bn_meaning,
            "numberOfAyahs": expected_count,
            "revelationType": u_s["revelationType"]
        }
        surahs_list.append(surah_obj)
        
        for a_idx in range(expected_count):
            a_num = a_idx + 1
            u_a = u_ayahs[a_idx]
            e_a = e_ayahs[a_idx]
            b_a = b_ayahs[a_idx]
            
            assert u_a["numberInSurah"] == a_num, f"Ayah ordering mismatch in Surah {s_num}: {u_a['numberInSurah']} vs {a_num}"
            assert e_a["numberInSurah"] == a_num
            assert b_a["numberInSurah"] == a_num
            
            # Clean BOM if present in Surah 1 Ayah 1
            ar_text = u_a["text"].strip("\ufeff")
            en_text = e_a["text"].strip()
            bn_text = b_a["text"].strip()
            
            assert len(ar_text) > 0, f"Empty Arabic text at Surah {s_num} Ayah {a_num}"
            assert len(en_text) > 0, f"Empty English translation at Surah {s_num} Ayah {a_num}"
            assert len(bn_text) > 0, f"Empty Bengali translation at Surah {s_num} Ayah {a_num}"
            
            total_ayahs_count += 1
            
            ayah_obj = {
                "id": total_ayahs_count,
                "surahNumber": s_num,
                "ayahNumber": a_num,
                "arabicText": ar_text,
                "translationEn": en_text,
                "translationBn": bn_text,
                "juz": u_a["juz"],
                "page": u_a["page"],
                "manzil": u_a.get("manzil", 1),
                "ruku": u_a.get("ruku", 1),
                "hizbQuarter": u_a.get("hizbQuarter", 1),
                "sajda": bool(u_a.get("sajda", False))
            }
            ayahs_list.append(ayah_obj)
            
    assert total_ayahs_count == 6236, f"Total ayahs count is {total_ayahs_count}, expected 6236"
    print(f"Validation Passed: 114 Surahs and {total_ayahs_count} Ayahs fully verified!")
    
    # Checksum computation
    hasher = hashlib.sha256()
    for a in ayahs_list:
        hasher.update(f"{a['surahNumber']}:{a['ayahNumber']}:{a['arabicText']}".encode("utf-8"))
    dataset_checksum = hasher.hexdigest()
    print(f"Quran Uthmani Checksum (SHA-256): {dataset_checksum}")
    
    # Save JSON dataset
    dataset_payload = {
        "metadata": {
            "version": "1.0.0",
            "source": "AlQuran Cloud / Tanzil Uthmani Verified Dataset",
            "timestamp": int(time.time()),
            "totalSurahs": 114,
            "totalAyahs": 6236,
            "sha256Checksum": dataset_checksum
        },
        "surahs": surahs_list,
        "juzs": JUZ_METADATA,
        "ayahs": ayahs_list
    }
    
    assets_dir = "app/src/main/assets/quran"
    os.makedirs(assets_dir, exist_ok=True)
    
    json_path = os.path.join(assets_dir, "quran_data.json")
    print(f"Writing JSON dataset to {json_path}...")
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(dataset_payload, f, ensure_ascii=False, indent=None)
    
    json_size_mb = os.path.getsize(json_path) / (1024 * 1024)
    print(f"JSON dataset written ({json_size_mb:.2f} MB)")
    
    # Build SQLite Database
    db_path = os.path.join(assets_dir, "quran.db")
    if os.path.exists(db_path):
        os.remove(db_path)
    
    print(f"Building SQLite database at {db_path}...")
    conn = sqlite3.connect(db_path)
    cur = conn.cursor()
    
    cur.execute("""
    CREATE TABLE surahs (
        number INTEGER PRIMARY KEY,
        nameArabic TEXT NOT NULL,
        nameEnglish TEXT NOT NULL,
        englishMeaning TEXT NOT NULL,
        nameBengali TEXT NOT NULL,
        bengaliMeaning TEXT NOT NULL,
        numberOfAyahs INTEGER NOT NULL,
        revelationType TEXT NOT NULL
    );
    """)
    
    cur.execute("""
    CREATE TABLE ayahs (
        id INTEGER PRIMARY KEY,
        surahNumber INTEGER NOT NULL,
        ayahNumber INTEGER NOT NULL,
        arabicText TEXT NOT NULL,
        translationEn TEXT NOT NULL,
        translationBn TEXT NOT NULL,
        juz INTEGER NOT NULL,
        page INTEGER NOT NULL,
        manzil INTEGER NOT NULL,
        ruku INTEGER NOT NULL,
        hizbQuarter INTEGER NOT NULL,
        sajda INTEGER NOT NULL
    );
    """)
    
    cur.execute("CREATE INDEX idx_ayahs_surah ON ayahs(surahNumber);")
    cur.execute("CREATE INDEX idx_ayahs_juz ON ayahs(juz);")
    cur.execute("CREATE INDEX idx_ayahs_page ON ayahs(page);")
    
    cur.execute("""
    CREATE TABLE bookmarks (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        surahNumber INTEGER NOT NULL,
        ayahNumber INTEGER NOT NULL,
        timestamp INTEGER NOT NULL,
        note TEXT
    );
    """)
    
    cur.execute("""
    CREATE TABLE last_read (
        id INTEGER PRIMARY KEY,
        surahNumber INTEGER NOT NULL,
        ayahNumber INTEGER NOT NULL,
        timestamp INTEGER NOT NULL
    );
    """)
    
    for s in surahs_list:
        cur.execute(
            "INSERT INTO surahs VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            (s["number"], s["nameArabic"], s["nameEnglish"], s["englishMeaning"],
             s["nameBengali"], s["bengaliMeaning"], s["numberOfAyahs"], s["revelationType"])
        )
        
    for a in ayahs_list:
        cur.execute(
            "INSERT INTO ayahs VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            (a["id"], a["surahNumber"], a["ayahNumber"], a["arabicText"],
             a["translationEn"], a["translationBn"], a["juz"], a["page"],
             a["manzil"], a["ruku"], a["hizbQuarter"], 1 if a["sajda"] else 0)
        )
        
    # Default last_read: Surah 1 Ayah 1
    cur.execute("INSERT INTO last_read VALUES (1, 1, 1, ?)", (int(time.time()),))
    
    conn.commit()
    
    # Check integrity
    cur.execute("PRAGMA integrity_check;")
    integrity = cur.fetchone()
    assert integrity[0] == "ok", f"SQLite integrity check failed: {integrity}"
    
    cur.execute("SELECT count(*) FROM surahs;")
    assert cur.fetchone()[0] == 114
    
    cur.execute("SELECT count(*) FROM ayahs;")
    assert cur.fetchone()[0] == 6236
    
    conn.close()
    
    db_size_mb = os.path.getsize(db_path) / (1024 * 1024)
    print(f"SQLite database built successfully ({db_size_mb:.2f} MB)")
    print("\n=== All Quran Data Successfully Prepared & Validated ===")


if __name__ == "__main__":
    main()
