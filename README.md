# Budget Builder

Költségvetés kezelő alkalmazás JavaFX-ben.

## Funkciók

- ✅ Template Excel fájl automatikus betöltése
- ✅ `Preferencies.txt` alapú export/template könyvtár beállítás az alkalmazás mappájában
- ✅ Tételek kiválasztása és hozzáadása
- ✅ Anyagok és munkatételek szűrése
- ✅ Memória alapú költségvetés kezelés
- ✅ Excel export szép formázással
- ✅ Duplikátumok konszolidálása

## Technológia

- **Language**: Java
- **GUI**: JavaFX
- **Build**: Maven
- **Excel**: Apache POI
- **Logging**: SLF4J / Logback

## Előfeltételek

- Java 11+
- Maven 3.6+

## Telepítés

```bash
git clone https://github.com/zkapitany/budget-builder.git
cd budget-builder

## Futtatás

mvn javafx:run

## Konfiguráció

- Az alkalmazás induláskor az app mappában létrehozza/használja a `Preferencies.txt` fájlt.
- Kezelt kulcsok:
  - `default.export.directory`
  - `default.template.directory`
- Alap template helye: `template/template.xlsx`

##Szerző

zkapitany
