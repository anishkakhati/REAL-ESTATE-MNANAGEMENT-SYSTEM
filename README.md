# REAL-ESTATE-MNANAGEMENT-SYSTEM
This is my sem 3 java project 


# 📌 Real Estate Management System (Java + MySQL)

This project is a **console-based Real Estate Management System** implemented in **Java** using **JDBC (Java Database Connectivity)** and **MySQL**.  
It allows basic property, tenant, and rental management operations through a simple menu-driven interface.

This project is suitable for **college assignments, Java practice, or learning JDBC + MySQL integration**.

---

## 🚀 Features

### 🏠 Property Management
- Add new properties  
- View all properties  
- Update property details (address, type, price, availability)  
- Delete a property  
- Search properties using:
  - Property type  
  - Availability status  

### 👤 Tenant Management
- Add new tenants  
- View tenant information (via rentals)  

### 🔑 Rental Management
- Assign a tenant to a property  
- Mark property as occupied  
- Automatically records start date of rental  
- View all rental histories (including ongoing rentals)  

---

### **Property.java**
Represents a property with fields like:  
`propId`, `address`, `propType`, `price`, `available`.

### **Tenant.java**  
Represents a tenant with:  
`tenantId`, `name`, `contact`.

### **RealEstateSystem.java**  
Handles all **database operations** (CRUD) using JDBC:  
- Insert / update / delete  
- Search  
- Join queries  
- Transaction handling for assignments  

### **RealEstateSystemApp.java**  
Provides a **menu-based console interface** for the user.

---

## 🔧 Technologies Used
- **Java (Core Java, OOP)**
- **JDBC**
- **MySQL Database**
- **SQL (CRUD + Joins + Transactions)**

---


## ✅ What This Project Demonstrates

- Practical understanding of **Object-Oriented Programming**
- Ability to use **JDBC** to interact with a real database
- Working knowledge of **CRUD operations**
- Usage of **SQL joins**, **transactions**, and **prepared statements**
- A complete **console-based management system**

This makes the project perfect for:

✔ **Academic submissions**  
✔ **Portfolio projects**  
✔ **JDBC practice**  
✔ **Beginners learning database connectivity in Java**



## 📝 Summary

The Real Estate Management System is a simple yet effective Java–MySQL project designed to demonstrate core programming concepts such as OOP, JDBC connectivity, SQL operations, and console-based application design. It provides essential features for managing properties, tenants, and rentals while maintaining clean code structure and practical database interactions. This project serves as a solid foundation for beginners and can be expanded into a more advanced real estate platform in the future.

