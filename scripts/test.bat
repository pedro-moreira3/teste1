@echo off

rem Script para executar testes automatizados no projeto

rem Use a propriedade 'webdriver.chrome.driver' para especificar a localização do webdriver chrome. e.g. -Dwebdriver.chrome.driver=C:\\web-drivers\\chromedriver.exe 
rem Use a propriedade 'webdriver.gecko.driver' para especificar a localização do webdriver firefox. e.g. -Dwebdriver.gecko.driver=C:\\web-drivers\\geckodriver.exe 
rem Use a propriedade 'urlteste' para mudar o endereço de teste, o padrão é localhost. e.g. -Durlteste=http://xxx.xxx.xxx.xxx/intelidente
rem Use a propriedade 'headless' para executar chrome sem interface gráfica, por padrão está desativado. e.g. -Dheadless=true
rem Use a propriedade 'finalizar' para especificar se o navegador deve ou não finalizar após o teste, o padrão é true. e.g. -Dfinalizar=false

rem Execução de script de config para extração de variaveis
SETLOCAL EnableDelayedExpansion
call config.bat
cd %~dp0

if not defined SELENIUM_CHROME_DRIVER (
	echo "Variável SELENIUM_CHROME_DRIVER não está configurada, por favor aponte para driver do chrome"
	pause
	exit /b
)

rem Caso não configuradom pergunta para qual ambiente realizar deploy
if not defined DEPLOY_LOCAL (
	set /p DEPLOY_LOCAL="Realizar teste local? y/n"
)

set TEST_URL=
if [%DEPLOY_LOCAL%]==[n] (
	if not defined SERVER_URL (
		set /p SERVER_URL="Url de testes não configurada, por favor insira url teste"
	)
	set TEST_URL="-Durlteste=!SERVER_URL!"
)

cd %PROJECT_HOME%
call mvn test "-Dwebdriver.chrome.driver=%SELENIUM_CHROME_DRIVER%" "-Dfinalizar=false" "-Dmaven.main.skip=true" %TEST_URL%
if [%ERRORLEVEL%]==[0] (
	exit /b
)

ENDLOCAL
cd %~dp0
pause