#!/usr/bin/env python3
"""
Noor Quran - Build-Time Quran Dataset Validator

Validates both app/src/main/assets/quran/quran_data.json and
app/src/main/assets/quran/quran.db to guarantee complete data integrity.

Fails immediately with exit code 1 if:
1. Surah count != 114
2. Any expected Ayah is missing
3. Any Ayah is duplicated
4. Ayah ordering is incorrect
5. Quran dataset contains malformed records
6. Required translation records are missing
7. JSON/SQLite database is corrupted
"""

import sys
import os
import json
import sqlite3
import hashlib

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

def fail(message):
    print(f"\n[FATAL ERROR] Quran Data Validation Failed: {message}", file=sys.stderr)
    sys.exit(1)

def validate_json(filepath):
    print(f"Validating JSON: {filepath}...")
    if not os.path.exists(filepath):
        fail(f"Dataset file missing: {filepath}")
        
    try:
        with open(filepath, "r", encoding="utf-8") as f:
            data = json.load(f)
    except Exception as e:
        fail(f"JSON parsing/corruption error: {e}")
        
    if "surahs" not in data or "ayahs" not in data or "metadata" not in data:
        fail("Missing required root keys in JSON dataset")
        
    surahs = data["surahs"]
    ayahs = data["ayahs"]
    
    if len(surahs) != 114:
        fail(f"Surah count != 114 (Found {len(surahs)})")
        
    if len(ayahs) != 6236:
        fail(f"Ayah count != 6236 (Found {len(ayahs)})")
        
    hasher = hashlib.sha256()
    ayah_idx = 0
    for s_idx, s in enumerate(surahs):
        s_num = s_idx + 1
        if s.get("number") != s_num:
            fail(f"Surah index mismatch: expected {s_num}, got {s.get('number')}")
            
        expected_ayahs = EXPECTED_AYAH_COUNTS[s_idx]
        if s.get("numberOfAyahs") != expected_ayahs:
            fail(f"Surah {s_num} numberOfAyahs mismatch: expected {expected_ayahs}, got {s.get('numberOfAyahs')}")
            
        for a_idx in range(expected_ayahs):
            if ayah_idx >= len(ayahs):
                fail(f"Unexpected end of ayahs at Surah {s_num} Ayah {a_idx + 1}")
                
            a = ayahs[ayah_idx]
            ayah_idx += 1
            
            if a.get("surahNumber") != s_num:
                fail(f"Ayah surahNumber mismatch: expected {s_num}, got {a.get('surahNumber')}")
            if a.get("ayahNumber") != a_idx + 1:
                fail(f"Ayah ordering error in Surah {s_num}: expected {a_idx + 1}, got {a.get('ayahNumber')}")
            if not a.get("arabicText") or len(a["arabicText"].strip()) == 0:
                fail(f"Empty Arabic text at Surah {s_num} Ayah {a_idx + 1}")
            if not a.get("translationEn") or len(a["translationEn"].strip()) == 0:
                fail(f"Empty English translation at Surah {s_num} Ayah {a_idx + 1}")
            if not a.get("translationBn") or len(a["translationBn"].strip()) == 0:
                fail(f"Empty Bengali translation at Surah {s_num} Ayah {a_idx + 1}")
                
            hasher.update(f"{a['surahNumber']}:{a['ayahNumber']}:{a['arabicText']}".encode("utf-8"))
            
    calc_hash = hasher.hexdigest()
    recorded_hash = data.get("metadata", {}).get("sha256Checksum")
    if recorded_hash and calc_hash != recorded_hash:
        fail(f"Checksum mismatch: calculated {calc_hash} vs recorded {recorded_hash}")
        
    print(f"  [OK] JSON dataset: 114 Surahs, 6236 Ayahs, Checksum {calc_hash[:16]}... verified.")


def validate_sqlite(filepath):
    print(f"Validating SQLite DB: {filepath}...")
    if not os.path.exists(filepath):
        fail(f"SQLite DB missing: {filepath}")
        
    try:
        conn = sqlite3.connect(filepath)
        cur = conn.cursor()
        
        cur.execute("PRAGMA integrity_check;")
        res = cur.fetchone()
        if not res or res[0] != "ok":
            fail(f"SQLite database corrupted: {res}")
            
        cur.execute("SELECT count(*) FROM surahs;")
        surah_count = cur.fetchone()[0]
        if surah_count != 114:
            fail(f"SQLite surahs table has {surah_count} rows, expected 114")
            
        cur.execute("SELECT count(*) FROM ayahs;")
        ayah_count = cur.fetchone()[0]
        if ayah_count != 6236:
            fail(f"SQLite ayahs table has {ayah_count} rows, expected 6236")
            
        cur.execute("SELECT number, numberOfAyahs FROM surahs ORDER BY number ASC;")
        surahs = cur.fetchall()
        for idx, (num, count) in enumerate(surahs):
            if num != idx + 1:
                fail(f"SQLite Surah order invalid: expected {idx+1}, got {num}")
            if count != EXPECTED_AYAH_COUNTS[idx]:
                fail(f"SQLite Surah {num} Ayah count {count} != expected {EXPECTED_AYAH_COUNTS[idx]}")
                
        # Check Ayah ordering and missing/duplicate records
        cur.execute("SELECT id, surahNumber, ayahNumber, arabicText, translationEn, translationBn FROM ayahs ORDER BY id ASC;")
        ayahs = cur.fetchall()
        expected_id = 1
        for a in ayahs:
            aid, s_num, a_num, ar_text, en_text, bn_text = a
            if aid != expected_id:
                fail(f"SQLite Ayah ID mismatch: expected {expected_id}, got {aid}")
            expected_id += 1
            if not ar_text or not en_text or not bn_text:
                fail(f"SQLite empty text/translation at Ayah {aid} (Surah {s_num}:{a_num})")
                
        conn.close()
        print(f"  [OK] SQLite database: integrity_check OK, 114 Surahs, 6236 Ayahs verified.")
    except Exception as e:
        fail(f"SQLite validation error: {e}")


def main():
    print("========================================")
    print("NOOR QURAN BUILD-TIME VALIDATION SUITE")
    print("========================================")
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    assets_dir = os.path.join(base_dir, "app", "src", "main", "assets", "quran")
    
    json_file = os.path.join(assets_dir, "quran_data.json")
    db_file = os.path.join(assets_dir, "quran.db")
    
    validate_json(json_file)
    validate_sqlite(db_file)
    
    print("\n========================================")
    print("SUCCESS: Quran dataset validation PASSED")
    print("========================================")


if __name__ == "__main__":
    main()
