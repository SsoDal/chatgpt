import requests

# Yahoo Finance 요청 시 필수 헤더 (없으면 빈 응답 차단당함)
HEADERS = {"User-Agent": "Mozilla/5.0"}

# 한국 주요 종목명 → Yahoo Finance 티커 변환표
KR_TICKER_MAP = {
    "삼성전자": "005930.KS",
    "SK하이닉스": "000660.KS",
    "LG에너지솔루션": "373220.KS",
    "삼성바이오로직스": "207940.KS",
    "현대차": "005380.KS",
    "셀트리온": "068270.KS",
    "POSCO홀딩스": "005490.KS",
    "KB금융": "105560.KS",
    "카카오": "035720.KS",
    "네이버": "035420.KS",
    "LG화학": "051910.KS",
    "삼성SDI": "006400.KS",
    "기아": "000270.KS",
    "한국전력": "015760.KS",
    "신한지주": "055550.KS",
    "하나금융지주": "086790.KS",
    "두산에너빌리티": "034020.KS",
    "크래프톤": "259960.KS",
    "카카오뱅크": "323410.KS",
    "엔씨소프트": "036570.KS",
    # 해외 주요 종목
    "테슬라": "TSLA",
    "엔비디아": "NVDA",
    "애플": "AAPL",
    "마이크로소프트": "MSFT",
    "구글": "GOOGL",
    "아마존": "AMZN",
    "메타": "META",
}


def resolve_ticker(name: str) -> str:
    """기업명 → 티커 변환. 매핑 없으면 그대로 반환."""
    return KR_TICKER_MAP.get(name, name)


def get_market_data(ticker: str) -> dict | None:
    # 기업명이 들어올 경우 티커로 변환
    ticker = resolve_ticker(ticker)

    url = f"https://query1.finance.yahoo.com/v8/finance/chart/{ticker}"
    try:
        r = requests.get(url, headers=HEADERS, timeout=10)
        r.raise_for_status()
        data = r.json()
        meta = data["chart"]["result"][0]["meta"]
        return {
            "ticker": ticker,
            "price": meta["regularMarketPrice"],
            "volume": meta.get("regularMarketVolume", 0)
        }
    except Exception as e:
        print(f"⚠️  [{ticker}] 시세 조회 실패: {e}")
        return None
