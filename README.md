# MAD25_T01_Team3  
# StudyBuddy – Student Productivity & Learning Companion

## Table of Contents

1. [Introduction](#introduction)
2. [App Category](#app-category)
3. [Motivation & Objectives](#motivation--objectives)
4. [Design Considerations](#design-considerations)
5. [Feature Overview](#feature-overview)
6. [Stage 1 Feature Responsibilities](#stage-1-feature-responsibilities)
7. [Planned Stage 2 Features](#planned-stage-2-features)
8. [LLM Usage Declaration](#llm-usage-declaration)
9. [Team Members](#team-members)
10. [Disclaimer](#disclaimer)

---

## Introduction

**StudyBuddy** is an educational productivity mobile application designed to help students organize their study life, stay focused, and remain motivated.  
By integrating note-taking, focus timers, quizzes, and motivational content, StudyBuddy acts as an all-in-one personal study assistant.

The app is suitable for:

- Primary School  
- Secondary School  
- JC / Poly / ITE  
- University students  
- Working adults seeking productive learning habits  

---

## App Category

StudyBuddy falls under the **Education** category, focusing on:

- Study Productivity  
- Time Management  
- Note Organization  
- Self-Learning Tools  
- Motivation & Study Discipline  

---

## Motivation & Objectives

Students often struggle with:

- Losing notes  
- Difficulty focusing  
- Lack of motivation  
- Poor time management  
- Inefficient revision habits  

### StudyBuddy aims to solve these by:

- Offering a centralized platform for studying  
- Encouraging productive routines using Pomodoro  
- Providing daily inspiration and study tips  
- Supporting self-assessment through quizzes  
- Allowing easy and structured note-taking  

The primary objective is to help students build **consistent, organized, and effective study habits**.

---

## Design Considerations

### 1. Student-Centric UI  
A clean interface that reduces cognitive load and improves navigation for younger and older students.

### 2. Modular Functions  
Each module (Notes, Timer, Motivation Hub, Quiz Zone) is independently built for scalability and clarity.

### 3. Offline-First Architecture  
Core features such as **Notes, Motivational quotes, and Quizzes** are now stored using Firebase Firestore, ensuring secure cloud backup and multi-device accessibility.
The **Pomodoro timer** will have its data stored offline in Share Preference.

### 4. Consistency & Familiarity  
Design inspiration taken from Notion, Quizlet, and Forest to maintain familiarity while offering unique features.

### 5. Expandability for Stage 2  
The structure allows future enhancements such as cloud sync, analytics, and notification-based reminders. (Add more information)

---

## Feature Overview

### **1. Login + Notes Manager** (By Pey Zhi Xun)
- Handles user login and authentication  
- CRUD operations for study notes  
- Categorization by subject / module  

### **2. Motivation Hub** (By Fan Zhizhong)
- Displays daily motivational quotes  
- Users can save favourite quotes to local storage  
- Includes study tips  
- May include background images for multimedia requirement  

### **3. Study Timer (Pomodoro)** (By Arjun)
- 25-minute focus cycles  
- 5-minute rest cycles  
- Simple animations and timer display  
- Helps maintain consistent study habits  

### **4. Quiz Zone** (By Daniel)
- Subject-based multiple-choice quizzes  
- Local JSON / database question storage  
- Displays scores and correctness for self-assessment  

---

## Stage 1 Feature Responsibilities

| Member | Feature | Description |
|--------|---------|-------------|
| **Pey Zhi Xun** | Login + Notes Manager | Authentication + notes CRUD and organization |
| **Fan Zhizhong** | Motivation Hub | Quotes, study tips, and favourite saving |
| **Arjun** | Study Timer | Pomodoro timer and UI for focus sessions |
| **Daniel** | Quiz Zone | Quiz interface, scoring logic, and question sets |

---

## Planned Stage 2 Features

Each team member will implement one *stand-alone* feature requiring new concepts, such as a new Activity, background Service, or advanced API.

> **To be updated once finalized by the team.**

| Member | Planned Stage 2 Feature |
|--------|--------------------------|
| **Pey Zhi Xun** | *To be added* |
| **Fan Zhizhong** | *To be added* |
| **Arjun** | *To be added* |
| **Daniel** | *To be added* |

---

## LLM Usage Declaration

This project uses **ChatGPT** to assist with:

- Kotlin syntax clarification  
- Debugging and code review  
- Documentation writing  
- UI/UX layout suggestions  
- Learning Android concepts (Room, ViewModel, etc.)  

**All team members** (Zhizhong, Zhi Xun, Arjun, Daniel) used ChatGPT as a learning and support tool.  
All AI output was **reviewed, edited, and customized** before inclusion in code.

---

## Team Members

<table>
  <tr>
    <td align="center"><a href="https://github.com/whyjsjejwj"><img src="https://github.com/whyjsjejwj.png" width="100px;" alt=""/><br /><sub><b>Fan Zhizhong</b></sub></a></td>
    <td align="center"><a href="https://github.com/PeyZhiXun"><img src="https://github.com/PeyZhiXun.png" width="100px;" alt=""/><br /><sub><b>Pey Zhi Xun</b></sub></a></td>
    <td align="center"><a href="https://github.com/mk1342"><img src="https://github.com/mk1342.png" width="100px;" alt=""/><br /><sub><b>Arjun Vivekananthan</b></sub></a></td>
    <td align="center"><a href="https://github.com/Daniel-Sha14"><img src="https://github.com/Daniel-Sha14.png" width="100px;" alt=""/><br /><sub><b>Daniel Sha</b></sub></a></td>
  </tr>
</table>



---

## Disclaimer

This is a student assignment project for the **Mobile App Development (MAD)** module at **Ngee Ann Polytechnic**.  
This project is developed solely for **educational purposes**.

