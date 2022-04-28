@echo off

rem Utilizar script para realizar deploy em ambiente selecionado (clusterizado ou standalone)

rem Execução de script de config para extração de variaveis
SETLOCAL EnableDelayedExpansion
call config.bat
cd %~dp0

rem Configura comando de reinicialização

if not defined SERVER_GROUP (
	set WILDFLY_RESTART_COMMAND="-Dwildfly.commands=:shutdown(restart=true)"
) else (
	set WILDFLY_RESTART_COMMAND="-Dwildfly.commands=/server-group=%SERVER_GROUP%:restart-servers"
)

rem Caso não configuradom pergunta para qual ambiente realizar deploy
if not defined DEPLOY_LOCAL (
	set /p DEPLOY_LOCAL="Realizar deploy local? y/n"
)

rem Parâmetro de acesso ao wildfly para plugin wildfly-maven
if /I [%DEPLOY_LOCAL%]==[n] (
	set MVN_WILDFLY_ACCESS="-Dwildfly.hostname=%SERVER_HOST%" "-Dwildfly.username=%SERVER_USERN%" "-Dwildfly.password=%SERVER_PASSW%"
) else (
	set MVN_WILDFLY_ACCESS=
)

if not defined SERVER_GROUP (
	set MVN_WILDFLY_ACCESS=%MVN_WILDFLY_ACCESS% "-Dwildfly.serverGroups=%SERVER_GROUP%"
)

rem Variáveis utilizadas
@echo PROJECT_HOME: %PROJECT_HOME%
@echo PROJECT_NAME: %PROJECT_NAME%
@echo SERVER_GROUP: %SERVER_GROUP%

if [%DEPLOY_LOCAL%]==[n] (
	@echo "Realizando deploy em ambiente configurado em config.bat"
	@echo "SERVER_HOST: %SERVER_HOST%"
	@echo "SERVER_USERN: %SERVER_USERN%"
	@echo "SERVER_PASSW: %SERVER_PASSW%"
	@echo "MVN_WILDFLY_ACCESS: %MVN_WILDFLY_ACCESS%"
) else (
	@echo "Realizando deploy local"
	if not defined JBOSS_HOME (
		echo "Variável JBOSS_HOME não está configurada, por favor aponte para diretório wildfly"
		pause
		exit /b 1
	)
	@echo "Utilizando wildfly localizado em: %JBOSS_HOME%"
)

rem Caso não seja encontrado um arquivo em target/isvendas*.war o script ira chamar clean-package antes de realizar o deploy
rem if not exist "%PROJECT_HOME%\target\%PROJECT_NAME%*.war" (call clean-package.bat)

rem Navega para pasta root do projeto
cd %PROJECT_HOME%

rem Remove deploys ativos, reinicia e realiza deploy apos intervalo

call mvn wildfly:undeploy %MVN_WILDFLY_ACCESS%
if not [%ERRORLEVEL%]==[0] ( 
	pause 
	exit /b 1
)

call mvn wildfly:execute-commands %WILDFLY_RESTART_COMMAND% %MVN_WILDFLY_ACCESS%
if not [%ERRORLEVEL%]==[0] ( 
	pause 
	exit /b 1
)
timeout 3

call mvn initialize wildfly:deploy-only %MVN_WILDFLY_ACCESS%
if [%ERRORLEVEL%]==[0] (
	@echo "Realizar testes? y/n"
	set /p TEST_NOW=
	if /I [!TEST_NOW!]==[Y] (
		cd %~dp0
		call test.bat
	)
	exit /b 0
)

ENDLOCAL
pause
exit /b 1