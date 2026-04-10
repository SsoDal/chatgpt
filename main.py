import json
import time
from analyzer import score_news, extract_stocks
from data import get_market_data
from strategy import calculate_levels, classify_trade
from chart import get_chart_data, calculate_support_resistance, price_position
from strategy_upgrade import calculate_winrate
from telegram_bot import send


def run(news_list):

    results = []

    for news in news_list:
        stocks = extract_stocks(news)
        score = score_news(news)
        print(f"📰 뉴스: {news}")
        print(f"   감성 점수: {score} | 추출 종목: {stocks}")

        for s in stocks:
            market = get_market_data(s)

            if not market:
                continue

            price  = market["price"]
            volume = market["volume"]

            stop, target = calculate_levels(price, score)
            strategy     = classify_trade(score, volume)

            # 차트 분석 (지지/저항 + 위치)
            highs, lows             = get_chart_data(market["ticker"])
            support, resistance     = calculate_support_resistance(highs, lows)
            position                = price_position(price, support, resistance)

            # 승률 계산
            winrate = calculate_winrate(score, volume, position)

            results.append({
                "ticker":     market["ticker"],
                "price":      price,
                "volume":     volume,
                "strategy":   strategy,
                "stop":       round(stop, 0),
                "target":     round(target, 0),
                "support":    round(support, 0)    if support    else None,
                "resistance": round(resistance, 0) if resistance else None,
                "position":   position,
                "winrate":    winrate
            })

            print(f"   ✅ {market['ticker']} | 현재가: {price:,.0f} | {strategy}")
            print(f"      손절: {stop:,.0f} / 익절: {target:,.0f}")
            print(f"      지지: {support} / 저항: {resistance} | 위치: {position} | 승률: {winrate}%")

    if not results:
        print("⚠️  분석 결과 없음 (종목 추출 실패 또는 시세 조회 실패)")
        return

    # 저장 (웹용)
    with open("latest.json", "w", encoding="utf-8") as f:
        json.dump(results, f, ensure_ascii=False, indent=2)
    print("💾 latest.json 저장 완료")

    # 텔레그램
    msg = "📊 매매 판단 정보\n\n"
    for r in results:
        msg += f"{r['ticker']} | {r['strategy']}\n"
        msg += f"승률: {r['winrate']}%\n"
        msg += f"위치: {r['position']}\n"
        msg += f"손절 {r['stop']:.0f} / 익절 {r['target']:.0f}\n\n"

    send(msg)


if __name__ == "__main__":
    print("🚀 트레이딩 판단 보조 시스템 시작")
    print("⏱️  5분마다 자동 분석 실행 (Ctrl+C 로 종료)\n")

    while True:
        try:
            print(f"{'='*50}")
            print(f"🔄 분석 실행 중...")
            news = ["삼성전자 AI 투자 확대", "테슬라 실적 증가"]
            run(news)
        except KeyboardInterrupt:
            print("\n👋 시스템 종료")
            break
        except Exception as e:
            print(f"❌ 오류 발생: {e}")

        print(f"\n⏳ 5분 후 재실행...\n")
        time.sleep(300)
