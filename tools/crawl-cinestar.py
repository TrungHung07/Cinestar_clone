# -*- coding: utf-8 -*-
"""
Crawl du lieu + anh tu cinestar.com.vn (muc dich hoc tap/demo):
- Phim: /movie/ (NEXT_DATA -> res.listMovie)
- Rap:  listCinemas cung trang
- Khuyen mai: parse anh banner tu /chuong-trinh-khuyen-mai/
Xuat:
- javabackend/src/main/resources/data/{movies,cinemas,promotions}.json
- javabackend/src/main/resources/static/images/{posters,cinemas,promotions}/
"""
import json, re, os, time, urllib.request, unicodedata, ssl

TEMP = os.environ.get('TEMP', '/tmp')
OUT_DATA = os.path.join('javabackend', 'src', 'main', 'resources', 'data')
OUT_IMG = os.path.join('javabackend', 'src', 'main', 'resources', 'static', 'images')
UA = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120'}
CTX = ssl.create_default_context()
CTX.check_hostname = False
CTX.verify_mode = ssl.CERT_NONE  # mot so CDN cert chain loi tren Windows

os.makedirs(os.path.join(OUT_DATA), exist_ok=True)
for sub in ('posters', 'banners', 'cinemas', 'promotions'):
    os.makedirs(os.path.join(OUT_IMG, sub), exist_ok=True)


def fetch(url, timeout=30):
    req = urllib.request.Request(url, headers=UA)
    with urllib.request.urlopen(req, timeout=timeout, context=CTX) as r:
        return r.read()


def next_data(url):
    html = fetch(url).decode('utf-8', 'ignore')
    m = re.search(r'<script id="__NEXT_DATA__" type="application/json">(.*?)</script>', html, re.S)
    return json.loads(m.group(1))['props']['pageProps'] if m else None


def no_accent(s):
    s = unicodedata.normalize('NFD', s)
    s = ''.join(c for c in s if unicodedata.category(c) != 'Mn')
    return s.replace('đ', 'd').replace('Đ', 'D')


def slugify(s):
    s = no_accent(s.lower())
    s = re.sub(r'[^a-z0-9]+', '-', s).strip('-')
    return s[:80] or 'phim'


def download(url, dest):
    if not url:
        return None
    try:
        data = fetch(url, timeout=60)
        ext = os.path.splitext(url.split('?')[0])[1].lower() or '.jpg'
        if ext not in ('.jpg', '.jpeg', '.png', '.webp'):
            ext = '.jpg'
        dest = os.path.splitext(dest)[0] + ext
        with open(dest, 'wb') as f:
            f.write(data)
        return os.path.basename(dest)
    except Exception as e:
        print('   [WARN] tai anh loi:', url[:80], '->', e)
        return None


def dl(url, folder, name):
    fname = download(url, os.path.join(OUT_IMG, folder, name))
    return f'/images/{folder}/{fname}' if fname else None


# ============ 1. PHIM ============
print('== Crawl phim tu /movie/ ==')
pp = next_data('https://cinestar.com.vn/movie/')
movies = pp['res']['listMovie']
result = []
for i, mv in enumerate(movies):
    title_raw = (mv.get('name_vn') or '').strip()
    # Bo suffix nhan tuoi dinh trong ten: "(K)", "(T13)", "LT (K)", "PD (T16)"...
    title = re.sub(r'\s*\((?:K|P|T13|T16|T18|CHUA PHAN LOAI)\)\s*$', '', title_raw, flags=re.I).strip()
    title = re.sub(r'\s*\b(?:LT|PD|LT VD)\b\s*$', '', title, flags=re.I).strip()
    title = re.sub(r'\s*\((?:K|P|T13|T16|T18)\)\s*$', '', title, flags=re.I).strip() or title_raw
    slug = slugify(title)

    age = (mv.get('limitage_vn') or '').strip()
    age = None if age.lower().startswith('chưa') else age

    lang = (mv.get('language_vn') or '').strip()
    subtitle = 'LONG_TIENG' if 'lồng' in lang.lower() else 'PHU_DE'

    duration = None
    try:
        duration = int(str(mv.get('time')).strip())
    except Exception:
        duration = 100

    release = None
    rd = mv.get('release_date')
    if rd:
        mm = re.match(r'(\d{1,2})/(\d{1,2})/(\d{4})', rd)
        if mm:
            release = f'{mm.group(3)}-{int(mm.group(1)):02d}-{int(mm.group(2)):02d}'

    rating = None
    try:
        r = float(str(mv.get('ratings')).strip())
        if 0 < r <= 10: rating = r
    except Exception:
        pass

    trailer = mv.get('trailer') or ''
    trailer_url = ('https://youtu.be/' + trailer.strip()) if trailer and 'http' not in trailer else (trailer or None)

    poster = dl(mv.get('image'), 'posters', slug)
    backdrop = dl(mv.get('himage'), 'banners', slug + '-bd')

    item = {
        'title': title,
        'slug': slug,
        'description': (mv.get('desc_vn') or mv.get('brief_vn') or '').strip() or None,
        'posterUrl': poster,
        'backdropUrl': backdrop,
        'trailerUrl': trailer_url,
        'duration': duration,
        'country': (mv.get('country_name_vn') or '').strip() or None,
        'releaseDate': release,
        'status': 'NOW_SHOWING' if str(mv.get('status')) == '1' else 'UPCOMING',
        'ageRating': age,
        'format': (mv.get('formats_name_vn') or '2D').strip(),
        'subtitleType': subtitle,
        'director': (mv.get('director') or '').strip() or None,
        'actors': (mv.get('actor') or '').strip() or None,
        'rating': rating,
        'isFeatured': i < 5 and str(mv.get('status')) == '1',
        'genres': [g.strip() for g in (mv.get('type_name_vn') or '').split(',') if g.strip()],
    }
    result.append(item)
    print(f"  [{i+1}/{len(movies)}] {item['status']:11s} {title[:45]:45s} poster={'OK' if poster else 'FAIL'}")
    time.sleep(0.2)

