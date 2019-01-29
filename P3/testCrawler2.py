import requests
from bs4 import BeautifulSoup
s = requests.Session()
data = {'username': 'u5888146', 'password': '30082540'}
s.post('https://mycourses.ict.mahidol.ac.th/login/index.php', data=data)
r = s.get('https://mycourses.ict.mahidol.ac.th/mod/forum/view.php?f=9&showall=1')
soup = BeautifulSoup(r.content)

for link in soup.find_all('a'):
    print(link.get('href'))