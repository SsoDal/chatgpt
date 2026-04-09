import requests
from config import TELEGRAM_TOKEN, TELEGRAM_CHAT_ID


def send(msg: str) -> bool:
    """텔레그램 메시지 전송. 성공 시 True, 실패 시 False 반환."""
    if not TELEGRAM_TOKEN or not TELEGRAM_CHAT_ID:
        print("⚠️  TELEGRAM_TOKEN 또는 TELEGRAM_CHAT_ID가 설정되지 않았습니다.")
        return False

    url = f"https://api.telegram.org/bot{TELEGRAM_TOKEN}/sendMessage"
    try:
        res = requests.post(url, data={
            "chat_id": TELEGRAM_CHAT_ID,
            "text": msg
        }, timeout=10)
        res.raise_for_status()
        result = res.json()
        if result.get("ok"):
            print("✅ 텔레그램 전송 성공")
            return True
        else:
            print(f"⚠️  텔레그램 오류: {result.get('description')}")
            return False
    except Exception as e:
        print(f"⚠️  텔레그램 전송 실패: {e}")
        return False
