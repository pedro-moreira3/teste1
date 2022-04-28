# Criando um patch para o Wildfly

[Referencia](http://aparnachaudhary.me/2014/11/16/Patch-User-Defined-WildFly-Modules-in-Domain-Mode.html)

criar um patch para instalar o módulo do driver do postgres
entrar em cada pasta e checar o shasum de cada arquivo

`cat module.xml | shasum`
`cat postgresql-42.2.24.jar |shasum`

criar o "patch.xml" e adicionar o shasum em cada arquivo

    <?xml version="1.0" ?>

    <!-- Note the patch identifier, same as directory name -->
    <patch xmlns="urn:jboss:patch:1.0" id="driver-postgresql">
        <description>
            Este patch instala o driver do postgresql
        </description>
        <no-upgrade name="WildFly" version="24.0.1.Final"/>

        <!-- Custom defined modules shall be installed as misc files -->
        <misc-files>
          <!-- since we are installing the module for the first time, use added element. -->
          <added hash="4035818e75b14b8b116be8a0ef8f1009073e031e" path="modules/org/postgres/main/postgresql-42.2.24.jar"/>
          <added hash="7e72772c9896bc2c19620a6e32bbced9a7a2f6a9" path="modules/org/postgres/main/module.xml"/>
        </misc-files>

    </patch>

criar a estrutura dos arquivos 
patch.xml na raiz do arquivo compactado
adiconar "driver-postgresql/misc/modules..." - lembrando que "driver-postgresql"=campo id no patch.xml

    driver-postgresql
      \misc
        \modules
          \<subpastas aqui>     
    patch.xml	  


verifica o patch
`/opt/wildfly/bin/jboss-cli.sh --connect --command="patch inspect patch_driver_postgresql.zip"`

instala o patch
`/opt/wildfly/bin/jboss-cli.sh --connect --command="patch apply patch_driver_postgresql.zip --host=host1"`

Verifica o pacote
Obs: se não aparecer nada verificar no host apasta ".instalation/patches" dentro do wildfly
`/opt/wildfly/bin/jboss-cli.sh --connect --command="patch  history --host=host1"`

removendo o pacote
`/opt/wildfly/bin/jboss-cli.sh --connect --command="patch  rollback --host=host1 --patch-id=driver-postgresql --reset-configuration=false"`
`/opt/wildfly/bin/jboss-cli.sh --connect --command="patch  rollback --host=host1 --patch-id=modules-javax --reset-configuration=false"`

reinicia o servidor
`/opt/wildfly/bin/jboss-cli.sh --connect --command="/host=host1:shutdown(restart=true)"`

[Extra](https://superuser.com/questions/458326/sha1sum-for-a-directory-of-directories)
calcula os shasum para todos os arquivos nos subdirs

`find . -type f -print0  | xargs -0 sha1sum`