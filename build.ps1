# Script de compilación y ejecución para AM Visual
# Uso: .\build.ps1 [compile|run|clean|all]

param(
    [Parameter(Position=0)]
    [ValidateSet('compile', 'run', 'clean', 'all')]
    [string]$Action = 'all'
)

$SrcDir = "src"
$OutputDir = "bin"

function Compile {
    Write-Host "Compilando AM Visual..." -ForegroundColor Cyan
    
    # Crear directorio de salida si no existe
    if (-not (Test-Path $OutputDir)) {
        New-Item -ItemType Directory -Path $OutputDir | Out-Null
    }
    
    # Compilar
    Push-Location $SrcDir
    javac -d "..\$OutputDir" com/amvisual/AplicacionMarcaAgua.java
    $compileResult = $LASTEXITCODE
    Pop-Location
    
    if ($compileResult -eq 0) {
        Write-Host "✓ Compilación exitosa" -ForegroundColor Green
        return $true
    } else {
        Write-Host "✗ Error en compilación" -ForegroundColor Red
        return $false
    }
}

function Run {
    Write-Host "Ejecutando AM Visual..." -ForegroundColor Cyan
    
    if (-not (Test-Path $OutputDir)) {
        Write-Host "✗ No se encontraron archivos compilados. Ejecute primero 'compile'" -ForegroundColor Red
        return
    }
    
    Push-Location $OutputDir
    java com.amvisual.AplicacionMarcaAgua
    Pop-Location
}

function Clean {
    Write-Host "Limpiando archivos compilados..." -ForegroundColor Cyan
    
    if (Test-Path $OutputDir) {
        Remove-Item -Recurse -Force $OutputDir
        Write-Host "✓ Archivos eliminados" -ForegroundColor Green
    } else {
        Write-Host "✓ No hay archivos para limpiar" -ForegroundColor Yellow
    }
}

# Ejecutar acción
switch ($Action) {
    'compile' { Compile }
    'run' { Run }
    'clean' { Clean }
    'all' { 
        Clean
        if (Compile) {
            Run
        }
    }
}
