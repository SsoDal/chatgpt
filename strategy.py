def calculate_levels(price, score):

    # 손절
    stop = price * 0.97

    # 익절 (점수 기반)
    if score >= 3:
        target = price * 1.07
    elif score >= 2:
        target = price * 1.05
    else:
        target = price * 1.03

    return stop, target


def classify_trade(score, volume):

    if score >= 3 and volume > 5000000:
        return "🔥 단타 강"
    elif score >= 2:
        return "⚡ 단타"
    elif score >= 1:
        return "📊 스윙"
    else:
        return "관망"
