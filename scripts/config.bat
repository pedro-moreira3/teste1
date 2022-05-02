@echo off

rem Script base de configuração de variáveis para todos os outros scripts do projeto

rem Diretório base do projeto
set PROJECT_HOME=%~dp0\..
rem Nome do artefato gerado por esse projeto (mesmo definido em pom.xml)
set PROJECT_NAME=intelidente

rem Host do servidor wildfly
set SERVER_HOST=192.168.102.70
rem Usuario de acesso ao servidor wildfly
set SERVER_USERN=desenv
rem Senha de acesso ao servidor wildfly
set SERVER_PASSW=linux123
rem Server group do wildfly em modo cluster, vazio se em modo standalone
set SERVER_GROUP=
rem Url de acesso automatizado pelos scripts
set SERVER_URL=https://dev-intelidente.lumetec.com.br/

rem Driver chrome para realizar testes selenium
set SELENIUM_CHROME_DRIVER=C:\\web-drivers\\chromedriver.exe

rem Descomentar para automatizar inputs em outros scripts 
rem set DEPLOY_LOCAL=y

rem Descomentar para sobrescrever variável de ambiente
rem set JBOSS_HOME=C:\\wildfly-24.0.1.Final