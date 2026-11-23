# SmartNotesAI

**An AI-powered Android note-taking application built with Kotlin, Jetpack Compose, MVI Architecture, Clean Architecture, and Firebase AI Logic.**

SmartNotesAI is a modern Android application that combines intelligent note management with Generative AI capabilities. It allows users to create and manage notes, interact with an AI assistant, understand images using AI, and maintain persistent chat conversations.

The project demonstrates practical Android development skills, modern application architecture, multi-module design, and Generative AI integration.

## Features

### 1. Home
- Browse and manage notes.
- Access the note editor.
- Navigate between application features.
- Modern Jetpack Compose interface.

### 2. Notes Editor
- Create and edit notes.
- Organize note content.
- Persist notes using the local database.

### 3. AI Chat Assistant
- Generate AI-powered responses.
- Support multi-turn conversations.
- Stream AI responses.
- Persist conversation history and messages.
- Restore previous conversations.
- Continue existing conversations.

### 4. Image Understanding
- Select images from the device.
- Process images using Android image handling.
- Send images to the AI service.
- Receive AI-generated descriptions and insights.

## Technology Stack

| Category | Technologies |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVI, Clean Architecture |
| Dependency Injection | Hilt |
| Local Database | Room |
| Asynchronous Programming | Kotlin Coroutines, Flow |
| Generative AI | Firebase AI Logic, Gemini |
| Image Handling | Android URI, ContentResolver |
| Build System | Gradle, Kotlin DSL |
| Testing | JUnit, Kotlin Coroutines Test, Robolectric |

## Architecture

SmartNotesAI follows MVI and Clean Architecture principles with a multi-module project structure.

### Project Structure

```text
SmartNotesAI/
│
├── app/
│
├── domain/
│
├── data/
│
├── core/
│   ├── common/
│   ├── database/
│   └── designsystem/
│
└── feature/
    ├── home/
    ├── editor/
    └── chat/
```

### Architecture Responsibilities

- **App:** Application entry point and module integration.
- **Domain:** Business models, repository contracts, and use cases.
- **Data:** Repository implementations, data sources, and AI integration.
- **Core Common:** Shared application error and result models.
- **Core Database:** Room database, entities, and DAOs.
- **Core Design System:** Shared UI components and design resources.
- **Feature Home:** Home screen and note discovery.
- **Feature Editor:** Note creation and editing.
- **Feature Chat:** AI chat, streaming responses, image understanding, and conversation history.

### MVI Data Flow

```text
User Action
    ↓
ViewModel
    ↓
Use Case
    ↓
Repository
    ↓
Data Source
    ↓
Result
    ↓
UI State
    ↓
Compose UI
```

## AI Integration

SmartNotesAI integrates Firebase AI Logic to provide Generative AI capabilities.

Implemented capabilities include:

- Text generation.
- Streaming AI responses.
- Multi-turn AI conversations.
- Image understanding.
- Structured AI responses.
- Error handling and retry support.

## Database and Persistence

Room is used for local persistence.

The application stores:
- Notes and their content.
- Chat conversations.
- Chat messages.
- Conversation metadata.

Room database schemas are maintained in version control to support database migration management.

## Error Handling and Testing

The project includes:
- Centralized application error models.
- Generic result handling.
- Safe-call utility.
- AI-specific error mapping.
- Coroutine cancellation handling.
- Database failure handling.
- Image-processing error handling.
- ChatViewModel unit tests.

## Getting Started

### Prerequisites

- Android Studio.
- JDK compatible with the project configuration.
- Android SDK configured for the project.
- Firebase project with Firebase AI Logic configured.
- Gemini model access through the configured Firebase AI Logic setup.

### Installation

1. Clone the repository:

```bash
git clone https://github.com/Developer-Abhimanyu/SmartNotesAI.git
```

2. Open the project in Android Studio.

3. Configure your Firebase project and add your own `google-services.json` file to the app module.

4. Verify the Firebase AI Logic configuration.

5. Sync Gradle.

6. Build and run the application on an Android device or emulator.

### Build Commands

Build the debug application:

```bash
./gradlew assembleDebug
```

Run unit tests:

```bash
./gradlew test
```

Build the release application:

```bash
./gradlew assembleRelease
```

## GitHub Repository

[https://github.com/Developer-Abhimanyu/SmartNotesAI](https://github.com/Developer-Abhimanyu/SmartNotesAI)

## Future Enhancements

Potential future improvements include:

- Retrieval-Augmented Generation (RAG).
- On-device AI capabilities.
- Advanced AI-powered note search.
- AI-driven note summarization.
- Enhanced offline functionality.
- Agentic AI workflows.

## Author

**Abhimanyu Verma**

Android Application Developer | Kotlin | Jetpack Compose | MVI | Clean Architecture | Generative AI

GitHub: [Developer-Abhimanyu](https://github.com/Developer-Abhimanyu)

---

*SmartNotesAI — Bringing intelligent note-taking and Generative AI together in a modern Android application.*
