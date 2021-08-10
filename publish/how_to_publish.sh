# how 2 publish

./gradlew githubRelease -PgithubToken=$GITHUB_TOKEN
py update_updatejson.py
./curseforge_all.sh -PcurseToken=$CURSEFORGE_TOKEN