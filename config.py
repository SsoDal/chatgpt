import os
from dotenv import load_dotenv

# .env 파일 자동 로드
load_dotenv()

# Telegram 설정
TELEGRAM_TOKEN = os.getenv("TELEGRAM_TOKEN", "")
TELEGRAM_CHAT_ID = os.getenv("TELEGRAM_CHAT_ID", "")

# OpenAI 설정
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "")

# 필수 키 확인
if not TELEGRAM_TOKEN:
    print("⚠️  경고: TELEGRAM_TOKEN이 설정되지 않았습니다.")
if not TELEGRAM_CHAT_ID:
    print("⚠️  경고: TELEGRAM_CHAT_ID가 설정되지 않았습니다.")
if not OPENAI_API_KEY:
    print("⚠️  경고: OPENAI_API_KEY가 설정되지 않았습니다.")
