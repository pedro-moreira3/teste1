@echo off

rem Utilizar script para gerar war limpo do projeto
rem Caso algum erro de stackoverflow seja causado pelo lombok, apagar pasta target manualmente e executar script novamente

rem Execução de script de config para extração de variaveis
SETLOCAL EnableDelayedExpansion
call config.bat
cd %~dp0

cd %PROJECT_HOME%

@echo "Limpar projeto completamente? y/n"
set /p CLEAN_NOW=
if /I [%CLEAN_NOW%]==[Y] (
	call mvn clean initialize compile war:war
) else (
	call mvn initialize compile war:war
)

if [%ERRORLEVEL%]==[0] (
	@echo "Realizar deploy? y/n"
	set /p DEPLOY_NOW=
	if /I [!DEPLOY_NOW!]==[Y] (
		cd %~dp0
		call deploy.bat
	)
	exit /b 0
)

ENDLOCAL
pause
exit /b 1