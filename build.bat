@echo off
REM Script de compilacion para Windows (CMD)
REM Uso: build.bat [compile|run|clean|all]

set SRC_DIR=src
set BIN_DIR=bin

if "%1"=="" goto all
if "%1"=="compile" goto compile
if "%1"=="run" goto run
if "%1"=="clean" goto clean
if "%1"=="all" goto all

:compile
echo Compilando AM Visual...
if not exist %BIN_DIR% mkdir %BIN_DIR%
cd %SRC_DIR%
javac -d ..\%BIN_DIR% com\amvisual\AplicacionMarcaAgua.java
if %ERRORLEVEL% EQU 0 (
    echo [OK] Compilacion exitosa
) else (
    echo [ERROR] Error en compilacion
)
cd ..
goto :eof

:run
echo Ejecutando AM Visual...
if not exist %BIN_DIR% (
    echo [ERROR] No se encontraron archivos compilados
    goto :eof
)
cd %BIN_DIR%
java com.amvisual.AplicacionMarcaAgua
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

:all
call :clean
call :compile
if %ERRORLEVEL% EQU 0 call :run
goto :eof
