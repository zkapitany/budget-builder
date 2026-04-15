$ErrorActionPreference = "Stop"

# Állítsd be a JDK-t (nálad ez biztosan jó):
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "JAVA_HOME=$env:JAVA_HOME"
java -version
jlink --version
jpackage --version

Write-Host "`n== Maven build =="
mvn clean package -DskipTests

# JavaFX Win JAR-ok a local Maven repo-ból
$M2 = Join-Path $env:USERPROFILE ".m2\repository"
$JFXV = "21.0.1"

$jfxBase     = Join-Path $M2 "org\openjfx\javafx-base\$JFXV\javafx-base-$JFXV-win.jar"
$jfxGraphics = Join-Path $M2 "org\openjfx\javafx-graphics\$JFXV\javafx-graphics-$JFXV-win.jar"
$jfxControls = Join-Path $M2 "org\openjfx\javafx-controls\$JFXV\javafx-controls-$JFXV-win.jar"
$jfxFxml     = Join-Path $M2 "org\openjfx\javafx-fxml\$JFXV\javafx-fxml-$JFXV-win.jar"

@($jfxBase,$jfxGraphics,$jfxControls,$jfxFxml) | ForEach-Object {
  if (-not (Test-Path $_)) { throw "Missing JavaFX jar: $_" }
}

$modulePath = "$env:JAVA_HOME\jmods;$jfxBase;$jfxGraphics;$jfxControls;$jfxFxml"

Write-Host "`n== jlink runtime =="
Remove-Item -Recurse -Force "target\runtime" -ErrorAction SilentlyContinue
jlink `
  --module-path $modulePath `
  --add-modules "java.base,java.logging,java.desktop,java.naming,java.xml,java.scripting,javafx.controls,javafx.fxml,javafx.graphics" `
  --output "target\runtime" `
  --strip-debug `
  --no-header-files `
  --no-man-pages

# --- FIX: dedikált jpackage input staging dir + jar automatikus keresése ---
$TargetDir = Join-Path $PSScriptRoot "target"

$Jar = Get-ChildItem -Path $TargetDir -Filter "*-jar-with-dependencies.jar" -File |
  Sort-Object LastWriteTime -Descending |
  Select-Object -First 1

if (-not $Jar) {
  throw "Nem találok '*-jar-with-dependencies.jar' fájlt a target mappában: $TargetDir"
}

$JarName = $Jar.Name
$JarPath = $Jar.FullName
Write-Host "Using fat-jar: $JarPath"

$JpkgInputDir = Join-Path $TargetDir "jpackage-input"
Remove-Item -Recurse -Force $JpkgInputDir -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $JpkgInputDir | Out-Null

Copy-Item -Force $JarPath (Join-Path $JpkgInputDir $JarName)

Write-Host "`n== jpackage app-image (WiX nélkül) =="
Remove-Item -Recurse -Force (Join-Path $TargetDir "dist") -ErrorAction SilentlyContinue
jpackage `
  --type app-image `
  --name BudgetBuilder `
  --app-version 1.0.0 `
  --vendor "zkapitany" `
  --dest (Join-Path $TargetDir "dist") `
  --input $JpkgInputDir `
  --app-content "template" `
  --main-jar $JarName `
  --main-class "com.budgetbuilder.BudgetBuilderApp" `
  --runtime-image (Join-Path $TargetDir "runtime")

Write-Host "`nDONE!"
Write-Host "App image folder: target\dist\BudgetBuilder"
Write-Host "EXE várható helye: target\dist\BudgetBuilder\BudgetBuilder.exe (vagy target\dist\BudgetBuilder\bin\BudgetBuilder.exe)"