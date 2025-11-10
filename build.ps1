# Script de compilación y ejecución para AM Visual
# Uso: .\build.ps1 [compile|run|clean|all]

param(
    [Parameter(Position=0)]
    [ValidateSet('compile', 'run', 'clean', 'all', 'compileFX', 'runFX')]
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

function Copy-Resources {
    # Copiar archivos .fxml y .css a bin/ preservando estructura
    Get-ChildItem -Path $SrcDir -Recurse -Include *.fxml, *.css | ForEach-Object {
        $relPath = $_.FullName.Substring((Resolve-Path $SrcDir).Path.Length)
        $dest = Join-Path $OutputDir $relPath
        $destDir = Split-Path $dest -Parent
        if (-not (Test-Path $destDir)) { New-Item -ItemType Directory -Path $destDir | Out-Null }
        Copy-Item $_.FullName $dest -Force
    }
}

function CompileFX {
    Write-Host "Compilando AM Visual (JavaFX)..." -ForegroundColor Cyan

    if (-not $env:JAVAFX_HOME) {
        Write-Host "✗ La variable de entorno JAVAFX_HOME no está definida." -ForegroundColor Red
        Write-Host "  Descarga JavaFX SDK y define JAVAFX_HOME a su directorio (contiene la carpeta 'lib')."
        return $false
    }

    if (-not (Test-Path $OutputDir)) { New-Item -ItemType Directory -Path $OutputDir | Out-Null }

    $modulePath = Join-Path $env:JAVAFX_HOME 'lib'
    Push-Location $SrcDir
    $sources = Get-ChildItem -Recurse -Include *.java | ForEach-Object { $_.FullName }
    & javac --module-path "$modulePath" --add-modules javafx.controls,javafx.fxml,javafx.swing -d "..\$OutputDir" $sources
    $compileResult = $LASTEXITCODE
    Pop-Location

    if ($compileResult -eq 0) {
        Copy-Resources
        Write-Host "✓ Compilación JavaFX exitosa" -ForegroundColor Green
        return $true
    } else {
        Write-Host "✗ Error en compilación JavaFX" -ForegroundColor Red
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

function RunFX {
    Write-Host "Ejecutando AM Visual (JavaFX)..." -ForegroundColor Cyan

    if (-not (Test-Path $OutputDir)) {
        Write-Host "✗ No se encontraron archivos compilados. Ejecute primero 'compileFX'" -ForegroundColor Red
        return
    }

    if (-not $env:JAVAFX_HOME) {
        Write-Host "✗ La variable de entorno JAVAFX_HOME no está definida." -ForegroundColor Red
        return
    }

    $modulePath = Join-Path $env:JAVAFX_HOME 'lib'
    Push-Location $OutputDir
    & java --module-path "$modulePath" --add-modules javafx.controls,javafx.fxml,javafx.swing --enable-native-access=javafx.graphics --add-opens java.base/java.lang=ALL-UNNAMED com.amvisual.AMVisualFXApp
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

# Verificar que las funciones estén disponibles
if (-not (Get-Command CompileFX -ErrorAction SilentlyContinue)) {
    Write-Host "Error: La función CompileFX no está definida correctamente." -ForegroundColor Red
    exit 1
}

# Ejecutar acción
switch ($Action) {
    'compile' { 
        Compile
    }
    'run' { 
        Run
    }
    'clean' { 
        Clean
    }
    'all' { 
        Clean
        $compileResult = Compile
        if ($compileResult) {
            Run
        }
    }
    'compileFX' { 
        CompileFX
    }
    'runFX' { 
        $compileResult = CompileFX
        if ($compileResult) {
            RunFX
        }
    }
}
