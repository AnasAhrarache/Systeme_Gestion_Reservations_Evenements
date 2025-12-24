# Organizer Features - Quick Reference Guide

## 🎯 Organizer Capabilities

### What Can an Organizer Do?

#### 1️⃣ **Create Events**

- Click "Créer un événement" button
- Fill in: Title, Description, Category, Dates, Location, City, Capacity, Price, Image
- Save as Draft → Later Publish when ready

#### 2️⃣ **Manage Events**

- View all your events in a organized dashboard
- **Edit** draft events to modify details
- **Publish** draft events to make them live (clients can reserve)
- **Cancel** published events (no new reservations allowed)
- **Delete** draft events (no reservations)
- **View Details** to see full event information

#### 3️⃣ **Monitor Reservations**

- Navigate to "Réservations" to see ALL reservations for your events
- See: Client name, email, places booked, total amount, reservation date
- Search by: Client name, reservation code, or email
- Filter by: Specific event

#### 4️⃣ **Accept/Reject Reservations**

- For each pending reservation (⏳ En attente):
  - ✅ **Accept** - Confirm reservation, optionally add comment for client
  - ❌ **Reject** - Cancel the reservation
- Cannot modify confirmed or already cancelled reservations

---

## 📍 Where to Find Everything

| What You Want to Do | Route                     | Button/Link                      |
| ------------------- | ------------------------- | -------------------------------- |
| Create Event        | organizer/event/new       | "Créer un événement" in header   |
| View All Events     | organizer/events          | Main organizer dashboard         |
| Edit Event          | organizer/event/edit/{id} | ✏️ Edit button in events list    |
| View Reservations   | organizer/reservations    | "Réservations" button in header  |
| Accept Reservation  | organizer/reservations    | ✅ Confirm button (pending only) |
| Reject Reservation  | organizer/reservations    | ❌ Reject button (pending only)  |

---

## 🎨 Status Colors & Meanings

### Event Status

- **🟤 BROUILLON** (Draft) - Still preparing, hidden from clients
- **🟢 PUBLIE** (Published) - Live and accepting reservations
- **🔴 ANNULE** (Cancelled) - No longer available, no new reservations
- **⚫ TERMINE** (Finished) - Event has ended

### Reservation Status

- **🟡 EN ATTENTE** (Pending) - Waiting for your confirmation
- **🟢 CONFIRMEE** (Confirmed) - Client is confirmed
- **🔴 ANNULEE** (Cancelled) - Reservation rejected/cancelled

---

## 💡 Quick Tips

✨ **Best Practices:**

1. Create events in draft mode first
2. Fill in ALL event details before publishing
3. Regularly check reservations tab for new bookings
4. Respond to pending reservations promptly
5. Add helpful comments when accepting/rejecting

⚠️ **Important Rules:**

- Can only edit DRAFT events
- Once published, cannot edit - must cancel and recreate if needed
- Cannot delete events with reservations
- Reservations can only be accepted/rejected if status is "En attente"

🔒 **Permissions:**

- Only organizers can manage their own events
- Admins can view/manage all events
- Cannot see or modify other organizers' events

---

## 📊 Sample User Journey

```
1. LOGIN
   ↓
2. NAVIGATE TO organizer/events (Dashboard)
   ├─ See all my events
   └─ Click "Create Event"
   ↓
3. FILL EVENT FORM
   ├─ Title, dates, location, capacity, price
   └─ SAVE (automatically Draft status)
   ↓
4. RETURN TO DASHBOARD
   └─ Click "Publish" on the draft event
   ↓
5. EVENT NOW LIVE (status = PUBLIE)
   └─ Clients can now reserve seats
   ↓
6. CLICK "RÉSERVATIONS" in header
   ├─ See all pending reservations
   └─ For each pending:
      ├─ Click ✅ to ACCEPT (optionally add comment)
      └─ Click ❌ to REJECT
   ↓
7. MANAGE RESERVATIONS
   └─ Search/filter reservations as needed
```

---

## 🎁 Example: Complete Event Lifecycle

### Phase 1: Event Creation (organizer/event/new)

```
Fill form:
- Title: "Festival de Jazz 2025"
- Category: Concert/Music
- Start: 2025-06-15 19:00
- End: 2025-06-15 23:00
- Location: "Théâtre Mohammed V"
- City: "Casablanca"
- Capacity: 500
- Price: 150 DH
- Image: https://...

Status: BROUILLON (Draft)
```

### Phase 2: Event Published (organizer/events)

```
Dashboard shows event with:
- Title: Festival de Jazz 2025
- Status: 🟤 BROUILLON
- Button: "Publish"

Click Publish → Confirm Dialog
Status changes to: 🟢 PUBLIE
```

### Phase 3: Reservations Arrive (organizer/reservations)

```
View Reservations:
- Code: RES0001234
- Client: Ahmed Hassan
- Event: Festival de Jazz 2025
- Places: 2
- Total: 300 DH
- Status: 🟡 EN ATTENTE

Actions: ✅ Accept or ❌ Reject
```

### Phase 4: Respond to Reservation

```
Click ✅ Accept:
- Dialog opens
- Can add: "Merci de votre réservation!"
- Click Save

Status changes to: 🟢 CONFIRMEE
Client receives confirmation
```

---

## 🔧 System Features

### Automatic Features

- ✅ Reservation code generation (unique for each)
- ✅ Total amount calculation (places × unit price)
- ✅ Date validation (no past dates)
- ✅ Capacity tracking (available seats)
- ✅ Status transitions enforcement
- ✅ Permission-based access control

### Search & Filter

- Search reservations by: Client name, Email, Code
- Filter by: Event
- View: All statuses mixed together

### Reporting

- See: Client details, Reservation amounts, Dates
- Track: Status changes, Comments added

---

## ❓ FAQ

**Q: Can I edit a published event?**
A: No, you must cancel it first, then create a new one.

**Q: Can I reject a confirmed reservation?**
A: No, only pending reservations can be rejected. To cancel confirmed, contact support.

**Q: What happens when I cancel an event?**
A: Clients cannot make new reservations. Existing reservations status doesn't change automatically.

**Q: Can clients change their reservation status?**
A: No, only you (organizer) or admins can confirm/reject reservations.

**Q: Where's my revenue from reservations?**
A: Shown in reservation total amount. For detailed analytics, check event details.

**Q: Can I add refund notes?**
A: Yes, add them as comments when rejecting reservations.

---

## 🚀 Next Steps

After implementing these features:

1. Test creating an event end-to-end
2. Publish the event
3. Create test reservations (as different user)
4. Go to reservations tab
5. Accept/reject some reservations
6. Verify status changes

All features are ready to use! 🎉
