import requests
import json

proxies = {'http': 'http://127.0.0.1:7890', 'https': 'http://127.0.0.1:7890'}

def test_url(url):
    print('=== Testing:', url)
    try:
        r = requests.get(url, proxies=proxies, timeout=15)
        print('Status code:', r.status_code)
        if r.status_code == 200:
            data = r.json()
            mrdata = data.get('MRData', {})
            print('MRData keys:', list(mrdata.keys()))
            for k in ['RaceTable', 'StandingsTable']:
                if k in mrdata:
                    total = mrdata.get('total')
                    season = mrdata[k].get('season')
                    print(f'{k} summary: season={season}, total={total}')
                    print(json.dumps(mrdata[k], indent=2)[:800])
        else:
            print('Error body:', r.text[:300])
    except Exception as e:
        print('Exception:', e)

if __name__ == '__main__':
    test_url('https://api.jolpi.ca/ergast/f1/current.json')
    test_url('https://api.jolpi.ca/ergast/f1/current/driverStandings.json')
    test_url('https://api.jolpi.ca/ergast/f1/current/constructorStandings.json')
    test_url('https://api.jolpi.ca/ergast/f1/current/last/results.json')
    test_url('https://api.jolpi.ca/ergast/f1/current/1/results.json')
