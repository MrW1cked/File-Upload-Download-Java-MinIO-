# 📦File Upload and Download Project

## 👀Overview
This project is a Spring Boot application that provides RESTful APIs for uploading and downloading PDF files. The application uses MinIO for storing files and allows users to upload files with a maximum size of 25MB.

## 👑Features
- Upload PDF files (max size 25MB)
- Download files by their ID
- Retrieve a list of files uploaded by a specific user

## 🥷Technologies Used
- Spring Boot
- Spring Web
- MinIO for storage
- Lombok
- Java

## ⛩️Project Structure

The HTTP Requests are on the Controller Folders. 
Each Method calls a Method into the Service class.

## ⛺️Prerequisites
- Java 11 or higher
- Maven
- MinIO server running and configured

## ⚙️Setup Instructions

### 1️⃣. Clone the Repository
```bash
git clone https://github.com/your-username/file-upload-download.git
cd file-upload-download
```
### 2️⃣. Configure MinIO

Use the file .env to store this variables:
```bash
minio.access.name=your-access-key
minio.access.secret=your-secret-key
minio.bucket.name=your-bucket-name
minio.url=http://localhost:9000
```

### 3️⃣. Build the Project
```bash
mvn clean install
```

### 44️⃣. Run the Application
```bash
mvn spring-boot:run
```

### 5️⃣. Access the Application:

Open a web browser and go to http://localhost:9998

## 📜License

This project is licensed under the MIT License. See the LICENSE file for details.

## 📪Contact
If you have any questions or suggestions, please feel free to contact me at nunofvsousa@gmail.com
