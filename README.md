# 🚗 Sistema de Parqueadero en Java

![Java](https://img.shields.io/badge/Java-17-blue)
![Architecture](https://img.shields.io/badge/Architecture-Modular__Layered-orange)
![Status](https://img.shields.io/badge/Status-Active-brightgreen)
![License](https://img.shields.io/badge/License-MIT-yellow)

## 📌 Descripción del Sistema

Este proyecto es un sistema de gestión de parqueadero desarrollado en **Java con arquitectura modular**. Permite registrar vehículos, controlar su ingreso y salida, calcular el costo del estacionamiento según el tipo de vehículo y el tiempo de permanencia, y almacenar un historial de movimientos.

El sistema funciona a través de la consola e incluye **persistencia de datos en archivos externos**, lo que permite conservar la información de manera segura incluso después de cerrar la aplicación.

---

## 📸 Capturas del Sistema

### Menú Principal

![menu](screenshots/menu.png)

### Registro de Vehículo

![registro](screenshots/registro.png)

### Historial de Movimientos

![historial](screenshots/historial.png)

---

## 🎞️ Demo del Sistema

A continuación se muestra el funcionamiento completo del sistema en consola (menú interactivo, ingreso y salida de vehículos con cálculo de tarifa en tiempo real):

![demo](screenshots/demo.gif)

---

## ⚙️ Funcionalidades

### 🚙 Gestión de Vehículos

* **Registro de ingreso:** Control de entrada de vehículos al sistema.
* **Validación de seguridad:** Evita el registro de placas duplicadas en el sistema activo.
* **Monitoreo:** Listado en tiempo real de los vehículos actualmente estacionados.

### 🏷️ Tipos de Vehículo

* **Carro**
* **Moto**

El tipo de vehículo se selecciona desde un menú interactivo en consola.

---

### 💰 Control de Tarifas

* **Cálculo automático:** Computa el costo total basado en las horas de permanencia.
* **Tarifas diferenciadas:**
  * **Moto:** $2.000 por hora.
  * **Carro:** $4.000 por hora.
* **Cobro mínimo:** 1 hora obligatoria.

---

### 🚪 Salida de Vehículos

* Registro exacto de la hora de salida.
* Generación automática de factura con el desglose del cobro.
* Liberación del vehículo del sistema activo.

---

### 📜 Historial de Movimientos

* Registro detallado de todas las entradas y salidas.
* Consulta del historial completo desde la aplicación.
* Persistencia automática en el archivo `historial.txt`.

---

### 💾 Persistencia de Datos

* Guardado automático del estado actual de los vehículos en `data/vehiculos.txt`.
* Guardado del historial en `data/historial.txt`.
* Los archivos se crean automáticamente fuera de `src` usando la ruta del sistema (`user.dir`).

---

## 🏗️ Estructura del Proyecto

```text
SistemaParqueadero/
│
├── src/
│   ├── model/
│   │   ├── Vehiculo.java
│   │   ├── Movimiento.java
│   │   └── TipoVehiculo.java
│   │
│   ├── service/
│   │   └── Parqueadero.java
│   │
│   ├── util/
│   │   ├── AppConfig.java
│   │   └── FileUtil.java
│   │
│   └── Main.java
│
├── data/
│   ├── vehiculos.txt
│   └── historial.txt
└── bin/
```

---

## 📊 Arquitectura y Flujo del Sistema

### Diagrama de Dependencias (UML de Flujo)

```text
Main (Vista/Menú Consola)
            │
            ▼
Parqueadero (Service - Lógica de Negocio)
│
├──► Vehiculo (Model)
├──► Movimiento (Model)
└──► TipoVehiculo (Enum)
            │
            ▼
FileUtil (Util - Persistencia en Archivos)
```

### Flujo de Trabajo

1. **Ingreso:** Registro de placa y tipo de vehículo. El sistema valida duplicados, lo guarda en memoria y lo escribe en el archivo activo.
2. **Permanencia:** Control estricto del tiempo transcurrido desde el ingreso usando `LocalDateTime`.
3. **Salida:** Cálculo de la duración con `Duration`, generación automática de la factura con el desglose del cobro y liberación del espacio.
4. **Historial:** Registro persistente y definitivo de todos los movimientos en el archivo correspondiente.

---

## ▶️ Cómo Ejecutar el Proyecto

1. **Compilar los archivos fuente:**
   javac -d bin src/model/*.java src/service/*.java src/util/*.java src/Main.java

2. **Ejecutar la aplicación:**
   java -cp bin Main

---

## 🛠️ Tecnologías Utilizadas

* **Lenguaje:** ☕ Java (JDK 8 o superior).
* **Manejo de Archivos:** Uso de `FileReader`, `FileWriter`, `BufferedReader` y `BufferedWriter`.
* **API de Tiempo:** Gestión temporal precisa mediante `LocalDateTime` y `Duration`.
* **Paradigma:** Programación Orientada a Objetos (POO).
* **Arquitectura:** Estructura limpia organizada por capas (`model` / `service` / `util`).

---

## 🚀 Mejoras Futuras

* [ ] Implementar ENUM para tipos de vehículo de forma estricta.
* [ ] Migración de persistencia a Base de Datos Relacional (MySQL).
* [ ] Generación y exportación de facturas en formato PDF.
* [ ] Implementación de una API REST con Spring Boot.
* [ ] Interfaz gráfica de usuario (JavaFX o React).
* [ ] Sistema de autenticación y control de roles.
* [ ] Módulo de reportes y estadísticas administrativas.

---

## 😎 Autor

Proyecto desarrollado por Jonathan Aristizabal como práctica de arquitectura de software en Java, aplicando conceptos de POO, separación por capas y persistencia estructurada en archivos.
