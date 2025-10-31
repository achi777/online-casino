# ადმინისტრატორების როლების სრული სახელმძღვანელო

## 📋 შექმნილი ადმინისტრატორები

ყველა ადმინისტრატორი შექმნილია პაროლით: **Test1234**

| Username   | Email                | Role       | სახელი    | გვარი               | წვდომა                                    |
|------------|----------------------|------------|-----------|---------------------|-------------------------------------------|
| owner      | owner@casino.ge      | OWNER      | გიორგი    | მესვეური            | სრული წვდომა ყველაფერზე ✅                |
| admin      | admin@casino.ge      | ADMIN      | ნინო      | ადმინი              | თითქმის სრული წვდომა 🔐                   |
| finance    | finance@casino.ge    | FINANCE    | დავით     | ფინანსისტი          | ფინანსები, ტრანზაქციები 💰                |
| support    | support@casino.ge    | SUPPORT    | მარიამ    | მხარდამჭერი          | მომხმარებლები, KYC 👥                      |
| content    | content@casino.ge    | CONTENT    | ლაშა      | კონტენტმენეჯერი     | თამაშები, კონტენტი 🎮                     |
| analyst    | analyst@casino.ge    | ANALYST    | თამარ     | ანალიტიკოსი         | რეპორტები (read-only) 📊                 |
| compliance | compliance@casino.ge | COMPLIANCE | გიორგი    | შესაბამისობის მენ.  | KYC, AML, რეგულაციები ⚖️                |

---

## 🔑 როლების უფლებები (Permissions)

### 1. OWNER (მესვეური)
**სრული წვდომა ყველაფერზე**

✅ **Dashboard**
- ყველა სტატისტიკა
- რეალურ დროში მონაცემები
- სრული ანალიტიკა

✅ **მომხმარებლები (Users)**
- ნახვა, რედაქტირება, წაშლა
- KYC დამტკიცება/უარყოფა
- ბალანსის მართვა
- სტატუსის შეცვლა (Active/Blocked/Suspended)
- დეპოზიტ/გამოტანის ლიმიტები
- თვითგამორიცხვა (Self-exclusion)

✅ **ფინანსები (Finance)**
- ყველა ტრანზაქცია
- დეპოზიტების დამტკიცება
- გამოტანების დამტკიცება/უარყოფა
- ბალანსის კორექტირება
- ფინანსური რეპორტები

✅ **თამაშები (Games)**
- თამაშების დამატება/რედაქტირება
- პროვაიდერების მართვა
- თამაშების გააქტიურება/გამორთვა
- RTP პარამეტრები

✅ **რეპორტები (Reports)**
- ყველა ტიპის რეპორტი
- Export Excel/PDF
- დეტალური ანალიტიკა

✅ **ადმინისტრატორები**
- ახალი ადმინების დამატება
- როლების შეცვლა
- ადმინების წაშლა
- უფლებების მართვა

✅ **სისტემა (System)**
- კონფიგურაციები
- პარამეტრები
- Logs
- Backup/Restore

---

### 2. ADMIN (ადმინისტრატორი)
**თითქმის სრული წვდომა (გარდა სისტემის პარამეტრები)**

✅ **Dashboard** - სრული
✅ **მომხმარებლები** - სრული (გარდა წაშლა)
✅ **ფინანსები** - სრული (გარდა ბალანსის კორექტირება)
✅ **თამაშები** - სრული
✅ **რეპორტები** - სრული
✅ **ადმინისტრატორები** - მხოლოდ ნახვა
❌ **სისტემა** - არა წვდომა

**შეზღუდვები:**
- ვერ წაშლის მომხმარებლებს
- ვერ ცვლის სისტემის პარამეტრებს
- ვერ ქმნის OWNER როლის ადმინებს
- ვერ შეცვლის თავის როლს

---

### 3. FINANCE (ფინანსისტი)
**ფინანსური ოპერაციები, ტრანზაქციები**

✅ **Dashboard**
- ფინანსური მეტრიკები
- Deposits/Withdrawals სტატისტიკა
- Revenue მონაცემები

✅ **მომხმარებლები**
- მხოლოდ ნახვა
- ბალანსის ნახვა
- ტრანზაქციების ისტორია

✅ **ფინანსები**
- დეპოზიტების დამტკიცება
- გამოტანების დამტკიცება/უარყოფა
- ტრანზაქციების ისტორია
- ბალანსის კორექტირება
- ფინანსური რეპორტები

