# ShipThis Go Android App

A modern Kotlin Android skeleton app following best practices from Now in Android, featuring:

## Architecture
- **MVVM (Model-View-ViewModel)** - Clean separation of concerns
- **Jetpack Compose** - Modern declarative UI toolkit
- **Hilt** - Dependency injection framework
- **Repository Pattern** - Data layer abstraction

## Key Technologies
- **Kotlin** - Primary programming language
- **Jetpack Compose** - UI framework
- **Coroutines** - Asynchronous programming
- **OkHttp** - HTTP client
- **Retrofit** - Type-safe HTTP client
- **Hilt** - Dependency injection
- **Navigation Compose** - Navigation component

## Project Structure
```
app/
├── src/main/java/com/shipthis/go/
│   ├── data/
│   │   ├── api/                    # API service interfaces
│   │   │   └── SampleApiService.kt
│   │   └── repository/             # Repository implementations
│   │       └── SampleRepository.kt
│   ├── di/                        # Dependency injection modules
│   │   └── NetworkModule.kt
│   ├── ui/
│   │   ├── navigation/             # Navigation setup
│   │   │   └── ShipThisGoNavigation.kt
│   │   ├── screens/                # Compose screens
│   │   │   └── home/
│   │   │       ├── HomeScreen.kt
│   │   │       └── HomeViewModel.kt
│   │   └── theme/                  # App theming
│   │       ├── Color.kt
│   │       ├── Theme.kt
│   │       └── Type.kt
│   ├── MainActivity.kt
│   └── ShipThisGoApplication.kt
```

## Getting Started

1. Open the project in Android Studio
2. Sync the project with Gradle files
3. Run the app on an emulator or device

## Features
- **Modern Material 3 design** with dynamic theming
- **Dark/Light theme support** with system preference detection
- **Network layer** with OkHttp and Retrofit
- **Coroutine-based async operations** with proper state management
- **Hilt dependency injection** for clean architecture
- **Compose navigation** with type-safe routing
- **MVVM architecture** with reactive UI updates
- **Repository pattern** for data abstraction

## Dependencies
- **Android SDK 24+** (Android 7.0+)
- **Kotlin 1.9.10** - Primary language
- **Compose BOM 2023.10.01** - UI framework
- **Hilt 2.48** - Dependency injection
- **OkHttp 4.12.0** - HTTP client
- **Retrofit 2.9.0** - Type-safe HTTP client
- **Navigation Compose 2.7.5** - Navigation
- **Coroutines 1.7.3** - Async programming

## Package Structure
- **Namespace**: `com.shipthis.go`
- **Application ID**: `com.shipthis.go`
- **App Name**: "ShipThis Go"
