# MPAZ Reservas

Repositorio del proyecto desarrollado para el **Colegio Marcela Paz**.

MPAZ Reservas es una aplicación Android que permite gestionar la reserva de los espacios institucionales del colegio (laboratorios, biblioteca, sala sensorial, huerto, etc.). El personal solicita el uso de un recinto y la Inspectoría revisa, aprueba o rechaza cada solicitud, dejando un registro claro de quién hizo cada acción.

---

## Roles de usuario

| Rol | Descripción | Qué puede hacer |
|---|---|---|
| **Personal Autorizado** (cliente) | Docentes, UTP, Dirección y profesionales PIE o de apoyo | Crear solicitudes de reserva, ver y cancelar sus reservas, consultar el catálogo de espacios, agendar en su calendario y tomar fotos de evidencia |
| **Inspectoría** (administrador) | Encargados de la gestión de los recintos | Aprobar, rechazar o cancelar reservas, gestionar el estado de los espacios, administrar usuarios, revisar el historial de auditoría y registrar evidencia fotográfica |

El acceso se realiza con correo institucional (`@colegiomariapaz.cl`) y contraseña.

---

## Funcionalidades

### Reservas
- Solicitud de reserva indicando espacio, fecha, bloque horario, actividad específica, curso o grupo, cantidad de participantes, motivo y observaciones.
- Horarios basados en los **bloques pedagógicos de 45 minutos** del colegio.
- Validaciones automáticas:
  - La reserva debe solicitarse con **al menos 24 horas de anticipación**.
  - La cantidad de participantes no puede superar la **capacidad máxima** del espacio.
  - No se permite reservar un espacio que ya está reservado en el mismo horario.
- Estados de la reserva: **Pendiente, Aprobada, Rechazada, Finalizada y Cancelada**.
- Cada reserva recibe un número identificador, por ejemplo `RES-8156`.
- Rechazar o cancelar desde el panel de Inspectoría exige ingresar una **justificación**.
- El solicitante puede cancelar sus reservas pendientes o aprobadas, con confirmación previa.

### Catálogo de espacios
Consulta de descripción, ubicación, capacidad y estado de cada recinto: Laboratorio de Computación, Laboratorio de Ciencias, Biblioteca, Sala Sensorial, Centro de Innovación CIEMP, Sala de Reuniones / Capacitación y Huerto Escolar. Inspectoría puede cambiar el estado de cada espacio (Disponible, Ocupado, En Mantenimiento, Fuera de Servicio).

### Panel de Inspectoría
- Listado de todas las solicitudes, mostrando quién generó cada una (por ejemplo: `RES-8156 - Inspector General`).
- Cambio de estado de reservas con confirmación.
- Gestión de usuarios institucionales: creación y eliminación.
- **Historial de auditoría** con registro cronológico de las acciones (inicios de sesión, reservas creadas, cambios de estado, evidencias capturadas, etc.).

### Notificaciones
Notificaciones dentro de la app: Inspectoría recibe aviso de cada nueva solicitud y el solicitante recibe aviso cuando su reserva es aprobada, rechazada o cancelada, incluyendo el motivo si corresponde.

---

## Funciones nativas del dispositivo

### Cámara: evidencia del estado del espacio
Permite tomar una fotografía para dejar constancia de **cómo se encuentra el espacio antes y después de entregarlo**. La app solicita el permiso de cámara en tiempo de ejecución y maneja los casos en que el permiso es denegado o el dispositivo no tiene una app de cámara. La captura queda registrada en el historial de auditoría.

### Calendario: importar la reserva
Las reservas aprobadas pueden **agregarse al calendario del dispositivo** con un solo botón. La app abre el calendario nativo del usuario con el evento ya completado (espacio, actividad, motivo, fecha y horario).

---

## Persistencia de datos

Las solicitudes de reserva se almacenan localmente en una base de datos **Room (SQLite)**, por lo que se conservan al cerrar y volver a abrir la aplicación.

---

## Tecnologías

- Kotlin
- Jetpack Compose y Material 3
- Navigation Compose
- Arquitectura MVVM con `ViewModel` y `StateFlow`
- Room para la persistencia local
- API nativas de Android: cámara (`TakePicturePreview`), permisos en tiempo de ejecución y `CalendarContract`

---

## Estructura del proyecto

```
com.example.mpaz_tickets
├── data      → Entidades Room, DAO, base de datos y repositorio
├── model     → Modelos de dominio y ReservaViewModel
└── ui        → Pantallas en Compose
```

---

## Requisitos y ejecución

1. Clonar el repositorio y abrirlo con Android Studio.
2. Verificar que exista el archivo `local.properties` con la ruta del SDK (Android Studio lo genera al abrir el proyecto).
3. Sincronizar Gradle y ejecutar la app.
4. Para probar la cámara y el calendario, usar un emulador con imagen **Google APIs** o **Play Store**, o un dispositivo físico.

### Usuarios de prueba

| Correo | Contraseña | Rol |
|---|---|---|
| inspector@colegiomariapaz.cl | 123456 | Inspectoría |
| juan@colegiomariapaz.cl | 123456 | Personal Autorizado |
| ana@colegiomariapaz.cl | 123456 | Personal Autorizado |

### Flujo de prueba sugerido
1. Entrar como personal autorizado y crear una reserva para mañana o una fecha posterior.
2. Entrar como Inspectoría y aprobar la reserva desde el panel.
3. Volver al usuario solicitante: en **Reservas** aparecerán las opciones **Agendar** (calendario) y **Cámara** (evidencia).
