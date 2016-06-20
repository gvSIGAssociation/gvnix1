
Proof of Concept
================

* http://scmcit.gva.es/svn/gvnix-proof/trunk/petclinic-i18n

TODO
====

En el ejemplo petclinic, con el tema de la CIT instalado, no me aparece la bandera del cambio de idioma a Valenciano, sin embargo sí que me funciona accediendo directamente a la URL "http://localhost:8080/petclinic/?lang=ca".

    * web mvc install language --code ca
    * theme install --id cit
    * theme set --id cit

Se debe a que ejecuto la instalación del idioma antes de instalar y establecer el tema.

Si después de instalar y establecer el tema vuelvo a ejecutar la instalación del idioma, aparece la bandera correctamente. Es por ello que no lo considero un error, simplemente será dejar claro en la documentación del add-on que debe su ejecución debe ser posterior al instalar y establecer un nuevo tema.

Es un problema del tema. Roo pone los enlaces para el cambio de idioma en footer.jspx, el problema está en que los temas tb pueden incluir este fichero y sobreescriben el que ha modificado Roo.

La solución pasa por leer los idiomas instalados en el footer.jspx antes de sobreescribir, una vez sobreescrito actualizar el footer.jsp del tema con los idiomas leídos.
