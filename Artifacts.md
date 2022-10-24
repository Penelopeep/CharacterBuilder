# <center> How to add new builds to `builds.json`?

First let's look at the schema for it:
`````json
"CharacterName":{ //For example: Eula/Diluc/Itto
    // After character name use "{"
    // and on the end there's "}"
    "BuildName1":[ // After build name there's "['
        "ArtifactCode1", //Place for artifact codes
        "ArtifactCode2", 
        "ArtifactCode3", //If there's next artifact
        "ArtifactCode4", //place "," at the end
        "ArtifactCode5" //Don't use "," here
    ], // Close each build with "]"
    // If there's more than one build also use ","
    "BuildName2":[ //Name it however you want
        "ArtifactCode1", //Place for artifact codes
        "ArtifactCode2",
        "ArtifactCode3",
        "ArtifactCode4",
        "ArtifactCode5" //Don't use "," here
    ] // On last build don't use ","
}
`````
## Example build:

`````json
"Zhongli": {
  "tenacityhp": [
    "91544 10001 501024,1 501034,6 501204,1 501224,1 lv20",
    "91524 10003 501024,1 501034,6 501204,1 501224,1 lv20",
    "91554 10002 501024,6 501064,1 501204,1 501224,1 lv20",
    "91514 10002 501024,6 501064,1 501204,1 501224,1 lv20",
    "91534 10002 501024,6 501064,1 501204,1 501224,1 lv20"
  ],
  "tenacityatk": [
    "91544 10001 501054,1 501064,1 501204,1 501224,6 lv20",
    "91524 10003 501064,1 501244,1 501204,1 501224,6 lv20",
    "91554 10004 501054,1 501244,1 501204,1 501224,6 lv20",
    "91514 15013 501054,1 501064,1 501204,1 501224,6 lv20",
    "91534 30950 501054,1 501064,1 501204,1 501224,6 lv20"
  ]
}
`````

# FAQ (Important!)

1. **Some characters don't work after I add them**:

### Plugin uses coded names for characters, usually it's not a problem since most characters are coded with their name, BUT:
#### Characters with 2nd name like `Kamisato Ayaka` uses their name as codename (for example `Kamisato Ayato = Ayato, Sangonomiya Kokomi = Kokomi`)
#### Few character have different name, be aware of it. This is list of these characters:

- Amber = Ambor
- Yanfei = Feiyan
- Noelle = Noel
- Lumine = PlayerGirl
- Aether = PlayerBoy
- Jean = Qin
- Raiden = Shougun
- Thoma = Tohma
- Yun Jin = Yunjin (she's the only example of 2 names being merged into one)

2. **Are order of artifacts matter?**
 - No, you put them in any order
3. **Can I put more than 5 artifacts?**
 - Yes but why would you?
4. **Some stats are missing!**
 - It'll happen and I can't help with that since there are many versions of Grasscutter resources.
