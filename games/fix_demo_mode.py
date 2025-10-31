#!/usr/bin/env python3
import re

files = [
    '/Users/archilodishelidze/Desktop/dev/gambling/games/slots/simple5-reel-video-slots/index.html',
    '/Users/archilodishelidze/Desktop/dev/gambling/games/slots/book-style-slots/index.html',
    '/Users/archilodishelidze/Desktop/dev/gambling/games/slots/hold-and-win/index.html',
    '/Users/archilodishelidze/Desktop/dev/gambling/games/table-games/blackjack/index.html',
    '/Users/archilodishelidze/Desktop/dev/gambling/games/poker/jack-or-better/index.html',
    '/Users/archilodishelidze/Desktop/dev/gambling/games/poker/three-card-poker/index.html',
    '/Users/archilodishelidze/Desktop/dev/gambling/games/poker/caribbean-stud-poker/index.html'
]

old_pattern = r'''(\s+)if \(!sessionToken\) \{
(\s+)console\.error\('NO SESSION TOKEN!'\);
(\s+)statusEl\.innerHTML = '<div class="error">No session token found</div>';
(\s+)return;
(\s+)\}

(\s+)if \(isDemo\) \{'''

new_code = r'''\1if (isDemo) {
\1    console.log('Starting in DEMO mode');
\1    // Demo mode - no backend calls
\1    balance = 100;
\1    updateBalance();
\1    showGame();
\1    return;
\1}

\1if (!sessionToken) {
\1    console.error('NO SESSION TOKEN!');
\1    statusEl.innerHTML = '<div class="error">No session token found</div>';
\1    return;
\1}

\1// Real money mode
\1{'''

for filepath in files:
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()

        # Replace the pattern
        new_content = re.sub(old_pattern, new_code, content, flags=re.MULTILINE)

        if new_content != content:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(new_content)
            print(f"✓ Fixed: {filepath}")
        else:
            print(f"- No change needed: {filepath}")
    except Exception as e:
        print(f"✗ Error processing {filepath}: {e}")

print("\nDone!")
