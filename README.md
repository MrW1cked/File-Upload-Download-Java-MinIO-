# File Upload and Download Project

## Overview
This project is a Spring Boot application that provides RESTful APIs for uploading and downloading PDF files. The application uses MinIO for storing files and allows users to upload files with a maximum size of 25MB.

## Features
- Upload PDF files (max size 25MB)
- Download files by their ID
- Retrieve a list of files uploaded by a specific user

## Technologies Used
- Spring Boot
- Spring Web
- MinIO for storage
- Lombok
- Java

## Project Structure

The HTTP Requests are on the Controller Folders. 
Each Method calls a Method into the Service class.

## Prerequisites
- Java 11 or higher
- Maven
- MinIO server running and configured

## Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/file-upload-download.git
cd file-upload-download

### 2. Configure MinIO