✅ **რეპორტები**
- ფინანსური რეპორტები
- Transactions Report
- Revenue Report
- Payment Methods Report

❌ **თამაშები** - არა წვდომა
❌ **ადმინისტრატორები** - არა წვდომა
❌ **სისტემა** - არა წვდომა

**უფლებები:**
- დეპოზიტის დამტკიცება
- გამოტანის დამტკიცება/უარყოფა
- ბალანსის manual adjustment
- ტრანზაქციების ექსპორტი

---

### 4. SUPPORT (მხარდაჭერა)
**მომხმარებლების მხარდაჭერა, KYC**

✅ **Dashboard**
- მომხმარებლების სტატისტიკა
- Active Users
- Pending KYC

✅ **მომხმარებლები**
- სრული ნახვა
- რედაქტირება (Profile Info)
- KYC დოკუმენტების ნახვა
- KYC დამტკიცება/უარყოფა
- სტატუსის შეცვლა
- თვითგამორიცხვის მართვა
- Responsible Gaming Limits

✅ **რეპორტები**
- Users Report
- KYC Report
- Active Sessions

❌ **ფინანსები** - მხოლოდ ნახვა (არა დამტკიცება)
❌ **თამაშები** - არა წვდომა
❌ **ადმინისტრატორები** - არა წვდომა
❌ **სისტემა** - არა წვდომა

**უფლებები:**
- KYC verification
- მომხმარებლის დაბლოკვა/განბლოკვა
- პაროლის რესეტი
- თვითგამორიცხვის დაყენება
- Responsible Gaming ლიმიტები

---

### 5. CONTENT (კონტენტ მენეჯერი)
**თამაშები, პროვაიდერები, კონტენტი**

✅ **Dashboard**
- თამაშების სტატისტიკა
- პოპულარული თამაშები
- თამაშების Performance

✅ **თამაშები**
- თამაშების დამატება/რედაქტირება
- თამაშების გააქტიურება/გამორთვა
- კატეგორიების მართვა
- RTP პარამეტრები
- Featured თამაშები
- Sort Order

✅ **პროვაიდერები**
- პროვაიდერების დამატება/რედაქტირება
- ინტეგრაციის პარამეტრები
- API Configuration

✅ **რეპორტები**
- Games Report
- Most Played Games
- Games Revenue

❌ **მომხმარებლები** - მხოლოდ ნახვა
❌ **ფინანსები** - არა წვდომა
❌ **ადმინისტრატორები** - არა წვდომა
❌ **სისტემა** - არა წვდომა

**უფლებები:**
- ახალი თამაშის დამატება
- თამაშების პარამეტრების რედაქტირება
- პროვაიდერების მართვა
- კონტენტის განახლება

---

### 6. ANALYST (ანალიტიკოსი)
**რეპორტები, სტატისტიკა (read-only)**

✅ **Dashboard**
- სრული Dashboard
- ყველა მეტრიკა
- რეალურ დროში მონაცემები

✅ **რეპორტები**
- ყველა ტიპის რეპორტი
- Export Excel/PDF
- დეტალური ანალიტიკა
- Custom Date Ranges
- Advanced Filters

✅ **ნახვა (Read-Only)**
- მომხმარებლები (მხოლოდ ნახვა)
- ტრანზაქციები (მხოლოდ ნახვა)
- თამაშები (მხოლოდ ნახვა)
- ფინანსები (მხოლოდ ნახვა)

❌ **რედაქტირება** - არცერთი
❌ **დამტკიცება** - არცერთი
❌ **წაშლა** - არცერთი
❌ **ადმინისტრატორები** - არა წვდომა
❌ **სისტემა** - არა წვდომა

**უფლებები:**
- რეპორტების გენერაცია
- მონაცემების ექსპორტი
- Dashboard ნახვა
- მხოლოდ read-only წვდომა

---

### 7. COMPLIANCE (შესაბამისობის მენეჯერი)
**რეგულაციები, KYC, AML**

✅ **Dashboard**
- KYC Pending
- AML Alerts
- Compliance Metrics

✅ **მომხმარებლები**
- KYC დოკუმენტების ნახვა
- KYC დამტკიცება/უარყოფა
- AML ანალიზი
- Suspicious Activity
- High-Risk Users

✅ **Compliance**
- AML Reports
- KYC Statistics
- Regulatory Reports
- Audit Logs

✅ **რეპორტები**
- KYC Report
- AML Report
- Compliance Report
- Audit Report

