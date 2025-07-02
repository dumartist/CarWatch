# 🚗 CarWatch - Smart Vehicle Monitoring System

## Intelligent Vehicle Monitoring with Real-time Computer Vision and Secure Android Interface

**CarWatch** is a modern Android application designed for intelligent vehicle monitoring through computer vision integration, secure user authentication, and comprehensive activity tracking. Built with modern Android architecture patterns and Material Design, CarWatch offers real-time vehicle detection, weather integration, and detailed history management for smart parking and security applications.

---

## 📋 Table of Contents

* [🚗 Overview](#-overview)
* [✨ Key Features](#-key-features)
* [🛠️ Technical Architecture](#️-technical-architecture)
* [🚀 Installation & Setup](#-installation--setup)
* [⚙️ Configuration](#️-configuration)
* [🔗 API Integration](#-api-integration)
* [📊 Features In Detail](#-features-in-detail)
* [🛡️ Security Implementation](#️-security-implementation)
* [🐛 Troubleshooting](#-troubleshooting)
* [📄 License](#-license)

---

## 🚗 Overview

CarWatch is a sophisticated Android application that combines computer vision capabilities with a robust mobile interface for vehicle monitoring and management. The system integrates with a Flask backend API to provide real-time vehicle detection, secure user authentication, weather information, and comprehensive activity logging.

### **Key Technologies**
- **Frontend:** Android Java with MVVM Architecture
- **Backend:** Flask RESTful API with Computer Vision
- **Database:** MySQL for data persistence
- **Security:** Session-based authentication with persistent cookies
- **UI:** Material Design 3 Components with custom theming
- **Networking:** Retrofit2 + OkHttp3 with logging interceptors

---

## ✨ Key Features

### 🔐 **Secure Authentication System**
- User registration and login with comprehensive input validation
- Session-based authentication with persistent cookie management
- Secure password requirements and validation
- Account management including username/password updates and account deletion
- Automatic logout with server-side session clearing

### 🏠 **Smart Dashboard (Home Fragment)**
- Real-time vehicle detection status display
- Latest captured vehicle images with automatic refresh
- Personalized user greeting and session management
- Quick access navigation to other features
- Optimized image loading with Glide integration

### 📊 **Comprehensive History Management**
- Date-based activity filtering with Material Date Picker
- Timezone-aware data display (Asia/Jakarta conversion)
- RecyclerView with DiffUtil for optimal performance
- Search and filtering capabilities
- Real-time data updates with proper error handling

### 🌤️ **Weather Integration**
- Real-time weather data from OpenWeatherMap API
- City-based weather search with default Jakarta location
- Detailed weather metrics (temperature, humidity, wind speed)
- Dynamic weather condition display
- Secure API key management through BuildConfig

### 👤 **Advanced Account Management**
- Username and password update with validation
- Secure account deletion with password confirmation
- Material Design 3 dialog themes for consistent UX
- Profile information display and management
- Comprehensive error handling and user feedback

---

## 🛠️ Technical Architecture

### **Android Application Architecture**

```
CarWatch Android App (API 26-35)
├── 📱 UI Layer (MVVM)
│   ├── LoginFragment + LoginViewModel
│   ├── HomeFragment + HomeViewModel  
│   ├── HistoryFragment + HistoryViewModel
│   ├── WeatherFragment + WeatherViewModel
│   └── AccountFragment + AccountViewModel
├── 🌐 Network Layer
│   ├── RetrofitClient (HTTP + Cookie Management)
│   ├── ApiService (RESTful Endpoints)
│   └── ImageService (Image Fetching + Caching)
├── 💾 Data Layer
│   ├── Model Classes (LoginData, HistoryData, etc.)
│   ├── Response Models (ServerResponse, LoginResponse)
│   └── Persistent Cookie Storage
└── 🎨 UI Components
    ├── Material Design 3 Theming
    ├── Navigation Components
    └── Custom Dialog Themes
```

### **Core Dependencies & Versions**

#### **Build Configuration**
- **Compile SDK:** 35 (Android 14+)
- **Min SDK:** 26 (Android 8.0+)
- **Target SDK:** 35
- **Java Version:** 11
- **Gradle:** 8.9.3

#### **Key Dependencies**
```gradle
// Navigation & UI Framework
androidx.navigation:navigation-fragment:2.9.0
com.google.android.material:material:1.12.0
androidx.appcompat:appcompat:1.7.0

// Networking Stack
com.squareup.retrofit2:retrofit:2.9.0
com.squareup.retrofit2:converter-gson:2.9.0
com.squareup.okhttp3:okhttp:4.12.0
com.squareup.okhttp3:logging-interceptor:4.12.0

// Cookie & Session Management
com.github.franmontiel:PersistentCookieJar:v1.0.1

// Image Loading & Caching
com.github.bumptech.glide:glide:4.16.0

// Lifecycle Components
androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.0
androidx.lifecycle:lifecycle-livedata-ktx:2.9.0

// UI Components
androidx.recyclerview:recyclerview:1.4.0
de.hdodenhof:circleimageview:3.1.0
```

### **Backend Integration**
- **Framework:** Flask (Python) with RESTful API design
- **Authentication:** Session-based with secure cookie persistence
- **Database:** MySQL with proper connection pooling
- **Computer Vision:** OpenCV + YOLO for license plate recognition
- **Security:** Input sanitization, password hashing, SQL injection prevention

### **Network Configuration**
- **Development:** `http://10.0.2.2:8000` (Android Emulator)
- **Production:** `https://carwatch.xetf.my.id` or `flask.dumartist.my.id`
- **Timeout Settings:** Connect(15s), Read(30s), Write(15s)
- **Security:** Network Security Config with cleartext traffic support for development

---

## � Installation & Setup

### **Prerequisites**
- **Android Studio:** Arctic Fox or later
- **Android SDK:** API 26-35
- **Java Development Kit:** JDK 11 or later
- **Device/Emulator:** Android 8.0+ (API 26)

### **Backend Requirements**
- **Python:** 3.8+
- **Flask Framework** with dependencies
- **MySQL Server:** 5.7+ or 8.0+
- **OpenCV:** Computer vision processing
- **YOLO Models:** For license plate detection

### **Android Project Setup**

1. **Clone and Open Project**
   ```bash
   git clone <repository-url>
   cd carwatch
   ```
   Open the project in Android Studio

2. **Configure API Keys**
   Create `gradle.properties` in project root:
   ```properties
   OPENWEATHER_API_KEY=your_openweather_api_key_here
   ```

3. **Update Server Configuration**
   
   **For Development (Android Emulator):**
   ```java
   // RetrofitClient.java & ImageService.java
   private static final String BASE_URL = "http://10.0.2.2:8000";
   ```
   
   **For Production Deployment:**
   ```java
   // RetrofitClient.java & ImageService.java  
   private static final String BASE_URL = "https://carwatch.xetf.my.id";
   ```

4. **Build and Install**
   ```bash
   ./gradlew assembleDebug
   # Or use Android Studio Build > Make Project
   ```

### **Backend Setup**

1. **Install Python Dependencies**
   ```bash
   pip install flask mysql-connector-python opencv-python
   pip install numpy bleach requests pillow
   ```

2. **Database Configuration**
   ```sql
   CREATE DATABASE carwatch CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   
   CREATE TABLE users (
       user_id INT AUTO_INCREMENT PRIMARY KEY,
       username VARCHAR(50) UNIQUE NOT NULL,
       password VARCHAR(255) NOT NULL,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );
   
   CREATE TABLE history (
       id INT AUTO_INCREMENT PRIMARY KEY,
       plate VARCHAR(20),
       subject VARCHAR(100),
       description TEXT,
       date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       user_id INT,
       FOREIGN KEY (user_id) REFERENCES users(user_id)
   );
   ```

3. **Flask Server Configuration**
   ```python
   # config.py
   DATABASE_CONFIG = {
       'host': 'localhost',
       'user': 'your_mysql_user',
       'password': 'your_mysql_password', 
       'database': 'carwatch'
   }
   
   SECRET_KEY = 'your-secret-key-here'
   ```

4. **Launch Backend**
   ```bash
   python app.py
   # Server runs on http://localhost:8000
   ```

---

## ⚙️ Configuration

### **Network Security Configuration**
```xml
<!-- app/src/main/res/xml/network_security_config.xml -->
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">localhost</domain>
    </domain-config>
</network-security-config>
```

### **Required Permissions**
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

### **Application Configuration**
```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    android:usesCleartextTraffic="true"
    android:requestLegacyExternalStorage="true"
    android:theme="@style/AppTheme">
</application>
```

---

## 🔗 API Integration

### **Authentication Endpoints**
```java
// User Management
POST /auth/login          // User authentication
POST /auth/register       // User registration  
POST /auth/logout         // Session termination
POST /auth/update/username // Update username
POST /auth/update/password // Change password
POST /auth/delete         // Delete user account
```

### **Data Endpoints**
```java
// Vehicle & History
GET  /api/history         // Retrieve activity history
POST /api/upload_image    // Upload vehicle image for processing
GET  /api/get_image       // Fetch latest processed image
```

### **API Request/Response Format**
```java
// Standard Request Body
{
    "username": "user123",
    "password": "securepass",
    "newUsername": "newuser123"  // for updates
}

// Standard Response Format
{
    "success": true,
    "message": "Operation successful",
    "data": { /* response data */ }
}
```

### **Weather API Integration**
```java
// OpenWeatherMap API
GET https://api.openweathermap.org/data/2.5/weather
Parameters: q={city}&appid={API_KEY}&units=metric
```

---
## 📊 Features In Detail

### **🔐 Authentication System**
The app implements a robust authentication system with:
- **Session Management:** Persistent cookie storage using PersistentCookieJar
- **Input Validation:** Client-side validation with server-side verification
- **Security:** Secure password requirements and hashing
- **State Management:** LoginViewModel handles authentication state with LiveData

### **🏠 Home Dashboard**
Real-time monitoring interface featuring:
- **Image Display:** Latest vehicle detection images with Glide loading
- **Status Updates:** Real-time vehicle detection status
- **Navigation:** Quick access to all app features
- **Session Handling:** Automatic login state management

### **📈 History Management**
Comprehensive activity tracking with:
- **Date Filtering:** Material DatePicker with timezone conversion (Asia/Jakarta)
- **Performance:** RecyclerView with DiffUtil for smooth scrolling
- **Search:** Real-time filtering and search capabilities
- **Data Sync:** Automatic refresh and error handling

### **🌤️ Weather Integration**
Smart weather monitoring includes:
- **API Integration:** OpenWeatherMap with secure key management
- **Location Services:** Default Jakarta location with custom city search
- **Data Display:** Temperature, humidity, wind speed, and conditions
- **Error Handling:** Graceful failure handling and user feedback

### **👤 Account Management**
User profile management featuring:
- **Profile Updates:** Username and password modification
- **Security:** Password confirmation for critical operations
- **UI/UX:** Material Design 3 dialogs with proper themes
- **Validation:** Comprehensive input validation and error messaging

---

## 🛡️ Security Implementation

### **Client-Side Security**
- **Input Validation:** All user inputs validated before transmission
- **Session Management:** Secure cookie storage and automatic session handling
- **Network Security:** HTTPS enforcement for production, cleartext for development
- **Error Handling:** Secure error messages without sensitive information exposure

### **Network Security Configuration**
```xml
<!-- Allows cleartext traffic for development environments -->
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
    </domain-config>
</network-security-config>
```

### **API Security**
- **Authentication:** Session-based authentication with secure cookies
- **Authorization:** Server-side user session validation
- **Data Protection:** Encrypted password storage and secure data transmission
- **Input Sanitization:** Server-side input cleaning to prevent injection attacks

---

## 🐛 Troubleshooting

### **Common Connection Issues**

#### **1. Backend Connection Failed**
```
Error: Unable to connect to server
```
**Solutions:**
- Verify server IP configuration in `RetrofitClient.java` and `ImageService.java`
- For Android Emulator: Use `http://10.0.2.2:8000`
- For Physical Device: Use your computer's local IP address
- Check Flask server is running: `python app.py`

#### **2. Image Loading Failures**
```
Error: Failed to load image
```
**Solutions:**
- Verify image endpoint availability: `/api/get_image`
- Check network permissions in AndroidManifest.xml
- Ensure backend image processing service is running
- Clear app cache and storage

#### **3. Weather API Issues**
```
Error: Weather data unavailable
```
**Solutions:**
- Verify OpenWeatherMap API key in `gradle.properties`
- Check internet connectivity
- Validate API key permissions and quota
- Try different city names or coordinates

#### **4. Authentication Problems**
```
Error: Login failed or session expired
```
**Solutions:**
- Clear app data and cookies
- Verify backend database connectivity
- Check username/password validation rules
- Restart the Flask server

### **Debug Configuration**

#### **Enable HTTP Logging**
```java
// In RetrofitClient.java - Change logging level for debugging
HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // For full request/response
```

#### **Backend Debugging**
```python
# Enable Flask debug mode
app.run(debug=True, host='0.0.0.0', port=8000)

# Check database connectivity
mysql -u username -p carwatch
SHOW TABLES;
SELECT * FROM users;
```

#### **Network Debugging**
- Use Android Studio Network Inspector
- Monitor HTTP requests in Logcat with tag "OkHttp"
- Test API endpoints with Postman or curl
- Verify firewall and network security settings

### **Build Issues**

#### **Gradle Sync Problems**
```bash
# Clean and rebuild project
./gradlew clean
./gradlew build

# Check dependency versions in libs.versions.toml
# Ensure API keys are properly configured in gradle.properties
```

#### **API Key Configuration**
```properties
# gradle.properties (create if missing)
OPENWEATHER_API_KEY=your_actual_api_key_here
```

#### **Device Compatibility**
- **Minimum SDK:** Android 8.0 (API 26)
- **Target SDK:** Android 14 (API 35)
- **Required Permissions:** Internet, Network State, Storage
- **Hardware:** Camera access for future image capture features

---

## 📄 License

This project is licensed under the MIT License. See the LICENSE file for full license text.

---

## **Development Resources**
- **Android Documentation:** [developer.android.com](https://developer.android.com)
- **Material Design:** [material.io](https://material.io)
- **Retrofit Documentation:** [square.github.io/retrofit](https://square.github.io/retrofit/)
- **Flask Documentation:** [flask.palletsprojects.com](https://flask.palletsprojects.com)

---

**🚗 CarWatch** - *Making vehicle monitoring smart, simple, and secure*

*Built with ❤️ for smart home and security applications*

**Version:** 1.2.0 | **Last Updated:** July 2025 | **Platform:** Android 8.0+
