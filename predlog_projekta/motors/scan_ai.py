import anthropic
import base64
import csv
import json
import os

INPUT_DIR = "images"
OUTPUT_DIR = "tables"

PROMPT = """This image is a motor datasheet. It may contain one or multiple tables.
Extract all tables as a JSON array. Each table is an object with "headers" and "rows".

Rules:
- If a cell is merged across multiple rows, repeat its value in each row it spans
- If a cell is merged across multiple columns, include the value under each column it spans
- If there are multiple separate tables in the image, return all of them as separate objects in the array
- Use null for empty cells
- Return only valid JSON, no explanation, no markdown fences

Format: [{"headers": [...], "rows": [{...}, ...]}, ...]"""

client = anthropic.Anthropic(api_key="API_KEY")

image_files = [f for f in os.listdir(INPUT_DIR) if f.lower().endswith((".jpg", ".jpeg", ".png"))]

def has_output(stem):
    if os.path.exists(os.path.join(OUTPUT_DIR, stem + ".xlsx")):
        return True
    if os.path.exists(os.path.join(OUTPUT_DIR, stem + ".csv")):
        return True
    if os.path.exists(os.path.join(OUTPUT_DIR, stem + "_table1.csv")):
        return True
    return False

failed = [f for f in image_files if not has_output(os.path.splitext(f)[0])]

print(f"Found {len(failed)} images without tables\n")

for filename in sorted(failed):
    input_path = os.path.join(INPUT_DIR, filename)
    out_stem = os.path.join(OUTPUT_DIR, os.path.splitext(filename)[0])

    print(f"Processing: {filename} ...", end=" ", flush=True)

    with open(input_path, "rb") as f:
        image_data = base64.standard_b64encode(f.read()).decode("utf-8")

    media_type = "image/png" if filename.lower().endswith(".png") else "image/jpeg"

    try:
        response = client.messages.create(
            model="claude-opus-4-5",
            max_tokens=4096,
            messages=[
                {
                    "role": "user",
                    "content": [
                        {"type": "image", "source": {"type": "base64", "media_type": media_type, "data": image_data}},
                        {"type": "text", "text": PROMPT},
                    ],
                }
            ],
        )

        raw = response.content[0].text.strip()
        if raw.startswith("```"):
            raw = raw.split("\n", 1)[1].rsplit("```", 1)[0].strip()

        tables = json.loads(raw)

        if not tables:
            print("no tables found")
            continue

        if len(tables) == 1:
            out_path = out_stem + ".csv"
            with open(out_path, "w", newline="", encoding="utf-8") as f:
                writer = csv.DictWriter(f, fieldnames=tables[0]["headers"], extrasaction="ignore")
                writer.writeheader()
                writer.writerows(tables[0]["rows"])
            print(f"OK -> {os.path.basename(out_path)}")
        else:
            for idx, table in enumerate(tables, 1):
                out_path = f"{out_stem}_table{idx}.csv"
                with open(out_path, "w", newline="", encoding="utf-8") as f:
                    writer = csv.DictWriter(f, fieldnames=table["headers"], extrasaction="ignore")
                    writer.writeheader()
                    writer.writerows(table["rows"])
            print(f"OK -> {len(tables)} tables")

    except json.JSONDecodeError as e:
        print(f"FAIL (bad JSON): {e}")
        with open(out_stem + "_raw.txt", "w", encoding="utf-8") as f:
            f.write(raw)
    except Exception as e:
        print(f"FAIL: {e}")