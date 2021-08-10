import json

jsonPath = "../../updatejson/update-matmos.json"

data = json.load(open(jsonPath, "r", encoding="utf8"))
ver = open("../version.txt", "r").read()

for gameVer in open("gameVersions.txt", "r").read().split(" "):
    fullVer = "{}-{}".format(gameVer, ver)
    
    if gameVer not in data:
        data[gameVer] = {}
    
    data[gameVer][fullVer] = ""
    data["promos"]["{}-latest".format(gameVer)] = fullVer

json.dump(data, open(jsonPath, "w", encoding="utf8"), indent=2)