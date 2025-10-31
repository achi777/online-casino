#!/usr/bin/env python3
import re

files = [
    '/Users/archilodishelidze/Desktop/dev/gambling/games/slots/simple5-reel-video-slots/index.html',
    '/Users/archilodishelidze/Desktop/dev/gambling/games/slots/book-style-slots/index.html',
    '/Users/archilodishelidze/Desktop/dev/gambling/games/slots/hold-and-win/index.html',
    '/Users/archilodishelidze/Desktop/dev/gambling/games/table-games/blackjack/index.html',
    '/Users/archilodishelidze/Desktop/dev/gambling/games/poker/jack-or-better/index.html',
    '/Users/archilodishelidze/Desktop/dev/gambling/games/poker/three-card-poker/index.html'
]

for filepath in files:
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            lines = f.readlines()

        # Find and remove duplicate demo mode code (around line 119-125)
        new_lines = []
        skip_until = -1
        for i, line in enumerate(lines):
            if skip_until > i:
                continue

            # Check for the problematic pattern: "// Real money mode" followed by "{ console.log('Starting in DEMO mode')"
            if i < len(lines) - 2 and '// Real money mode' in line:
                if i + 2 < len(lines) and 'console.log(\'Starting in DEMO mode\')' in lines[i+2]:
                    # Skip this duplicate demo block and find the real "} else {"
                    new_lines.append('            // Real money mode\n')
                    j = i + 1
                    while j < len(lines) and '} else {' not in lines[j]:
                        j += 1
                    if j < len(lines):
                        skip_until = j
                    continue

            new_lines.append(line)

        # Remove excessive empty lines
        cleaned = []
        prev_empty = False
        for line in new_lines:
            is_empty = line.strip() == ''
            if is_empty and prev_empty:
                continue
            cleaned.append(line)
            prev_empty = is_empty

        with open(filepath, 'w', encoding='utf-8') as f:
            f.writelines(cleaned)

        print(f"✓ Cleaned: {filepath}")
    except Exception as e:
        print(f"✗ Error: {filepath}: {e}")

print("\nDone!")