❌ **ფინანსები** - მხოლოდ ნახვა (გამოტანის დამტკიცება არა)
❌ **თამაშები** - არა წვდომა
❌ **ადმინისტრატორები** - არა წვდომა
❌ **სისტემა** - არა წვდომა

**უფლებები:**
- KYC verification
- AML investigation
- Suspicious Activity Report (SAR)
- რეგულაციური რეპორტები
- Audit logs წვდომა

---

## 🎯 მთავარი ფუნქციების Permissions Matrix

| ფუნქცია                           | OWNER | ADMIN | FINANCE | SUPPORT | CONTENT | ANALYST | COMPLIANCE |
|----------------------------------|-------|-------|---------|---------|---------|---------|------------|
| Dashboard სრული                  | ✅    | ✅    | 📊      | 👥      | 🎮      | ✅      | ⚖️         |
| მომხმარებლების დამატება            | ✅    | ✅    | ❌      | ❌      | ❌      | ❌      | ❌         |
| მომხმარებლების რედაქტირება         | ✅    | ✅    | ❌      | ✅      | ❌      | ❌      | ❌         |
| მომხმარებლების წაშლა               | ✅    | ❌    | ❌      | ❌      | ❌      | ❌      | ❌         |
| KYC დამტკიცება                   | ✅    | ✅    | ❌      | ✅      | ❌      | ❌      | ✅         |
| დეპოზიტის დამტკიცება               | ✅    | ✅    | ✅      | ❌      | ❌      | ❌      | ❌         |
| გამოტანის დამტკიცება               | ✅    | ✅    | ✅      | ❌      | ❌      | ❌      | ❌         |
| ბალანსის კორექტირება              | ✅    | ❌    | ✅      | ❌      | ❌      | ❌      | ❌         |
| თამაშების დამატება/რედაქტირება     | ✅    | ✅    | ❌      | ❌      | ✅      | ❌      | ❌         |
| პროვაიდერების მართვა              | ✅    | ✅    | ❌      | ❌      | ✅      | ❌      | ❌         |
| ყველა რეპორტი                    | ✅    | ✅    | 💰      | 👥      | 🎮      | ✅      | ⚖️         |
| ადმინების მართვა                  | ✅    | 👁️    | ❌      | ❌      | ❌      | ❌      | ❌         |
| სისტემის პარამეტრები              | ✅    | ❌    | ❌      | ❌      | ❌      | ❌      | ❌         |

**ლეგენდა:**
- ✅ სრული წვდომა
- 👁️ მხოლოდ ნახვა
- 📊 ფინანსური მეტრიკები
- 👥 მომხმარებლების მეტრიკები
- 🎮 თამაშების მეტრიკები
- 💰 ფინანსური რეპორტები
- ⚖️ Compliance მეტრიკები
- ❌ არა წვდომა

---

## 🔐 შესვლის ინსტრუქცია

### 1. Admin Panel-ში შესვლა

```
URL: http://localhost:3002/login
```

### 2. Credentials

აირჩიეთ რომელი როლით გნებავთ შესვლა:

**OWNER როლი:**
```
Username: owner
Password: Test1234
```

**ADMIN როლი:**
```
Username: admin
Password: Test1234
```

**FINANCE როლი:**
```
Username: finance
Password: Test1234
```

**SUPPORT როლი:**
```
Username: support
Password: Test1234
```

**CONTENT როლი:**
```
Username: content
Password: Test1234
```

**ANALYST როლი:**
```
Username: analyst
Password: Test1234
```

**COMPLIANCE როლი:**
```
Username: compliance
Password: Test1234
```

---

## 📱 Frontend Role-Based Access Control

Frontend-ზე უნდა დამალოთ/გამოჩნდეს მხოლოდ ის მენიუები და ფუნქციები, რაზეც აქვთ წვდომა:

### Navigation Menu

