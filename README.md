# Proyecto_DesarrolloDeAplicacionesWeb
GlaciarGestion
Aplicación web para gestión de pedidos de hielo, con dos perfiles:
•	Cliente
•	Administrador
Incluye catálogo, carrito, pedidos y un módulo de consultas tipo chat cliente-admin con creación manual de pedidos desde administración.
1. Instrucciones de instalación
1.1. Clonar el proyecto
git clone https://github.com/matbarr/Proyecto_DesarrolloDeAplicacionesWeb.git
cd Proyecto_GlaciarGestion
1.2. Requisitos previos
•	Java 17
•	Maven 3.9+
•	MySQL 8+
Verificar versiones:
java -version
mvn -version
1.3. Crear base de datos
En MySQL
2. Configuración
Este proyecto usa principalmente:
•	application.properties
•	application.properties
Asegúrate de tener estos valores (o adaptarlos a tu entorno):
spring.datasource.url=jdbc:mysql://localhost:3306/glaciar_gestion?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=Matias102007
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
server.port=8090

Notas:
1.	Si cambias el puerto, actualiza la URL de acceso.
2.	Si trabajas con múltiples archivos de propiedades, mantén credenciales sincronizadas para evitar inconsistencias.
3. Ejecución
3.1. Compilar
mvn clean install
3.2. Ejecutar
mvn spring-boot:run

3.3. Abrir en navegador
•	http://localhost:8090
3.4. Usuarios de prueba
El sistema inicializa datos automáticos al arrancar (si no existen), desde:
•	DataInitializer.java
Usuario administrador de prueba:
•	Correo: admin@glaciar.com
•	Contraseña: admin123
•	Rol: ADMINISTRADOR
Cliente de prueba:
•	No viene fijo por defecto.
•	Puedes crearlo desde la pantalla de registro:
•	/registro
4. Descripción de módulos
4.1. Autenticación
•	Registro de cliente
•	Inicio de sesión
•	Cierre de sesión
•	Redirección según rol (admin/cliente)
Archivos clave:
•	AuthController.java
•	AuthService.java
4.2. Módulo Cliente
•	Inicio (landing interna)
•	Catálogo de productos
•	Detalle de producto
•	Carrito (agregar/actualizar/eliminar)
•	Registro de direcciones
•	Confirmación de pedido
•	Historial y estado de pedidos
•	Consultas tipo chat con administración
Archivos clave:
•	ClienteController.java
•	cliente
4.3. Módulo Administrador
•	Gestión de productos (crear/editar/desactivar)
•	Gestión de pedidos (listar/detalle/cambio de estado)
•	Bandeja de consultas con filtros
•	Respuesta de mensajes
•	Cierre/reapertura de consultas
•	Creación manual de pedidos desde una consulta
Archivos clave:
•	AdminController.java
•	admin
4.4. Módulo de Consultas (Chat Cliente-Admin)
•	Cliente crea consulta con asunto + mensaje inicial
•	Intercambio de mensajes entre cliente y admin
•	Marcado de no leídos por rol
•	Consulta abierta/cerrada
•	Filtros en admin por cliente, estado y fechas
•	Pedido manual desde chat (admin)
Archivos clave:
•	ConsultaService.java
•	Consulta.java
•	MensajeConsulta.java
5. Persistencia de datos
•	Spring Data JPA + Hibernate
•	MySQL como base principal
•	Entidades principales:
•	Usuario, Producto, Carrito, DetalleCarrito, DireccionEntrega, Pedido, DetallePedido, Consulta, MensajeConsulta
Carpetas clave:
•	domain
•	repository
•	service
6. Comandos útiles
mvn test
mvn clean package

