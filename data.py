import requests

def get_market_data(ticker):
    url = f"https://query1.finance.yahoo.com/v8/finance/chart/{ticker}"
    r = requests.get(url).json()

    try:
        meta = r["chart"]["result"][0]["meta"]
        return {
            "price": meta["regularMarketPrice"],
            "volume": meta["regularMarketVolume"]
        }
    except:
        return None