```javascript
// OWNER - ხედავს ყველაფერს
Dashboard
├── Users
├── Finance
│   ├── Deposits
│   ├── Withdrawals
│   └── Transactions
├── Games
│   ├── All Games
│   └── Providers
├── Reports
│   ├── Financial
│   ├── Users
│   └── Games
├── Admins
│   └── Manage Admins
└── System
    ├── Settings
    └── Logs

// FINANCE - ხედავს მხოლოდ ფინანსურ ბლოკებს
Dashboard (Financial Metrics)
├── Users (View Only)
├── Finance
│   ├── Deposits
│   ├── Withdrawals
│   └── Transactions
└── Reports
    └── Financial

// SUPPORT - ხედავს მომხმარებლების მართვას
Dashboard (User Metrics)
├── Users
│   ├── All Users
│   ├── KYC Pending
│   └── Self-Exclusion
└── Reports
    └── Users

// CONTENT - ხედავს თამაშებს
Dashboard (Games Metrics)
├── Games
│   ├── All Games
│   ├── Add New Game
│   └── Providers
└── Reports
    └── Games

// ANALYST - ხედავს ყველაფერს read-only
Dashboard (All Metrics)
├── Users (View Only)
├── Finance (View Only)
├── Games (View Only)
└── Reports (All Reports)

// COMPLIANCE - ხედავს KYC და AML
Dashboard (Compliance Metrics)
├── Users
│   ├── KYC Verification
│   └── AML Alerts
└── Reports
    ├── KYC
    ├── AML
    └── Compliance
```

---

## ⚙️ Backend API Permissions

Backend API-ზე უნდა შემოწმდეს როლი:

```java
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
@PostMapping("/users/delete")
public ResponseEntity<?> deleteUser(@RequestParam Long userId) {
    // მხოლოდ OWNER და ADMIN-ს შეუძლია
}

@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('FINANCE')")
@PostMapping("/withdrawals/approve")
public ResponseEntity<?> approveWithdrawal(@RequestParam Long id) {
    // OWNER, ADMIN, FINANCE-ს შეუძლია
}

@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('SUPPORT') or hasRole('COMPLIANCE')")
@PostMapping("/kyc/approve")
public ResponseEntity<?> approveKyc(@RequestParam Long userId) {
    // OWNER, ADMIN, SUPPORT, COMPLIANCE-ს შეუძლია
}

@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('CONTENT')")
@PostMapping("/games/add")
public ResponseEntity<?> addGame(@RequestBody GameRequest request) {
    // OWNER, ADMIN, CONTENT-ს შეუძლია
}
```

---

## 🎨 UI Components Permission Check

```typescript
// React/TypeScript მაგალითი
const hasPermission = (requiredRoles: string[]) => {
  const userRole = getCurrentUserRole(); // 'OWNER', 'ADMIN', etc.
  return requiredRoles.includes(userRole);
};

// გამოყენება
{hasPermission(['OWNER', 'ADMIN']) && (
  <Button onClick={deleteUser}>Delete User</Button>
)}

{hasPermission(['OWNER', 'ADMIN', 'FINANCE']) && (
  <Button onClick={approveWithdrawal}>Approve</Button>
)}

{hasPermission(['OWNER', 'ADMIN', 'CONTENT']) && (
  <Button onClick={addGame}>Add Game</Button>
)}
```

---

## 📊 რეკომენდაციები

### უსაფრთხოება

1. **პაროლების შეცვლა** - პირველ შესვლაზე შეცვალეთ `Test1234` პაროლი
2. **Two-Factor Authentication** - დაამატეთ 2FA OWNER და ADMIN როლებისთვის
3. **Session Timeout** - დააყენეთ 30 წუთიანი inactivity timeout
4. **IP Whitelist** - OWNER და ADMIN-ის მხოლოდ კონკრეტული IP-დან წვდომა

### ლოგირება

1. **Audit Log** - ყველა admin მოქმედება უნდა ლოგირდებოდეს
2. **Critical Actions** - დელეტი, ბალანსის ცვლილება, KYC დამტკიცება
3. **Login History** - ყველა შესვლის ისტორია

### მონიტორინგი

1. **Failed Login Attempts** - 5 წარუმატებელი მცდელობის შემდეგ block
2. **Suspicious Activity** - უჩვეულო მოქმედებების გაფრთხილება
3. **Role Changes** - როლის შეცვლა აუცილებლად notify OWNER-ს

---

## 🚀 შემდეგი ნაბიჯები

1. ✅ Backend Role System - **დასრულებული**
2. ✅ ყველა როლის ადმინები შექმნილია - **დასრულებული**
3. ⏳ Frontend Role-Based Navigation
4. ⏳ Permission Checks on UI Components
5. ⏳ Backend API Authorization
6. ⏳ Audit Logging System

---

**გააქტიურებულია:** 7 როლი, 7 ადმინისტრატორი
**შესვლა:** http://localhost:3002/login
**პაროლი:** Test1234 (ყველასთვის)

დროებით დატესტეთ ყველა როლით და დარწმუნდით რომ თითოეული მომხმარებელი ხედავს მხოლოდ იმ ფუნქციებს, რაზეც აქვს წვდომა! 🎉
