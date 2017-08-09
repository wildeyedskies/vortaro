# Senreta Vortaro

Senreta Vortaro (Offline Dictionary) is an english-esperanto dictionary app inspired by the
web page [Tuja Vortaro](https://tujavortaro.net). It is designed to be easy to use for beginners.

<a href="https://f-droid.org/packages/org.mcxa.vortaro/" target="_blank">
<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="90"/></a>

## Data Sources

The app gets its dictionary data from the following sources. More information is available in the dictionary-src directory.
* ESPDIC -  esperanto-english translations
* Etymology - sourced from tuja vortaro's github page originally from archive.org
* Transitiveco - also sourced from tuja vortaro

## For future consideration
I would like to add more sources to the app in the future, that would require some extra data parsing and normalization.
* [ReVo](http://reta-vortaro.de/revo/) - very large multilanguage esperanto dictionary
* [Komputeko](http://komputeko.net/index_en.php) - library of computer terms

## App Architecture
Senreta Vortaro is written in the Kotlin language for Android.

Currently the app loads the espdic, etymology and transitiveco data from an SQLite database that is copied into the data folder when the app is first launched. Both English and Esperanto words are indexed in the database file, and searching is performed in a background thread. The current setup should be fairly performant.

The app displays output in a RecyclerView. Currently only exact matches to the english and esperanto words are included. Inexact matches will need to be added later, but they need to be sorted after exact matches.
