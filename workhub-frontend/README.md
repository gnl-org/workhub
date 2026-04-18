🚀 WorkHub Frontend
A lightweight, high-performance project management dashboard built with React, Vite, and Tailwind CSS v4. Designed for efficient development on 16GB RAM systems.

🛠️ Tech Stack
React 18 & Vite – Fast builds with a low memory footprint.
Tailwind CSS v4 – Modern utility-first styling with high specificity.
Lucide React – Clean, consistent iconography.
Axios – API client featuring JWT interceptors for session management.
JWT Auth – Integrated role-based access control (RBAC) for Admin and User views.

⚡ How to Run Locally
Install Dependencies
Bash
npm install

Start Development Server
Bash
npm run dev

Backend Configuration
Ensure the WorkHub Spring Boot backend is active on {URL}. The frontend automatically attaches JWT headers and handles ERR_CONNECTION_REFUSED states via global interceptors.

📂 Folder Structure
Plaintext
src/
├── api/          # Axios instance, interceptors, and network error handling
├── components/   # Sidebar, MainLayout, and reusable UI fragments
├── pages/        # Dashboard.jsx, Login.jsx, and view logic
├── util/         # auth.js (Lightweight JWT decoding & role extraction)
├── App.jsx       # Routing configuration (React Router v6)
├── index.css     # Tailwind v4 directives and global theme variables
└── main.jsx      # Application entry point