# Free Online Publishing Plan: GitHub Releases & GitHub Pages

Step-by-step architectural guide and automated deployment workflow for publishing NearGrid as a free open-source Android app on GitHub Releases with a free hosted landing page on GitHub Pages.

## User Review & Critical Decisions

> [!IMPORTANT]
> **Confirmed Choices from Phase 1:**
> - **APK Hosting Strategy**: GitHub Releases (100% free, unlimited downloads, automatic versioning).
> - **Web Showcase**: GitHub Pages landing page for web users to preview features and download the `.apk` directly onto Android devices.

---

## 1. Overview & Core Concept

This plan provides a step-by-step workflow to export your NearGrid project, publish executable release builds (`.apk` / `.aab`) on GitHub Releases for free, and host a responsive HTML/CSS landing page on GitHub Pages with one-click APK download links.

---

## 2. Publishing Workflow & User Journey

### A. Exchanging & Exporting Source Code
1. Click **Export / Push to GitHub** in the AI Studio top toolbar.
2. Link your GitHub account and select **Create New Repository** named `NearGrid-Android`.

### B. Building Release APKs in AI Studio
1. In AI Studio, open **Settings / Build Settings**.
2. Trigger **Generate Release APK** to produce `NearGrid-v1.0.apk`.
3. Download the signed `.apk` file to your local computer.

### C. Hosting APK on GitHub Releases
1. Navigate to your repository on GitHub: `https://github.com/<your-username>/NearGrid-Android`.
2. Click **Releases** > **Draft a new release**.
3. Tag the release (e.g. `v1.0.0`) and name it **NearGrid v1.0.0 Initial Release**.
4. Drag and drop `NearGrid-v1.0.apk` into the release binaries drop zone.
5. Click **Publish release**. Your APK will now have a permanent public direct link: `https://github.com/<your-username>/NearGrid-Android/releases/download/v1.0.0/NearGrid-v1.0.apk`.

### D. Hosting Free Landing Page on GitHub Pages
1. In the repository root, create a `docs/index.html` landing page showcasing NearGrid's features, screenshots, and direct `Download APK` button.
2. Go to **Repository Settings > Pages**.
3. Under **Build and deployment**, select **Source: Deploy from a branch**, Branch: `main`, Folder: `/docs`.
4. Click **Save**. Within 1–2 minutes, your website will be live for free at: `https://<your-username>.github.io/NearGrid-Android`.

---

## 3. Key Decisions & Trade-Offs

- **GitHub Releases vs Google Play Store**:
  - *Chosen*: GitHub Releases (100% free, no developer account fee, no review delays).
  - *Trade-off*: Users must tap "Install from Unknown Sources" on Android when opening the downloaded `.apk`.

- **GitHub Pages vs Vercel / Netlify**:
  - *Chosen*: GitHub Pages (`/docs` folder approach).
  - *Why*: Built natively into your GitHub repository with zero configuration or external accounts required.

---

## 4. Architecture & Workflow Diagram

```
┌────────────────────────────────────────────────────────┐
│               Google AI Studio Environment              │
│  - Jetpack Compose App                                 │
│  - Export repository -> GitHub                         │
│  - Generate Release APK                                │
└─────────────────────────┬──────────────────────────────┘
                          │
                          ▼
┌────────────────────────────────────────────────────────┐
│             GitHub Repository & Releases               │
│  - Source repository: github.com/<user>/NearGrid      │
│  - Release Tag: v1.0.0                                 │
│  - Hosted Binary: NearGrid-v1.0.apk                    │
└─────────────────────────┬──────────────────────────────┘
                          │
                          ▼
┌────────────────────────────────────────────────────────┐
│                 GitHub Pages Website                   │
│  - Live Site: https://<user>.github.io/NearGrid        │
│  - Docs/index.html Landing Page                        │
│  - Direct Download Button -> GitHub Release APK        │
└─────────────────────────┴──────────────────────────────┘
```

---

## 5. Free Online Publishing Step-by-Step Checklist

- [ ] **Step 1**: Export repository from AI Studio to GitHub.
- [ ] **Step 2**: Generate signed release APK in AI Studio menu.
- [ ] **Step 3**: Create `v1.0.0` release tag on GitHub and upload `.apk`.
- [ ] **Step 4**: Commit landing page code (`docs/index.html`) to repository.
- [ ] **Step 5**: Enable GitHub Pages in repository settings (`/docs` folder).
- [ ] **Step 6**: Test direct APK download on Android device!
