import os
from img2table.ocr import EasyOCR
from img2table.document import Image

INPUT_DIR = "images"
OUTPUT_DIR = "tables"

os.makedirs(OUTPUT_DIR, exist_ok=True)

ocr = EasyOCR(lang=["en"])

image_files = [f for f in os.listdir(INPUT_DIR) if f.lower().endswith((".jpg", ".jpeg", ".png"))]

for filename in sorted(image_files):
    input_path = os.path.join(INPUT_DIR, filename)
    output_path = os.path.join(OUTPUT_DIR, os.path.splitext(filename)[0] + ".xlsx")

    if os.path.exists(output_path):
        print(f"SKIP: {filename}")
        continue

    print(f"Processing: {filename} ...", end=" ", flush=True)

    try:
        img = Image(input_path)
        result = img.extract_tables(ocr=ocr, implicit_rows=True, borderless_tables=False)

        if not result:
            print("no tables found")
            continue

        img.to_xlsx(output_path, ocr=ocr, implicit_rows=True, borderless_tables=False)
        print(f"OK -> {os.path.basename(output_path)}")

    except Exception as e:
        print(f"FAIL: {e}")