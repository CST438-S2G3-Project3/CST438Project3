# Product Requirements Document (PRD): `Legally Distinct Trip Advisor`

[Back to the Project 03 guide](README.md)

**Team:** `03` · **Repo:** `https://github.com/CST438-S2G3-Project3/CST438Project3` · **Last updated:** `10/29/2026`

---

## 1. Users

- **Primary user:** `College students and young adults planning weekend trips or vacations with friends who want to organize destinations and activities in one place.`
- **Secondary user (optional):** `A trip participant who wants to view and contribute to an existing itinerary.`

## 2. Problem

`Students planning trips with friends often struggle to keep destinations, activities, and schedules organized because information is spread across group chats, notes, and multiple apps. Existing travel platforms often focus on booking or discovering destinations rather than providing a simple centralized place to organize a group's itinerary.`

## 3. Success measures

| #   | Measure                                                | Target              | How we check it                      |
| --- | ------------------------------------------------------ | ------------------- | ------------------------------------ |
| S1  | `Time to create a trip and add its first destination ` | `Under 2 minutes`   | `Timed during onboarding test`       |
| S2  | `Successful trip creation and activity scheduling`     | `90% of test users complete the task`  | `Playwright end-to-end test and user testing` |
| S3  | `Successful itinerary retrieval`                       | `100% of saved activities appear after reopening the trip` | `Automated API and end-to-end tests` |

## 4. Scope: MVP, vertical slice, and not building

- **MVP stories:** `PRD-1 through PRD-7`
- **Vertical slice:** `A signed-in user creates a trip, adds a destination, schedules an activity, and views the saved itinerary.`
- **Not building (this term):** `Flight or hotel booking, payment processing, live navigation or maps, and real-time collavorative editing.`

## 5. User stories and acceptance criteria

| ID    | Story                                                    | Acceptance criteria                                                       |
| ----- | -------------------------------------------------------- | ------------------------------------------------------------------------- |
| PRD-1 | As a `user`, I want to `sign in with GitHub or Google` so that `I can access my trips securely`. | **Given** `I have a verified provider account`, **when** `I sign in`, **then** `I receive access to my account and its trips`. |
| PRD-2 | As a `user`, I want to `create and manage trips so that I can organize travel plans`. | **Given** `I am signed in`, **when** `I create a trip with a name and dates`, **then** `the trip is saved and appears in my trip list`. |
| PRD-3 | As a `user`, I want to `add destinations to a trip so that I can organize the places I plan to visit`. | **Given** `I own a trip`, **when** `I add a destination with a name and location`, **then** `it appears in that trip's destinations`. |
| PRD-4 | As a `user`, I want to `schedule activities at destinations so that I can create an itinerary`. | **Given** `I own a trip with a destination`, **when** `I add an activity with a title, date, and time`, **then** `it appears in the trip itinerary`. |
| PRD-5 | As a `user`, I want to `view my itinerary so that I can see my planned activities in order`. | **Given** `I have saved activities`, **when** `I open my trip`, **then** `I see its destinations and activities organized by date and time`. |
| PRD-6 | As a `user`, I want to `update my profile so that my account information stays current`. | **Given** `I am signed in`, **when** `I update my profile`, **then** `the updated information is saved and displayed`. |
| PRD-7 | As a `user`, I want to `delete my account so that I can remove my personal data`. | *8Given** `I confirm account deletion`, **when** `my account is deleted`, **then** `my trips, destinations, and activities are also removed`. |

### Change log

| Date     | PR     | What changed and why |
| -------- | ------ | -------------------- |
| `10/29/26` | `#0` | Initial PRD draft    |
