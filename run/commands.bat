del /q "d:\packtemp\trunk\test003.txt"
svn export --username admin --password admin --non-interactive --force -r 27 "https://lenovo-pc/svn/jiratest/trunk/yyy" "d:/packtemp/trunk/yyy"
svn export --username admin --password admin --non-interactive --force -r 28 "https://lenovo-pc/svn/jiratest/trunk/xxx" "d:/packtemp/trunk/xxx"
svn export --username admin --password admin --non-interactive --force -r 28 "https://lenovo-pc/svn/jiratest/trunk/xxx/xx.txt" "d:/packtemp/trunk/xxx/xx.txt"
svn export --username admin --password admin --non-interactive --force -r 28 "https://lenovo-pc/svn/jiratest/trunk/yyy/xx - 副本.txt" "d:/packtemp/trunk/yyy/xx - 副本.txt"
del /q "d:\packtemp\trunk\yyy\xx.txt"
svn export --username admin --password admin --non-interactive --force -r 28 "https://lenovo-pc/svn/jiratest/trunk/yyy/zzz" "d:/packtemp/trunk/yyy/zzz"
pause
exit
