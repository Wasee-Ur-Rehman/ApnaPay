# ApnaPay - Modern Fintech Application

ApnaPay is a comprehensive Android-based financial application designed to provide users with a seamless experience for managing digital payments, tracking transactions, and handling personal finances. The app features a modern UI/UX inspired by current fintech trends, utilizing Material Design components.

## 🚀 Features

### 🔐 Authentication & Security
*   **Onboarding:** Welcoming splash and introductory screens.
*   **User Auth:** Dedicated Login and Sign-Up flows.
*   **Password Recovery:** Integrated "Forgot Password" workflow with OTP (One-Time Password) verification.
*   **Secure Navigation:** Controlled activity transitions and intent-based security.

### 💳 Financial Management
*   **Dashboard:** A central hub showing account overview and quick actions.
*   **Send & Receive:** Interface for transferring money between accounts.
*   **Load Money:** Capability to add funds to the digital wallet.
*   **Transaction History:** Detailed logs of all financial activities.
*   **Card Management:** View and manage linked bank cards.

### 🛠 Utility & Tools
*   **QR Scanner:** Integrated barcode and QR code scanning for quick payments (via ZXing).
*   **Statistics:** Visual insights into spending habits and financial trends.
*   **Profile Management:** User account settings and personal information management.

## 🛠 Tech Stack

*   **Language:** Java
*   **Platform:** Android (Min SDK 24, Target SDK 36)
*   **UI Framework:** XML with Material Design Components
*   **Architecture:** Activity-Fragment based architecture
*   **Libraries:**
    *   `Material Components`: For modern UI elements.
    *   `ZXing Android Embedded`: For QR and Barcode scanning.
    *   `ConstraintLayout`: For responsive and complex layouts.

## 📂 Project Structure

```text
com.example.apnapay
├── Activities
│   ├── SplashActivity          # Application entry point
│   ├── MainActivity            # Welcome/Get Started screen
│   ├── AuthActivity            # Selection between Login/Sign-up
│   ├── LoginActivity           # User login credentials handling
│   ├── SignUpActivity          # New user registration
│   ├── ForgotPasswordActivity  # Password recovery initiation
│   ├── VerifyOTPActivity       # OTP verification logic
│   ├── DashboardActivity       # Main container with Bottom Navigation
│   ├── SendMoneyActivity       # Funds transfer interface
│   ├── LoadMoney               # Wallet top-up interface
│   └── TransactionHistory      # Detailed list of past activities
│
├── Fragments (Dashboard Modules)
│   ├── homeFragment            # Dashboard overview and quick links
│   ├── MyCardsFragment         # Card management UI
│   ├── ScanFragment            # QR scanning interface
│   ├── StatistcsFragment       # Analytics and spending graphs
│   └── AccountFragment         # User profile and settings
│
└── Layouts (res/layout)
    ├── activity_*.xml          # UI definitions for all screens
    ├── fragment_*.xml          # UI for dashboard modules
    └── item_*.xml              # Reusable list/card components
```

## 🛤 Navigation Flow

1.  **Launch:** `SplashActivity` ➔ `MainActivity`.
2.  **Onboarding:** `MainActivity` ➔ `AuthActivity`.
3.  **Authentication:** `AuthActivity` ➔ `LoginActivity` or `SignUpActivity`.
4.  **Recovery:** `LoginActivity` ➔ `ForgotPasswordActivity` ➔ `VerifyOTPActivity`.
5.  **Main App:** Successful login leads to `DashboardActivity`.
6.  **Dashboard:** Uses a `BottomNavigationView` to switch between `Home`, `Cards`, `Scan`, `Statistics`, and `Account`.
7.  **Transactions:** From `Home`, users can navigate to `SendMoney`, `LoadMoney`, or `TransactionHistory`.

## ⚙️ Setup Instructions

1.  **Clone the Repository:**
    ```bash
    git clone https://github.com/your-username/ApnaPay.git
    ```
2.  **Open in Android Studio:**
    *   File > Open > Select the `ApnaPay` folder.
3.  **Gradle Sync:**
    *   Wait for the project to sync and download dependencies (Material components and ZXing).
4.  **Run:**
    *   Select an emulator or physical device (API 24 or higher).
    *   Click the "Run" button.

## 📝 Assumptions & Notes
*   **Backend:** Currently, the app functions as a high-fidelity UI prototype. Integration with a backend (like Firebase or a custom API) is assumed for future development to handle real-time authentication and data.
*   **Data Persistence:** Views currently use placeholder data; integration with Room DB or a remote database is expected for production use.
*   **Navigation:** Navigation is handled via explicit Intents and FragmentManager transactions.
