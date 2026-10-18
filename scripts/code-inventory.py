#!/usr/bin/env python3
"""Read-only source-size/density and exact normalized-window inventory."""
from collections import defaultdict
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
EXCLUDED = {"build", ".git", ".gradle", ".idea", ".kotlin", "run"}
sources = []
windows = defaultdict(list)
for path in sorted(ROOT.rglob("*")):
    relative = path.relative_to(ROOT)
    if any(part in EXCLUDED for part in relative.parts) or path.suffix not in {".kt", ".java", ".kts"} or not path.is_file():
        continue
    lines = path.read_text().splitlines()
    code = [(index, line) for index, line in enumerate(lines, 1)
            if line.strip() and not line.lstrip().startswith(("//", "/*", "*", "import ", "package "))]
    dense = [(index, len(line), line.count(";")) for index, line in code
             if len(line) >= 180 or line.count(";") >= 2]
    sources.append((str(relative), len(lines), max(map(len, lines), default=0), dense))
    if path.suffix in {".kt", ".java"}:
        normalized = [(index, re.sub(r"\s+", " ", line.strip())) for index, line in code]
        for start in range(len(normalized) - 7):
            window = tuple(text for _, text in normalized[start:start + 8])
            if sum(map(len, window)) >= 180:
                windows[window].append((str(relative), normalized[start][0]))

print("LONG FILES (>=400 physical lines)")
for path, count, maximum, dense in sorted(sources, key=lambda row: (-row[1], row[0])):
    if count >= 400:
        print(f"{count:4} | max {maximum:4} | dense {len(dense):3} | {path}")
print("\nDENSE FILES (>=180-character non-comment line or >=2 semicolons)")
for path, count, maximum, dense in sorted(sources, key=lambda row: (-len(row[3]), -row[2], row[0])):
    if dense:
        print(f"{len(dense):3} | max {maximum:4} | {path} | lines " + ", ".join(str(row[0]) for row in dense))
print("\nEXACT 8-CODE-LINE DUPLICATION CANDIDATES (top 35 non-overlapping groups)")
used = set()
groups = sorted((locations for locations in windows.values() if len({p for p, _ in locations}) > 1),
                key=lambda locations: (-len({p for p, _ in locations}), locations))
shown = 0
for locations in groups:
    if any((path, line // 12) in used for path, line in locations):
        continue
    print(" | ".join(f"{path}:{line}" for path, line in locations))
    used.update((path, line // 12) for path, line in locations)
    shown += 1
    if shown >= 35:
        break
print(f"\nScanned {len(sources)} source/build files.")
