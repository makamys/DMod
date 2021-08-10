# how 2 publish
```
1. ./gradlew githubRelease -PgithubToken=`cat $GITHUB_TOKEN_PATH`
2. py update_updatejson.py
3. ./curseforge_all.sh -PcurseToken=`cat $CURSEFORGE_TOKEN_PATH`
4. ./modrinth_all.sh -PmodrinthToken=`cat $MODRINTH_TOKEN_PATH`
```