$ErrorActionPreference = "Stop"

$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "JAVA_HOME=$env:JAVA_HOME"
java -version
jlink --version
jpackage --version

Write-Host "`n== Maven build =="
mvn clean package -DskipTests

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

Write-Host "`n== jpackage exe =="
Remove-Item -Recurse -Force "target\dist" -ErrorAction SilentlyContinue
jpackage `
  --type exe `
  --name BudgetBuilder `
  --app-version 1.0.0 `
  --vendor "zkapitany" `
  --dest "target\dist" `
  --input "target" `
  --app-content "template" `
  --main-jar "budget-builder-1.0.0-jar-with-dependencies.jar" `
  --main-class "com.budgetbuilder.BudgetBuilderApp" `
  --runtime-image "target\runtime" `
  --win-menu `
  --win-shortcut

Write-Host "`nDONE! EXE: target\dist\BudgetBuilder\BudgetBuilder.exe"
