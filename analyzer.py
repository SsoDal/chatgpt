from openai import OpenAI

client = OpenAI()

POSITIVE = ["수주", "계약", "호재", "급등", "성장", "흑자"]
NEGATIVE = ["적자", "하락", "악재", "손실", "규제"]


def score_news(text):
    score = 0
    for w in POSITIVE:
        if w in text:
            score += 2
    for w in NEGATIVE:
        if w in text:
            score -= 2
    return score


def extract_stocks(news):
    prompt = f"""
뉴스에서 언급된 기업명만 JSON 배열로 반환:
{news}
"""
    res = client.chat.completions.create(
        model="gpt-4.1-mini",
        messages=[{"role": "user", "content": prompt}],
        temperature=0.2
    )

    try:
        return eval(res.choices[0].message.content)
    except:
        return []
