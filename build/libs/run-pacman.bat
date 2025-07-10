@echo off
REM -------------------------------
REM Script di avvio per PacMan2.0
REM -------------------------------

REM 1) Percorso alla cartella 'lib' del JavaFX SDK
set "JAVA_FX=C:\javafx-sdk-17.0.15\lib"

REM 2) Nome del jar da lanciare
set "JAR_NAME=PacManProject-all.jar"

REM 3) Verifica che il jar esista
if not exist "%JAR_NAME%" (
  echo ERRORE: il file "%JAR_NAME%" non e^' stato trovato nella cartella corrente.
  pause
  exit /b 1
)

REM 4) Avvia Pac‑Man con i moduli JavaFX richiesti
java --module-path "%JAVA_FX%" ^
     --add-modules javafx.controls,javafx.fxml,javafx.graphics ^
     -jar "%JAR_NAME%"

REM 5) Mantieni aperta la finestra se l'app termina
echo.
echo Premere un tasto per chiudere...
pause >nul
