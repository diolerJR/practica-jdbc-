# Ejecuta la práctica JDBC con la consola en UTF-8 (acentos correctos)
# Uso: .\run.ps1
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

mvn -q compile
if ($LASTEXITCODE -ne 0) { Write-Error "Error compilando"; exit 1 }

mvn -q dependency:build-classpath "-Dmdep.outputFile=cp.txt"
if ($LASTEXITCODE -ne 0) { Write-Error "Error resolviendo dependencias"; exit 1 }

$cp = (Get-Content cp.txt -Raw).Trim()
java "-Dfile.encoding=UTF-8" "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" `
     -cp "target/classes;$cp" com.accesodatos.practicajdbc.Main
