#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Mianboor Panel Tools
====================
ابزار مدیریت کدهای اشتراک تاییدشده و کانفیگ‌های اپلیکیشن Mianboor

استفاده:
  python3 tools.py add-code    --days 30 --plan "اشتراک یک‌ماهه" --note "علی" [--code 1234567890]
  python3 tools.py remove-code --code 1234567890
  python3 tools.py list-codes
  python3 tools.py add-config  --name "🇩🇪 آلمان — پرسرعت" --uri "vless://..."
  python3 tools.py remove-config --index 1
  python3 tools.py list-configs
  python3 tools.py encode      --uri "vless://..."     (فقط رمزنگاری و چاپ)

نکته امنیتی: کد اشتراک به‌صورت هش SHA-256 ذخیره می‌شود؛ پس کد خام هرگز در ریپو قرار نمی‌گیرد.
اگر SECRET_XOR_KEY را در اپ (AppConfig.kt) تغییر دادید، اینجا هم با پارامتر --key همان مقدار را بدهید.
"""

import argparse
import base64
import hashlib
import json
import os
import random
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
CODES_FILE = os.path.join(HERE, "codes.json")
CONFIGS_FILE = os.path.join(HERE, "configs.json")

DEFAULT_XOR_KEY = "Mianboor@2026#Ne0nGl4ss"  # باید با AppConfig.SECRET_XOR_KEY یکسان باشد


def sha256_hex(text: str) -> str:
    return hashlib.sha256(text.encode("utf-8")).hexdigest()


def xor_encode(text: str, key: str) -> str:
    data = text.encode("utf-8")
    k = key.encode("utf-8")
    out = bytes(b ^ k[i % len(k)] for i, b in enumerate(data))
    return base64.b64encode(out).decode("ascii")


def load_json(path, default):
    if not os.path.exists(path):
        return default
    try:
        with open(path, "r", encoding="utf-8") as f:
            return json.load(f)
    except Exception as e:
        print(f"⚠️  خطا در خواندن {path}: {e}")
        return default


def save_json(path, data):
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")


# ---------------- کدها ----------------

def cmd_add_code(args):
    key = args.key or DEFAULT_XOR_KEY
    _ = key  # کد فقط هش می‌شود
    codes = load_json(CODES_FILE, {"version": 1, "codes": []})
    codes.setdefault("codes", [])

    code = args.code or str(random.randint(10 ** 9, 10 ** 10 - 1))
    h = sha256_hex(code)
    if any(c["hash"] == h for c in codes["codes"]):
        print("❌ این کد از قبل در پنل وجود دارد!")
        sys.exit(1)

    codes["codes"].append({
        "hash": h,
        "plan": args.plan,
        "days": args.days,
        "active": True,
        "note": args.note or "",
    })
    codes["updated"] = __import__("datetime").date.today().isoformat()
    save_json(CODES_FILE, codes)
    print("=" * 46)
    print("✅ کد اشتراک تایید و در پنل ذخیره شد")
    print(f"   کد خام (به مشتری بدهید): {code}")
    print(f"   پلن: {args.plan}  |  مدت: {args.days} روز")
    print("=" * 46)


def cmd_remove_code(args):
    codes = load_json(CODES_FILE, {"version": 1, "codes": []})
    h = sha256_hex(args.code)
    before = len(codes.get("codes", []))
    codes["codes"] = [c for c in codes.get("codes", []) if c["hash"] != h]
    save_json(CODES_FILE, codes)
    removed = before - len(codes["codes"])
    print(f"✅ {removed} کد حذف شد." if removed else "❌ کد یافت نشد.")


def cmd_toggle_code(args):
    codes = load_json(CODES_FILE, {"version": 1, "codes": []})
    h = sha256_hex(args.code)
    found = False
    for c in codes.get("codes", []):
        if c["hash"] == h:
            c["active"] = not c.get("active", True)
            found = True
            print(f"✅ کد {'فعال' if c['active'] else 'غیرفعال'} شد.")
            break
    if not found:
        print("❌ کد یافت نشد.")
    save_json(CODES_FILE, codes)


def cmd_list_codes(args):
    codes = load_json(CODES_FILE, {"version": 1, "codes": []})
    items = codes.get("codes", [])
    if not items:
        print("هنوز کدی ثبت نشده است.")
        return
    print(f"تعداد کل کدها: {len(items)}")
    for i, c in enumerate(items, 1):
        status = "✔ فعال" if c.get("active", True) else "✖ غیرفعال"
        print(f"  {i}. {c['hash'][:12]}…  |  {c['plan']}  |  {c['days']} روز  |  {status}  |  {c.get('note', '')}")


# ---------------- کانفیگ‌ها ----------------

def cmd_add_config(args):
    key = args.key or DEFAULT_XOR_KEY
    data = load_json(CONFIGS_FILE, {"version": 1, "configs": []})
    data.setdefault("configs", [])
    data["configs"].append({
        "name": args.name,
        "uri": xor_encode(args.uri, key),
    })
    data["updated"] = __import__("datetime").date.today().isoformat()
    save_json(CONFIGS_FILE, data)
    print(f"✅ کانفیگ «{args.name}» رمزنگاری و اضافه شد.")


def cmd_remove_config(args):
    data = load_json(CONFIGS_FILE, {"version": 1, "configs": []})
    items = data.get("configs", [])
    if 1 <= args.index <= len(items):
        removed = items.pop(args.index - 1)
        save_json(CONFIGS_FILE, data)
        print(f"✅ کانفیگ «{removed['name']}» حذف شد.")
    else:
        print("❌ شماره نامعتبر است.")


def cmd_list_configs(args):
    data = load_json(CONFIGS_FILE, {"version": 1, "configs": []})
    items = data.get("configs", [])
    if not items:
        print("هنوز کانفیگی ثبت نشده است.")
        return
    print(f"تعداد کل کانفیگ‌ها: {len(items)}")
    for i, c in enumerate(items, 1):
        print(f"  {i}. {c['name']}  ({c['uri'][:24]}…)")


def cmd_encode(args):
    key = args.key or DEFAULT_XOR_KEY
    print(xor_encode(args.uri, key))


def main():
    p = argparse.ArgumentParser(description="Mianboor Panel Tools", formatter_class=argparse.RawTextHelpFormatter)
    sub = p.add_subparsers(dest="cmd", required=True)

    sp = sub.add_parser("add-code", help="تولید/ثبت کد اشتراک تاییدشده")
    sp.add_argument("--days", type=int, required=True, help="مدت اعتبار به روز (مثلاً 30)")
    sp.add_argument("--plan", required=True, help="نام پلن (مثلاً «اشتراک یک‌ماهه»)")
    sp.add_argument("--note", default="", help="یادداشت (مثلاً نام مشتری)")
    sp.add_argument("--code", default=None, help="کد دلخواه ۱۰ رقمی (خالی = تولید خودکار)")
    sp.add_argument("--key", default=None, help="کلید XOR (اختیاری)")
    sp.set_defaults(func=cmd_add_code)

    sp = sub.add_parser("remove-code", help="حذف کد")
    sp.add_argument("--code", required=True)
    sp.set_defaults(func=cmd_remove_code)

    sp = sub.add_parser("toggle-code", help="فعال/غیرفعال کردن کد")
    sp.add_argument("--code", required=True)
    sp.set_defaults(func=cmd_toggle_code)

    sp = sub.add_parser("list-codes", help="لیست کدها")
    sp.set_defaults(func=cmd_list_codes)

    sp = sub.add_parser("add-config", help="افزودن کانفیگ (رمزنگاری‌شده)")
    sp.add_argument("--name", required=True, help="نام نمایشی (مثلاً «🇩🇪 آلمان — پرسرعت»)")
    sp.add_argument("--uri", required=True, help="کانفیگ خام (vless:// یا vmess:// یا ...)")
    sp.add_argument("--key", default=None, help="کلید XOR (اختیاری)")
    sp.set_defaults(func=cmd_add_config)

    sp = sub.add_parser("remove-config", help="حذف کانفیگ با شماره (از ۱)")
    sp.add_argument("--index", type=int, required=True)
    sp.set_defaults(func=cmd_remove_config)

    sp = sub.add_parser("list-configs", help="لیست کانفیگ‌ها")
    sp.set_defaults(func=cmd_list_configs)

    sp = sub.add_parser("encode", help="رمزنگاری یک uri و چاپ خروجی")
    sp.add_argument("--uri", required=True)
    sp.add_argument("--key", default=None, help="کلید XOR (اختیاری)")
    sp.set_defaults(func=cmd_encode)

    args = p.parse_args()
    args.func(args)


if __name__ == "__main__":
    main()
