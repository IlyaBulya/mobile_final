import asyncio
from datetime import datetime
from telegram.ext import Application
import json
import os

# Конфигурация
TELEGRAM_TOKEN = '7530750114:AAEdeMlYaLfCz7E3bh6EVc82QkVL6a97c5U'
CHAT_ID = "5515381121"

# Категории и приоритеты
CATEGORIES = ["1. Work", "2. Personal", "3. Study", "4. Shopping"]
PRIORITIES = ["1. High", "2. Medium", "3. Low"]

# Пути к файлам
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = os.path.join(BASE_DIR, 'data')
FILE_PATH = os.path.join(DATA_DIR, 'tasks.json')

# Добавим отладочную печать
print(f"BASE_DIR: {BASE_DIR}")
print(f"DATA_DIR: {DATA_DIR}")
print(f"FILE_PATH: {FILE_PATH}")

# Создаем директорию и файл
os.makedirs(DATA_DIR, exist_ok=True)
if not os.path.exists(FILE_PATH):
    with open(FILE_PATH, 'w', encoding='utf-8') as f:
        json.dump([], f)

class TelegramNotifier:
    def __init__(self):
        self.app = Application.builder().token(TELEGRAM_TOKEN).build()
        
    async def send_notification(self, message):
        try:
            async with self.app:
                await self.app.bot.send_message(chat_id=CHAT_ID, text=message)
            return True
        except Exception as e:
            print(f"Notification error: {e}")
            return False

class TaskManager:
    def __init__(self):
        self.tasks = []
        self.notifier = TelegramNotifier()
        self.load_tasks()

    def load_tasks(self):
        try:
            with open(FILE_PATH, 'r', encoding='utf-8') as f:
                self.tasks = json.load(f)
        except (FileNotFoundError, json.JSONDecodeError):
            self.tasks = []
            self.save_tasks()

    def save_tasks(self):
        with open(FILE_PATH, 'w', encoding='utf-8') as f:
            json.dump(self.tasks, f, indent=2, ensure_ascii=False)

    async def add_task(self):
        try:
            title = input("Title: ").strip()
            desc = input("Description: ").strip()
            
            print("\nCategories:", ", ".join(CATEGORIES))
            category = input("Category: ").strip()
            try:
                cat_num = int(category)
                if 1 <= cat_num <= len(CATEGORIES):
                    category = CATEGORIES[cat_num - 1]
                else:
                    print(f"Please enter number from 1 to {len(CATEGORIES)}")
                    return False
            except ValueError:
                print("Please enter a valid number")
                return False
            
            print("Priorities:", ", ".join(PRIORITIES))
            priority = input("Priority: ").strip()
            try:
                pri_num = int(priority)
                if 1 <= pri_num <= len(PRIORITIES):
                    priority = PRIORITIES[pri_num - 1]
                else:
                    print(f"Please enter number from 1 to {len(PRIORITIES)}")
                    return False
            except ValueError:
                print("Please enter a valid number")
                return False

            deadline = input("Deadline (YYYY-MM-DD): ").strip()
            try:
                datetime.strptime(deadline, "%Y-%m-%d")
            except ValueError:
                print("Invalid date format. Use YYYY-MM-DD")
                return False

            created_time = datetime.now().strftime("%Y-%m-%d %H:%M")
            
            task = {
                "id": len(self.tasks) + 1,
                "title": title,
                "description": desc,
                "category": category,
                "priority": priority,
                "deadline": deadline,
                "created": created_time
            }
            
            self.tasks.append(task)
            self.save_tasks()

            notification = f"""🆕 New task added:
📌 Task ID: {task['id']}
📝 Title: {title}
📄 Description: {desc}
📁 Category: {category}
⚡ Priority: {priority}
📅 Deadline: {deadline}
🕒 Created: {created_time}"""

            await self.notifier.send_notification(notification)
            print("\nTask added successfully!")
            return True
            
        except Exception as e:
            print(f"Error adding task: {e}")
            return False

    async def view_tasks(self):
        if not self.tasks:
            notification = "📝 No tasks available."
            await self.notifier.send_notification(notification)
            print("\nNo tasks available.")
            return

        notification = "📋 Tasks List:\n"
        for task in self.tasks:
            notification += f"""
📌 Task #{task['id']}
📝 Title: {task['title']}
📄 Description: {task['description']}
📁 Category: {task['category']}
⚡ Priority: {task['priority']}
📅 Deadline: {task['deadline']}
🕒 Created: {task['created']}
─────────────────"""

        await self.notifier.send_notification(notification)
        print("\nTasks list sent to Telegram!")

    async def delete_task(self):
        await self.view_tasks()
        try:
            task_id = int(input("\nEnter Task ID to delete: "))
            task = next((t for t in self.tasks if t['id'] == task_id), None)
            if task:
                self.tasks.remove(task)
                self.save_tasks()
                deleted_time = datetime.now().strftime("%Y-%m-%d %H:%M")
                
                notification = f"""❌ Task deleted:
📌 Task ID: {task['id']}
📝 Title: {task['title']}
📄 Description: {task['description']}
📁 Category: {task['category']}
⚡ Priority: {task['priority']}
📅 Deadline: {task['deadline']}
🕒 Deleted: {deleted_time}"""
                
                await self.notifier.send_notification(notification)
                print("\nTask deleted successfully!")
            else:
                print("Task not found.")
        except ValueError:
            print("Invalid input.")

async def main():
    try:
        manager = TaskManager()
        while True:
            print("\n=== Task Manager ===")
            print("1. Add Task\n2. View Tasks\n3. Delete Task\n4. Exit")
            choice = input("\nSelect action (1-4): ").strip()
            
            if choice == "1":
                await manager.add_task()
            elif choice == "2":
                await manager.view_tasks()
            elif choice == "3":
                await manager.delete_task()
            elif choice == "4":
                print("Goodbye!")
                break
            else:
                print("Invalid choice. Try again.")
            
    except KeyboardInterrupt:
        print("\nПрограмма завершена пользователем.")
        return
    except Exception as e:
        print(f"\nПроизошла ошибка: {e}")
        return

if __name__ == "__main__":
    asyncio.run(main())
