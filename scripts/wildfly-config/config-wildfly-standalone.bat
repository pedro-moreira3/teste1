@echo off

rem Script para configuração automática de ambiente standalone
rem Gustavo Rossi 2021-11-18
rem Versões testadas: wildfly-24.0.1.Final

if not defined JBOSS_HOME (
	echo Variável de ambente JBOSS_HOME precisa estar configurada para diretório do wildfly para script funcionar corretamente
	pause
	exit /b
)

cd %JBOSS_HOME%\bin
echo "Adicionando usuário padrão"
call add-user.bat -u "desenv" -p "linux123" -g "admin"

echo "Inicializando Wildfly"
call start "wildfly" cmd /c standalone.bat

rem timeout para aguardar inicio de servidor
timeout 15

echo "Aplicando patch modules-lume-produtos"
call jboss-cli.bat --connect --command="patch apply %~dp0\\modules-lume-produtos.zip"

echo "Aplicando configurações dev-intelidente-standalone"
call jboss-cli.bat --file=%~dp0\\dev-intelidente-standalone.cli

echo "Finalizando Wildfly"
call jboss-cli.bat --connect --command=:shutdown

echo "Script finalizado"
pause