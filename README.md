# CarWatch - Smart Vehicle Monitoring System

## 🚗 Overview

CarWatch is an intelligent Android application that combines real-time license plate recognition with comprehensive vehicle tracking capabilities. The app seamlessly integrates with a Flask backend to provide automated vehicle detection, weather monitoring, and detailed activity logging for smart home and parking management systems.

## ✨ Key Features

### 🔐 **User Authentication & Account Management**
- Secure user registration and login system
- Session-based authentication with persistent login state
- Complete account management (username/password updates, account deletion)
- Secure logout functionality with server-side session clearing

### 🏠 **Smart Home Dashboard**
- Real-time vehicle detection status display
- Latest license plate recognition results with timestamps
- Personalized greeting with username display
- Quick overview of recent vehicle activity
- Garage lamp control integration (IoT-ready)

### 📱 **Advanced License Plate Recognition**
- Automatic license plate detection using OpenCV
- YOLO-based OCR character recognition system
- Real-time image upload and processing
- Vehicle entry/exit status tracking
- Automatic history logging with timestamps

### 📊 **Comprehensive Activity History**
- Detailed vehicle activity logging with precise timestamps
- Advanced date-based filtering system (Asia/Jakarta timezone)
- Search functionality by specific dates
- Complete activity timeline with vehicle details
- Smart date formatting and timezone conversion

### 🌤️ **Weather Integration**
- Real-time weather data from OpenWeatherMap API
- City-based weather search functionality
- Detailed weather information (temperature, humidity, wind speed)
- Weather condition descriptions and dynamic icons
- Default location support (Jakarta) with custom city input

## 🛠️ **Technical Architecture**

### **Frontend (Android)**
- **Architecture**: MVVM (Model-View-ViewModel) pattern
- **UI Framework**: Material Design Components with ViewBinding
- **Programming Language**: Java
- **Navigation**: Fragment-based with BottomNavigationView
- **Data Management**: SharedPreferences for session storage
- **Reactive Programming**: LiveData and Observer patterns

### **Backend (Flask)**
- **Framework**: Flask (Python)
- **Database**: MySQL with connection pooling
- **Authentication**: Session-based with secure cookie management
- **Security**: Password hashing, input sanitization with Bleach
- **Computer Vision**: OpenCV + YOLO for license plate OCR
- **File Handling**: Temporary image storage and processing

### **API Integration**
- **Weather Service**: OpenWeatherMap API
- **Network Layer**: Retrofit2 with OkHttp3
- **Cookie Management**: JavaNetCookieJar for session persistence
- **Error Handling**: Comprehensive error responses and logging

## 📱 **Core Components**

### **ViewModels**
- `LoginViewModel` - Handles authentication logic
- `HomeViewModel` - Manages dashboard data and user sessions
- `HistoryViewModel` - Controls activity filtering and data processing
- `WeatherViewModel` - Manages weather API calls and data
- `AccountViewModel` - Handles user account operations

### **Fragments**
- `LoginFragment` - User authentication interface
- `HomeFragment` - Main dashboard with vehicle status
- `HistoryFragment` - Activity timeline with date filtering
- `WeatherFragment` - Weather information display
- `AccountFragment` - User account management

### **Models**
- `LoginData/LoginResponse` - Authentication data structures
- `HistoryData/HistoryResponse` - Activity logging models
- `ServerResponse` - Standardized API response format

## 🚀 **Installation & Setup**

### **Prerequisites**
- Android Studio (latest version)
- Android SDK (API level 21+)
- Python 3.8+
- MySQL Server
- OpenCV Python package
- YOLO model files

### **Backend Setup**
1. Clone the repository
2. Install Python dependencies:
   ```bash
   pip install flask mysql-connector-python opencv-python numpy bleach
   ```
3. Configure database connection in `config.py`
4. Set up MySQL database schema:
   ```sql
   CREATE DATABASE carwatch;
   CREATE TABLE users (user_id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50), password VARCHAR(255));
   CREATE TABLE history (id INT AUTO_INCREMENT PRIMARY KEY, plate VARCHAR(20), subject VARCHAR(100), description TEXT, date TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
   ```
5. Run the Flask server:
   ```bash
   python routes.py
   ```

### **Android Setup**
1. Open project in Android Studio
2. Update `RetrofitClient.java` with your server IP:
   ```java
   private static final String BASE_URL = "http://YOUR_SERVER_IP:8000";
   ```
3. Add OpenWeatherMap API key in `WeatherViewModel.java`:
   ```java
   private static final String API_KEY = "YOUR_API_KEY";
   ```
4. Build and install the APK

### **Required Permissions**
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## 🔧 **API Endpoints**

### **Authentication**
- `POST /register` - User registration
- `POST /login` - User authentication
- `POST /logout` - Session termination

### **User Management**
- `GET /user/me` - Get current user info
- `POST /user/update/username` - Update username
- `POST /user/update/password` - Change password
- `POST /user/delete` - Delete user account

### **Vehicle Monitoring**
- `POST /upload_image` - Upload vehicle image for OCR
- `POST /history/plate` - Record license plate manually
- `GET /history/get` - Retrieve activity history

## 🎯 **Use Cases**

### **Home Security**
- Automatic vehicle detection and logging
- Real-time notifications of vehicle activity
- Historical tracking for security analysis

### **Parking Management**
- Entry/exit tracking with timestamps
- License plate recognition for access control
- Activity reports and analytics

### **Smart Home Integration**
- IoT device control (garage lamps, gates)
- Weather-based automation
- User presence detection

## 📊 **Features In Detail**

### **License Plate Recognition Process**
1. Image capture via mobile app
2. OpenCV preprocessing and plate detection
3. YOLO-based character recognition
4. Automatic database logging
5. Real-time status updates

### **History Management**
- Timezone-aware timestamp conversion (Asia/Jakarta)
- Date-based filtering with intuitive UI
- Export capabilities (future enhancement)
- Search and sort functionality

### **Security Features**
- Password hashing with secure algorithms
- Input sanitization to prevent SQL injection
- Session-based authentication
- Secure API communication with proper error handling

## 🛡️ **Security Considerations**

- All user inputs are sanitized using Bleach library
- Passwords are hashed using secure algorithms
- Session management prevents unauthorized access
- API endpoints validate user authentication
- Database queries use parameterized statements

## 📈 **Future Enhancements**

- Real-time push notifications
- Multiple camera support
- Advanced analytics dashboard
- Cloud storage integration
- Mobile app for iOS
- API rate limiting and caching
- Machine learning improvements for OCR accuracy

## 🐛 **Troubleshooting**

### **Common Issues**
1. **Database Connection Error**: Verify MySQL server status and credentials
2. **OCR Not Working**: Check YOLO model files and OpenCV installation
3. **API Connection Failed**: Confirm server IP and port configuration
4. **Login Issues**: Verify session management and cookie configuration

### **Debug Mode**
Enable HTTP logging in `RetrofitClient.java`:
```java
loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
```

## 📄 **License**

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 **Contributing**

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📞 **Support**

For support and questions:
- Open an issue on GitHub
- Contact the development team
- Check the documentation

## 🙏 **Acknowledgments**

- OpenWeatherMap for weather API services
- OpenCV community for computer vision tools
- YOLO developers for object detection models
- Material Design team for UI components

---

**CarWatch** - Making vehicle monitoring smart, simple, and secure. 🚗📱

*Built with ❤️ for smart home and security applications*
