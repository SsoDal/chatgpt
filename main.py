import json
import time
from analyzer import score_news, extract_stocks
from data import get_market_data
from strategy import calculate_levels, classify_trade
from telegram_bot import send


def run(news_list):

    results = []

    for news in news_list:
        stocks = extract_stocks(news)
        score = score_news(news)

        for s in stocks:
            market = get_market_data(s)

            if not market:
                continue

            stop, target = calculate_levels(market["price"], score)
            strategy = classify_trade(score, market["volume"])

            results.append({
                "ticker": s,
                "price": market["price"],
                "volume": market["volume"],
                "strategy": strategy,
                "stop": stop,
                "target": target
            })

    # 저장 (웹용)
    with open("latest.json", "w") as f:
        json.dump(results, f)

    # 텔레그램
    msg = "📊 매매 판단 정보\n\n"
    for r in results:
        msg += f"{r['ticker']} | {r['strategy']}\n"
        msg += f"손절 {r['stop']:.0f} / 익절 {r['target']:.0f}\n\n"

    send(msg)


while True:
    news = ["삼성전자 AI 투자 확대", "테슬라 실적 증가"]
    run(news)
    time.sleep(300)
