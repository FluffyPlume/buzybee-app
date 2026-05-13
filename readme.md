#  BuzyBee: Dynamic Task Prioritization System

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-FF0000?style=for-the-badge&logo=java&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

**BuzyBee** is a desktop application designed to help students and professionals to create and design their personalized task scheduling system. By utilizing a modified **Time-Weighted Fractional Knapsack Algorithm** paired with **Greedy Task Scheduling**, the system dynamically allocates tasks into available daily free-time blocks based on computed urgency and difficulty ratios. Making an optimized task manager that makes it easy for users to achieve their goals in a timely manner.

## Core Algorithms
* **Time-Weighted Fractional Knapsack:** Calculates a dynamic Priority Ratio `(Difficulty × Urgency) / Estimated Time`. The Urgency Multiplier updates automatically as deadlines approach.
* **Greedy Task Scheduling:** Distributes the mathematically sorted tasks into the user's available Mon-Sun time slots, automatically pushing overflow tasks into a Backlog.

## Project Architecture
This project strictly follows the **MVC (Model-View-Controller)** architecture:
* `/controller` & `/resources/fxml`: UI handling and layout design.
* `/model` & `/dao`: Data objects and local storage (SQLite/JSON) interactions.
* `/scheduler`: Core mathematical algorithm logic.
* `/service`: Business logic validating user inputs and bridging the UI to the algorithms.

## BSCS 2-4 Design and Analysis Final Project | Groupmates
* **ACOB, Sophia Arianne A.**
* **BULALA, Kenneth Andrei G.**
* **GATMEN, Alliah**
* **ZACARIAS, Diana Loreanne M.**


---

##  For Collaborators

### Prerequisites
1. **Java Development Kit (JDK 17 or higher)**
2. **Maven** (Handled automatically by IntelliJ)
3. **IntelliJ IDEA** (Recommended IDE)

### How to Run Locally
1. Clone the repository: `git clone https://github.com/FluffyPlume/buzybee-app.git`
2. Open the project folder in IntelliJ.
3. Wait for Maven to download the dependencies.
4. Navigate to `src/main/java/com/buzybee/app/util/MainApp.java`.
5. Click the green **Run** button.

###  Collaboration Rules
* **Never push directly to the `main` branch!**
* If you are working on a new feature (e.g., the Dashboard UI), create a new branch: `git checkout -b feature-dashboard`.
* Commit your code, push your branch, and let the group review it before merging.


---

## License
This project is licensed under the MIT License. You are free to use, modify, and distribute this software, provided that proper credit is given to the original authors. 
