# NearGrid — Coordinate-Powered Neighborhood Connection

NearGrid is a location-aware neighborhood coordination platform built with Jetpack Compose and Kotlin. It connects residents within mixed suburban neighborhoods and local street clusters through privacy-preserving geographic cells, putting intent-based matching for community needs, resource sharing, and local civic resolution at the center of the experience.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and UX choices have been confirmed based on user feedback:
> - **Pre-configured Community Environment**: Mixed suburban neighborhood and local street cluster (featuring Oakwood Grove, Elmwood Avenue, Pinecrest Park, and surrounding neighborhood blocks).
> - **Coordinate Model & Privacy**: Interactive location mode with preset neighborhood cells (H3-style spatial indexing) and a real-time GPS toggle. Users' exact home coordinates are never revealed; public interactions only display approximate distance bands ("within 350 m", "in Oakwood West cell") and adjustable discovery radii (300 m, 800 m, 2 km).
> - **Hero Workflow**: Intent-Based Matching Engine for local needs and resource exchange ("Ask Nearby" & "Share & Exchange"), supported by community notices, issue resolution tracking, and micro-meetup coordination.

---

## 1. Overview & Core Concept

### What It Does
NearGrid eliminates the traditional friction of asking neighbors for help, tools, or shared activities. Rather than an aimless social media feed, it acts as a **proximity-based coordination network**:
1. **Coordinate-Powered Neighborhood Grid**: Automatically organizes localities into discrete privacy-preserving geographic cells (e.g., Cell #H3-8826, Oakwood Grove Center, Pinehurst West).
2. **Intent-Based Matching Engine**: Connects "Needs" (e.g., "Need a 10-foot ladder for 2 hours", "Looking for a math tutor") directly with nearby "Offers" (e.g., "Power tools available to lend", "Calculus tutoring available"), calculating compatibility scores, distance bands, and availability windows.
3. **Local Resource Exchange & Pickup Coordination**: Structured workflow with reservation requests, pickup/dropoff points, and automatic expiration timers.
4. **Community Noticeboard & Governance**: Official neighborhood announcements, alerts (e.g., scheduled water maintenance), and community polls.
5. **Issue-to-Resolution Tracker**: Civic problem reporting (e.g., broken streetlight on Elm St, fallen tree branch) with live status stages: *Reported → Acknowledged → In Progress → Resolved*.
6. **Micro-Meetups & Interest Circles**: Spontaneous local meetups (casual cricket, weekend walking club, gardening circle) with RSVP tracking.
7. **In-App Neighbor Conversations**: End-to-end local messaging for coordinated pickups and handoffs.

### Target Audience & Persona
- **Suburban Residents & Families**: Borrowing tools, asking for quick neighborhood favors, tracking local street updates.
- **Local Volunteers & Coordinators**: Managing community cleanups, resolving neighborhood issues, organizing local events.
- **New Movers & Active Neighbors**: Finding nearby interest buddies (cricket, walking, study groups) without sacrificing location privacy.

---

## 2. User Experience & Visual Design

### Visual Identity & Theme
- **Aesthetic Direction**: Warm Modern Editorial with Civic Polish. Warm cream surfaces, forest greens, terracotta/ochre accents, and crisp spatial hierarchy that feels welcoming, reliable, and grounded.
- **Color Palette (M3 Semantic Tokens)**:
  - `Primary`: Deep Forest Green (`#1E3E2B`) — represents trust, local nature, and stability.
  - `OnPrimary`: Pure White (`#FFFFFF`).
  - `PrimaryContainer`: Soft Mint Sage (`#DDE8DD`) — subtle highlighting for active chips and selected cells.
  - `Secondary`: Earthy Terracotta / Ochre (`#C86D27`) — highlights urgent needs, status badges, and action intents.
  - `Background`: Warm Porcelaine (`#FBFBF9`) — prevents harsh screen glare.
  - `Surface`: Clean Warm White (`#FFFFFF`).
  - `SurfaceVariant`: Subtle Stone Gray (`#F0EFEA`).
  - `Outline`: Soft Sand Gray (`#D9D7CE`).
- **Typography & Hierarchy**:
  - Display & Headlines: Crisp, modern humanist sans typography with deliberate weight contrast.
  - Body & Labels: High-legibility sans with generous line-height and letter-spacing for quick scanning outdoors.
- **Geographic Cell Motif**:
  - Interactive grid indicator showing active cell identifier, estimated neighbor density, and radius slider (300m, 800m, 2km).
  - Hexagonal/pill badges for distance bands (`~350 m away`, `Within your street block`).

### Core Navigation Architecture (5 Hubs)
1. **Home (Intent & Feed)**: Hero Intent Matcher card ("Ask for Help", "Share / Lend", "Find People"), active Needs & Offers feed with compatibility tags, distance filter chips, and daily neighborhood summary.
2. **Grid / Explore (Living Neighborhood Map)**: Interactive visual grid with active neighborhood cells, local activity density heatmap, meeting spots, and community notices layer. Cell switcher allows exploring adjacent blocks.
3. **Action / Create (New Intent or Report)**: Unified bottom sheet to post a Need (Request), Offer an item/skill, Report a Civic Issue, or Post a Micro-Meetup.
4. **Community (Notices & Civic Issues)**: Official noticeboard, Issue-to-Resolution tracker (with status chips and upvotes), and active community polls.
5. **Inbox & Profile**: Active coordination chats, exchange handoff confirmations, and privacy/reputation profile settings.

---

## 3. Key Product Decisions & Trade-Offs

| Decision | Chosen Approach | Rationale & Trade-offs |
|---|---|---|
| **Data Persistence** | **Local Room Database with Pre-seeded Neighborhood Data** | Ensures complete, reliable, and instant offline-first experience without requiring mandatory Google Cloud credentials or network latency during emulator testing. Includes pre-seeded neighbors, active requests, civic issues, and notices for realistic testing. |
| **Location Strategy** | **Discrete Grid Cells + Distance Bands with Interactive Toggle** | Avoids exposing sensitive residential GPS coordinates. Computes Euclidean & Haversine distance bands into fuzzy categories (`< 400 m`, `< 1 km`, `< 2 km`). Provides simulated cell switching (Oakwood Center, Elmwood, Pinehurst) plus optional live device GPS. |
| **Intent Matching** | **Smart Keyword & Category Proximity Engine** | Matches requester keywords and item tags (e.g. "ladder", "tools", "math", "cricket") with nearby active offers. Ranks items by proximity band and match score. |
| **State Management** | **Kotlin Coroutines + StateFlow + MVVM** | Standard Jetpack Compose best practice for clean separation of concerns, reactive UI updates, and testability. |

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
├──────────────────┬──────────────────┬─────────────────┬────────────────┤
│    HomeScreen    │  GridMapScreen   │ CommunityScreen │  InboxScreen   │
│ (Intent Matches, │(Interactive Grid,│ (Noticeboard &  │ (Coordination  │
│  Needs & Offers) │  Activity Cells) │  Issue Tracker) │  Chat/Handoff) │
└────────┬─────────┴────────┬─────────┴────────┬────────┴────────┬───────┘
         │                  │                  │                 │
         └──────────────────┼──────────────────┼─────────────────┘
                            ▼                  ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        NearGridViewModel                               │
│  - Active Discovery Radius (300m / 800m / 2km)                         │
│  - Current Neighborhood Cell (e.g., "Oakwood Grove #204")              │
│  - Filter State (All, Needs, Offers, Events, Urgent)                   │
│  - Active Intent Matchmaker Computation                                │
│  - Issue Status Updates (Reported -> In Progress -> Resolved)          │
└───────────────────────────┬────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────────┐
│                      NearGridRepository                                │
│  - Distance Band & Cell Proximity Filtering                            │
│  - Intent Matching Algorithm (Need ↔ Offer cross-referencing)          │
│  - Local Data Seeding (Realistic suburban community mock data)         │
└───────────────────────────┬────────────────────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────────────────────┐
│                      Room Database (SQLite)                            │
│  ┌───────────────────────┬───────────────────────┬──────────────────┐  │
│  │     NeighborItem      │    CommunityIssue     │ CommunityNotice  │  │
│  │ (Needs, Offers, Meets)│ (Status, Upvotes, Loc)│ (Urgent, Dept)   │  │
│  ├───────────────────────┼───────────────────────┼──────────────────┤  │
│  │     GridCellInfo      │     Conversation      │   ChatMessage    │  │
│  │  (Coordinates, Bounds)│ (Participants, ItemId)│  (Sender, Body)  │  │
│  └───────────────────────┴───────────────────────┴──────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

### Core Room Entities
1. `NeighborPostEntity`:
   - `id: String`, `type: PostType` (NEED, OFFER, MEETUP)
   - `authorName: String`, `authorAvatar: String`, `authorCellId: String`
   - `title: String`, `description: String`, `category: String` (TOOLS, HELP, SPORT, STUDY, HOUSEHOLD)
   - `urgency: UrgencyLevel` (LOW, MEDIUM, HIGH)
   - `latitude: Double`, `longitude: Double` (internal reference for distance computation)
   - `approxDistanceBand: String` (e.g., "within 350 m")
   - `expiresInHours: Int`, `status: PostStatus` (ACTIVE, MATCHED, COMPLETED)
   - `createdAt: Long`

2. `CommunityIssueEntity`:
   - `id: String`, `title: String`, `locationDescription: String`
   - `category: IssueCategory` (INFRASTRUCTURE, SAFETY, LIGHTING, SANITATION)
   - `status: IssueStatus` (REPORTED, ACKNOWLEDGED, IN_PROGRESS, RESOLVED)
   - `upvotesCount: Int`, `assignedVolunteer: String?`, `createdAt: Long`

3. `CommunityNoticeEntity`:
   - `id: String`, `title: String`, `body: String`, `department: String`
   - `isPinned: Boolean`, `isUrgent: Boolean`, `postedAt: Long`

4. `ChatMessageEntity` & `ConversationEntity`:
   - Handles real-time neighbor chats for coordinating handoffs and meetups.

---

## 5. Execution Stages

1. **Phase 1 — Data & Persistence Core**: Setup Room database with entities, DAOs, type converters, and repository pre-seeded with realistic suburban community data.
2. **Phase 2 — Theme, Typography & Design Tokens**: Configure M3 ColorScheme, Shapes, and typographic scale tailored to NearGrid's warm civic identity.
3. **Phase 3 — UI Screens & Components**:
   - `HomeScreen`: Top app bar with cell selector & radius picker, Quick Action intent cards, intent-match alerts, filterable list of neighborhood needs and offers.
   - `GridExploreScreen`: Interactive visual cell grid canvas displaying neighbor density, public hubs, and cell switching.
   - `CreateItemDialog / Sheet`: Clean dialog to post needs, offers, issues, or meetups.
   - `CommunityScreen`: Dual-tab interface for Noticeboard and Issue-to-Resolution tracker with interactive upvoting and status changes.
   - `InboxScreen & ChatView`: Conversation list and direct messaging interface with pickup agreement badges.
4. **Phase 4 — Verification & Polish**: Ensure edge-to-edge drawing, safe area padding, seamless state handling, test tags, and compile verification.
