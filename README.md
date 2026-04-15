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
- A beállítások a programból szerkeszthetők: **Beállítások** gomb → Export/Template könyvtár → **Mentés**

## Kiadott alkalmazás (app-image / EXE) elvárt mappaszerkezete

Az `app-image` vagy telepített EXE mappa az alábbi struktúrát kell tartalmazza:

```
BudgetBuilder/
├── BudgetBuilder.exe          ← indító futtatható
├── Preferencies.txt           ← automatikusan jön létre induláskor (szerkeszthető)
├── template/
│   └── template.xlsx          ← alap template (csomagolásba bekerül a build során)
├── app/
│   └── budget-builder-1.0.0-jar-with-dependencies.jar
└── runtime/                   ← beépített Java runtime (jlink)
```

### Preferencies.txt példa tartalma

```properties
# Budget Builder Preferences
default.export.directory=C:/Users/felhasznalo/Downloads
default.template.directory=C:/Program Files/BudgetBuilder/template
```

> **Megjegyzés:** Ha a `Preferencies.txt` hiányzik, a program automatikusan létrehozza a futtatható mellé alapértelmezett értékekkel (`user.home` export könyvtár, `<app_mappa>/template` template könyvtár).

### Build és csomagolás

Az EXE/app-image elkészítéséhez futtasd az alábbi scriptet PowerShell-ből:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\build-app-image.ps1
```

A script:
1. Maven-nel buildeli a fat-jar-t
2. `jlink`-kel elkészíti a custom Java runtime-ot
3. `jpackage`-gel összerakja az app-image-t (`target\dist\BudgetBuilder\`)
4. A `template/` mappát az app mellé csomagolja (`--app-content template`)

## Szerző

zkapitany
