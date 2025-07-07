@echo off
setlocal

set FX_LIB=javafx-sdk-17.0.15\lib

java --module-path "%FX_LIB%" --add-modules javafx.controls,javafx.fxml -jar PacManProject-all.jar

pause
