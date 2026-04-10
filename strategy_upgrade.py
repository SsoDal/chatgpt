def calculate_winrate(score, volume, position):

    winrate = 50

    # 뉴스 점수
    winrate += score * 5

    # 거래량
    if volume > 5000000:
        winrate += 10

    # 위치
    if "저점" in position:
        winrate += 10
    elif "고점" in position:
        winrate -= 10

    return max(5, min(95, winrate))
