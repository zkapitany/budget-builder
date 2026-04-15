@echo off
setlocal enabledelayedexpansion

REM ===== 0) Ensure JDK is used =====
if "%JAVA_HOME%"=="" (
  echo JAVA_HOME is not set. Please set it to a JDK 21 folder.
  exit /b 1
)

if not exist "%JAVA_HOME%\jmods" (
  echo ERROR: "%JAVA_HOME%\jmods" not found. JAVA_HOME must point to a JDK (not JRE).
  exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"
echo Using JAVA_HOME=%JAVA_HOME%
java -version
echo.

REM ===== 1) Build fat-jar (also downloads JavaFX) =====
call mvn -q clean package -DskipTests
if errorlevel 1 (
  echo Maven build failed
  exit /b 1
)

REM ===== 2) Locate JavaFX WIN JARs in local Maven repo =====
set "M2=%USERPROFILE%\.m2\repository"
set "JFXV=21.0.1"

set "JFX_BASE=%M2%\org\openjfx\javafx-base\%JFXV%\javafx-base-%JFXV%-win.jar"
set "JFX_GRAPHICS=%M2%\org\openjfx\javafx-graphics\%JFXV%\javafx-graphics-%JFXV%-win.jar"
set "JFX_CONTROLS=%M2%\org\openjfx\javafx-controls\%JFXV%\javafx-controls-%JFXV%-win.jar"
set "JFX_FXML=%M2%\org\openjfx\javafx-fxml\%JFXV%\javafx-fxml-%JFXV%-win.jar"

if not exist "%JFX_BASE%" (
  echo ERROR: Not found: %JFX_BASE%
  exit /b 1
)
if not exist "%JFX_GRAPHICS%" (
  echo ERROR: Not found: %JFX_GRAPHICS%
  exit /b 1
)
if not exist "%JFX_CONTROLS%" (
  echo ERROR: Not found: %JFX_CONTROLS%
  exit /b 1
)
if not exist "%JFX_FXML%" (
  echo ERROR: Not found: %JFX_FXML%
  exit /b 1
)

set "MP=%JAVA_HOME%\jmods;%JFX_BASE%;%JFX_GRAPHICS%;%JFX_CONTROLS%;%JFX_FXML%"

echo JavaFX jars:
echo %JFX_BASE%
echo %JFX_GRAPHICS%
echo %JFX_CONTROLS%
echo %JFX_FXML%
echo.

REM ===== 3) Create custom runtime (jlink) =====
rmdir /s /q target\runtime 2>nul

jlink ^
  --module-path "%MP%" ^
  --add-modules java.base,java.logging,java.desktop,java.naming,java.xml,java.scripting,javafx.controls,javafx.fxml,javafx.graphics ^
  --output target\runtime ^
  --strip-debug ^
  --no-header-files ^
  --no-man-pages

if errorlevel 1 (
  echo jlink failed
  exit /b 1
)

REM ===== 4) Package EXE (jpackage) =====
rmdir /s /q target\dist 2>nul

jpackage ^
  --type exe ^
  --name BudgetBuilder ^
  --app-version 1.0.0 ^
  --vendor "zkapitany" ^
  --dest target\dist ^
  --input target ^
  --app-content template ^
  --main-jar budget-builder-1.0.0-jar-with-dependencies.jar ^
  --main-class com.budgetbuilder.BudgetBuilderApp ^
  --runtime-image target\runtime ^
  --win-menu ^
  --win-shortcut

if errorlevel 1 (
  echo jpackage failed
  exit /b 1
)

echo.
echo DONE!
echo EXE location: target\dist\BudgetBuilder\BudgetBuilder.exe
endlocal
pause
