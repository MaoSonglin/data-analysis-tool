import requests
from bs4 import BeautifulSoup
import json
 
# 获取html文档
def get_html(url):
    """get the content of the url"""
    response = requests.get(url)
    response.encoding = 'utf-8'
    return response.text
    
# 获取笑话
def get_certain_joke(html):
    """get the joke of the html"""
    soup = BeautifulSoup(html)
    joke_content = soup.select("i[class^='am-icon']")
    a = []
    for i in joke_content:
        x = [item for item in i.next_siblings]
        css = i['class']
        if len(css)>0 and len(x) > 0:
            a.append({"icon":i['class'][0],"name":x[0]})
    return a
  
 
url_joke = "http://amazeui.org/css/icon#suo-you-tu-biao-lie-biao"
html = get_html(url_joke)
icons = get_certain_joke(html)
with open("icons.json","w",encoding="utf-8") as f:
    json.dump(icons,f)
# print joke_content
