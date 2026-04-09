import os
import json
from flask import Flask, render_template

app = Flask(__name__)

@app.route("/")
def home():
    # latest.json 없을 때 빈 화면 대신 안내 메시지 표시
    json_path = os.path.join(os.path.dirname(__file__), "..", "latest.json")
    try:
        with open(json_path, "r", encoding="utf-8") as f:
            data = json.load(f)
    except FileNotFoundError:
        data = []
    return render_template("index.html", data=data)


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
