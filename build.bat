@echo off
REM Script de compilacion para Windows (CMD)
REM Uso: build.bat [compileFX|runFX|clean]

set SRC_DIR=src
set BIN_DIR=bin

if "%1"=="" goto runFX
if "%1"=="compileFX" goto compileFX
if "%1"=="runFX" goto runFX
if "%1"=="clean" goto clean

:compileFX
echo Compilando AM Visual (JavaFX)...

if "%JAVAFX_HOME%"=="" (
    echo [ERROR] La variable de entorno JAVAFX_HOME no esta definida.
    echo        Descarga JavaFX SDK y define JAVAFX_HOME a su directorio.
    goto :eof
)

if not exist %BIN_DIR% mkdir %BIN_DIR%

set MODULE_PATH=%JAVAFX_HOME%\lib

cd %SRC_DIR%
dir /s /b *.java > sources.txt
javac --module-path "%MODULE_PATH%" --add-modules javafx.controls,javafx.fxml,javafx.swing -d ..\%BIN_DIR% @sources.txt
set COMPILE_RESULT=%ERRORLEVEL%
del sources.txt
cd ..

if %COMPILE_RESULT% EQU 0 (
    REM Copiar recursos FXML y CSS
    xcopy /s /y %SRC_DIR%\com\amvisual\viewfx\*.fxml %BIN_DIR%\com\amvisual\viewfx\ >nul 2>&1
    xcopy /s /y %SRC_DIR%\com\amvisual\viewfx\css\*.css %BIN_DIR%\com\amvisual\viewfx\css\ >nul 2>&1
    echo [OK] Compilacion JavaFX exitosa
) else (
    echo [ERROR] Error en compilacion JavaFX
)
goto :eof

:runFX
call :compileFX
if %ERRORLEVEL% NEQ 0 goto :eof

echo Ejecutando AM Visual (JavaFX)...

if not exist %BIN_DIR% (
    echo [ERROR] No se encontraron archivos compilados
    goto :eof
)

if "%JAVAFX_HOME%"=="" (
    echo [ERROR] La variable de entorno JAVAFX_HOME no esta definida.
    goto :eof
)

set MODULE_PATH=%JAVAFX_HOME%\lib

cd %BIN_DIR%
java --module-path "%MODULE_PATH%" --add-modules javafx.controls,javafx.fxml,javafx.swing --enable-native-access=javafx.graphics --add-opens java.base/java.lang=ALL-UNNAMED com.amvisual.AMVisualFXApp
cd ..
goto :eof

:clean
echo Limpiando archivos compilados...
if exist %BIN_DIR% (
    rmdir /s /q %BIN_DIR%
    echo [OK] Archivos eliminados
) else (
    echo [INFO] No hay archivos para limpiar
)
goto :eof
