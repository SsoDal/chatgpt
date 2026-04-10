import requests

# Yahoo Finance 요청 시 필수 헤더
HEADERS = {"User-Agent": "Mozilla/5.0"}


def get_chart_data(ticker):
    url = f"https://query1.finance.yahoo.com/v8/finance/chart/{ticker}?range=1mo&interval=1d"
    try:
        r = requests.get(url, headers=HEADERS, timeout=10)
        r.raise_for_status()
        result = r.json()["chart"]["result"][0]
        highs = result["indicators"]["quote"][0]["high"]
        lows  = result["indicators"]["quote"][0]["low"]
        # None 값 제거
        highs = [h for h in highs if h is not None]
        lows  = [l for l in lows  if l is not None]
        return highs, lows
    except Exception as e:
        print(f"⚠️  [{ticker}] 차트 데이터 조회 실패: {e}")
        return None, None


def calculate_support_resistance(highs, lows):

    if not highs or not lows:
        return None, None

    resistance = max(highs[-20:])  # 최근 20일 고점
    support    = min(lows[-20:])   # 최근 20일 저점

    return support, resistance


def price_position(price, support, resistance):

    if not support or not resistance:
        return "데이터 부족"

    range_val = resistance - support

    if range_val == 0:
        return "판단 불가"

    pos = (price - support) / range_val

    if pos < 0.3:
        return "🟢 저점 구간"
    elif pos < 0.7:
        return "🟡 중간 구간"
    else:
        return "🔴 고점 구간"
