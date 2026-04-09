import json
from openai import OpenAI
from config import OPENAI_API_KEY

# OpenAI 키 주입 (config에서 읽어서 전달)
client = OpenAI(api_key=OPENAI_API_KEY)

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
    try:
        res = client.chat.completions.create(
            model="gpt-4.1-mini",
            messages=[{"role": "user", "content": prompt}],
            temperature=0.2
        )
        content = res.choices[0].message.content.strip()

        # 코드 블록 제거 (```json ... ``` 형식 처리)
        if content.startswith("```"):
            content = content.split("```")[1]
            if content.startswith("json"):
                content = content[4:]
            content = content.strip()

        return json.loads(content)

    except Exception as e:
        print(f"⚠️  종목 추출 실패: {e}")
        return []