with open(os.path.join(OUT_DATA, 'movies.json'), 'w', encoding='utf-8') as f:
    json.dump(result, f, ensure_ascii=False, indent=1)
print(f'=> movies.json: {len(result)} phim')

# ============ 2. RAP ============
print('== Crawl rap ==')
cinemas = pp['res']['listCinemas']
cin_result = []
for i, c in enumerate(cinemas):
    name = (c.get('name_vn') or '').strip()
    slug = slugify(name)
    img = dl(c.get('image'), 'cinemas', slug)
    cin_result.append({
        'name': name,
        'slug': slug,
        'address': (c.get('address') or '').strip(),
        'city': (c.get('area_name_vn') or '').strip(),
        'hotline': (c.get('telephone') or '').strip().replace('.',''),
        'imageUrl': img,
        'active': True,
    })
    print(f"  [{i+1}/{len(cinemas)}] {name[:40]:40s} img={'OK' if img else 'FAIL'}")
with open(os.path.join(OUT_DATA, 'cinemas.json'), 'w', encoding='utf-8') as f:
    json.dump(cin_result, f, ensure_ascii=False, indent=1)
print(f'=> cinemas.json: {len(cin_result)} rap')

# ============ 3. KHUYEN MAI ============
print('== Crawl khuyen mai ==')
ppromo = next_data('https://cinestar.com.vn/chuong-trinh-khuyen-mai/')
promos = []
if ppromo:
    htmlC = (ppromo.get('res', {}).get('content_vn', {}) or {}).get('htmlContent', '')
    # Moi block khuyen mai: the <img ...> dung truoc the <h3>/<strong>/<p> chua tieu de
    blocks = re.findall(r'<img[^>]+src="([^"]+)"[^>]*>', htmlC)
    seen = set()
    idx = 0
    for src in blocks:
        if 'Promotions' not in src and 'Member' not in src and 'Slider' not in src:
            continue
        if src in seen:
            continue
        seen.add(src)
        idx += 1
        # tim alt
        m_alt = re.search(r'<img[^>]+src="' + re.escape(src) + r'"[^>]*alt="([^"]*)"', htmlC)
        title = (m_alt.group(1) if m_alt and m_alt.group(1).strip() else '') or f'Khuyến mãi Cinestar {idx}'
        img = dl(src, 'promotions', 'promo-' + str(idx))
        if not img:
            continue
        promos.append({
            'title': title[:140],
            'description': 'Ưu đãi độc quyền từ Cinestar — xem chi tiết tại website cinestar.com.vn',
            'imageUrl': img,
            'startDate': None,
            'endDate': None,
            'active': True,
        })
        print(f"  [{len(promos)}] {title[:50]}")
with open(os.path.join(OUT_DATA, 'promotions.json'), 'w', encoding='utf-8') as f:
    json.dump(promos, f, ensure_ascii=False, indent=1)
print(f'=> promotions.json: {len(promos)} khuyen mai')

# Tong dung luong anh
total = 0
for root, _, files in os.walk(OUT_IMG):
    for f in files:
        total += os.path.getsize(os.path.join(root, f))
print(f'\nTong dung luong anh: {total/1024/1024:.1f} MB')
