# PlayLore Backend — Spring Boot API

El backend que sirve para hacer llamadas desde el frontend.
Este README describe cómo instalar y desplegar el proyecto en un entorno de desarrollo (`dev`).

---

## Requisitos previos

Antes de ejecutar el proyecto, asegúrate de tener instaladas las siguientes herramientas:

- **Java JDK 24+**
- **Maven 3.6+**
- **MySQL 8+**
- **Git**
- IDE recomendado: **Eclipse**
- Extensión recomendada para Eclipse: **Spring Tools 4**

---

## Configuración del proyecto

### Paso 1 — Clonar el repositorio

Abre una terminal y ejecuta los siguientes comandos para descargar el proyecto en tu máquina:

```bash
git clone https://github.com/usuario/playlore-backend.git
cd playlore-backend
```

> ⚠️ **Nota:** Actualmente la API no está disponible; solo están los modelos que corresponden a las tablas de la versión actual del modelo ER. Está prevista su modificación próximamente.

### Paso 2 — Importar y actualizar el proyecto en Eclipse

Una vez clonado el repositorio, importa el proyecto en Eclipse como proyecto Maven existente. Después, para asegurarte de que Eclipse descarga todas las dependencias correctamente, haz clic derecho sobre el proyecto en el explorador y selecciona:

**Maven → Update Project...**

En el diálogo que aparece, comprueba que tu proyecto está marcado y pulsa **OK**. Esto sincronizará las dependencias y resolverá posibles errores de importación.

### Paso 3 — Configurar la base de datos

Navega hasta el fichero de configuración de la aplicación en:
src/main/resources/application.properties

Y edita las siguientes propiedades con los datos de tu instalación local de MySQL:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/NOMBRE_BD
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_CONTRASEÑA
```

Antes de arrancar la aplicación necesitas crear las tablas en la base de datos. Para ello ejecuta el siguiente script SQL:

📄 [Script de creación de tablas](https://drive.google.com/file/d/1SJOrOLjz6ekFqb2XkORvxD4oUAERMY5d/view?usp=sharing)

Puedes ejecutarlo desde MySQL Workbench, DBeaver, o directamente desde la consola de MySQL.

### Paso 4 — Arrancar la aplicación

Con la base de datos configurada y las tablas creadas, ya puedes iniciar el servidor. Haz clic derecho sobre el proyecto en Eclipse y selecciona:

**Run As → Spring Boot App**

La aplicación se iniciará y estará disponible por defecto en `http://localhost:8080`. Puedes verificarlo revisando la consola de Eclipse, donde debería aparecer el mensaje de arranque de Spring Boot.
